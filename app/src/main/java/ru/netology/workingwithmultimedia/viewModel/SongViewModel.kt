package ru.netology.workingwithmultimedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.netology.workingwithmultimedia.dto.Song
import ru.netology.workingwithmultimedia.dto.SongId
import ru.netology.workingwithmultimedia.repository.SongRepository
import ru.netology.workingwithmultimedia.util.MediaService
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val repository: SongRepository,
    private val mediaService: MediaService,
    application: Application
) : AndroidViewModel(application) {
    // Текущая композиция, которая сейчас играет. Храним в оперативной памяти здесь
    private val playingSongId = MutableStateFlow<SongId?>(null)

    // Позиции всех треков по url. Храним тоже в оперативной памяти
    private val positions =
        // Через scan составляем словарь из позиций всех треков на основе текущей currentPosition
        mediaService.currentPosition.scan(mapOf<String, Long>()) { accumulator, newPosition ->
            accumulator + (newPosition.url to newPosition.positionMillis)
        }
            // Чтобы можно было текущее значение прочитать, делаем StateFlow
            .stateIn(viewModelScope, SharingStarted.Lazily, mapOf())

    // Создаём подписку на список песен из 3 источников
    val data: Flow<List<Song>> = combine(
        repository.data, // Данные из БД
        playingSongId, // Текущий трек
        positions, // Позиции треков по url
    ) { allSongs, playingSongId, positions ->
        allSongs.map { song ->
            song.copy(
                play = song.id == playingSongId,
                currentPositionMillis = positions[song.url] ?: 0L
            )
        }
    }

    init {
        loadSongs()
        initPlayer()
    }

    // Переключает трек на другой или ставит на паузу, если вызвать повторно с тем же id
    fun play(id: Long) {
        val value = playingSongId.value
        val isPlaying = value == id

        if (isPlaying) {
            playingSongId.value = null
        } else {
            playingSongId.value = id
        }
    }

    // Нажатие на большую кнопку. Здесь логика простая максимально.
    // Если что-то играет, останавливаем музыку, а если не играет, то начинаем с первой
    fun playBig() {
        viewModelScope.launch {
            if (playingSongId.value == null) {
                playFirstSong()
            } else {
                playingSongId.value = null
            }
        }
    }

    private suspend fun playFirstSong() {
        data.first().firstOrNull()?.id?.let(::play)
    }

    fun like(id: Long) {
        viewModelScope.launch {
            repository.like(id)
        }
    }

    fun share(id: Long) {
        viewModelScope.launch {
            repository.share(id)
        }
    }

    fun loadSongs() { // В идеале следует добавить обработку ошибок
        viewModelScope.launch {
            repository.loadSongs()

            playFirstSong()

            mediaService.setOnCompleteListener { playNext() }
        }
    }

    private fun initPlayer() {
        // Наблюдаем за выбранным треком по id
        playingSongId.onEach { playingSongId ->
            if (playingSongId == null) {
                mediaService.stop()
            } else {
                // Нужны данные для перемотки, если трек уже проигрывался
                val toPlay = data.first().first { it.id == playingSongId }
                mediaService.play(
                    url = toPlay.url,
                    positionMillis = toPlay.currentPositionMillis,
                )
            }
        }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        addCloseable(mediaService) // Чтобы не тратить ресурсы при закрытии экрана
    }

    // Берём следующую песню и отправляем её в плеер
    // Если трек был последний, берём первый
    private fun playNext() {
        viewModelScope.launch {
            val currentSongId = playingSongId.value
            val songs = data.first()
            val nextIndex = songs.indexOfFirst { song -> song.id == currentSongId } + 1
            play(songs.getOrElse(nextIndex) { songs.first() }.id)
        }
    }
}