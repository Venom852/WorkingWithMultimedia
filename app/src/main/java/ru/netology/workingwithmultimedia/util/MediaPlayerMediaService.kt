package ru.netology.workingwithmultimedia.util

import android.media.MediaPlayer
import android.os.Build
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import javax.inject.Inject

/**
 * Реализация MediaService, которая работает со стандартным MediaPlayer
 */
class MediaPlayerMediaService @Inject constructor() : MediaService {
    private val mediaPlayer = MediaPlayer() // Переиспользуем 1 инстанс плеера
    private var completionListener = {}
    private var url: String = "" // Для понимания, какая композиция играет

    override val currentPosition: Flow<Position> = flow {
        while (currentCoroutineContext().isActive) {
            emit(
                Position(
                    url = url,
                    positionMillis = mediaPlayer.currentPosition.toLong()
                )
            )
            delay(100) // Шаг меньше секунды для плавности
        }
    }
        // После паузы mediaPlayer.currentPosition всегда начинается с 0,
        // поэтому ниже костыль для того, чтобы 0 был только в самом начале
        .filter { it.positionMillis != 0L }
        .onStart {
            emit(
                Position(
                    url = url,
                    positionMillis = 0L,
                )
            )
        }
        .distinctUntilChanged() // Фильтруем повторы

    override fun play(url: String, positionMillis: Long) {
        if (url == this.url) { // Лёгкий случай - снимаем с паузы
            mediaPlayer.start()
        } else { // Переключаем на другой трек
            this.url = url
            mediaPlayer.setOnCompletionListener {} // Убираем слушателя, чтобы лишний раз не сработал
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
            // Перематываем, если не сначала
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayer.seekTo(positionMillis, MediaPlayer.SEEK_CLOSEST)
            } else {
                mediaPlayer.seekTo(positionMillis.toInt())
            }
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { // Восстанавливаем слушателя
                completionListener()
            }
        }
    }

    override fun stop() {
        mediaPlayer.pause()
    }

    override fun setOnCompleteListener(action: () -> Unit) {
        completionListener = action
        mediaPlayer.setOnCompletionListener {
            action()
        }
    }

    override fun close() {
        mediaPlayer.release()
    }
}
