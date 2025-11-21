package ru.netology.workingwithmultimedia.viewModel

import android.app.Application
import android.media.MediaMetadataRetriever
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.workingwithmultimedia.dao.SongDao
import ru.netology.workingwithmultimedia.dto.Album
import ru.netology.workingwithmultimedia.dto.Song
import ru.netology.workingwithmultimedia.entity.SongEntity
import ru.netology.workingwithmultimedia.entity.toDto
import ru.netology.workingwithmultimedia.lifecycle.MediaLifecycleObserver
import ru.netology.workingwithmultimedia.repository.SongRepository
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import ru.netology.workingwithmultimedia.activity.AppActivity.Companion.isPaused
import ru.netology.workingwithmultimedia.activity.AppActivity.Companion.checked

@HiltViewModel
class SongViewModel @Inject constructor(
    private val dao: SongDao,
    private val repository: SongRepository,
    application: Application
) : AndroidViewModel(application) {
    private var song = Song(
        id = 0,
        title = "",
        time = 0.0,
        play = false,
        liked = false,
        share = false,
        beingPlayed = false,
        file = null
    )
    private val gson = Gson()
    private val mediaObserver = MediaLifecycleObserver()
    val data: Flow<List<Song>> = dao.getAll().map { it.toDto() }
    private val albumData = application.assets.open("albumData.json")
        .bufferedReader()
        .use {
            it.readText()
        }
    private val tracks = gson.fromJson(albumData, Album::class.java).tracks

    fun play(id: Long) {
        viewModelScope.launch {
            dao.play(id)
        }
    }

    fun like(id: Long) {
        viewModelScope.launch {
            dao.like(id)
        }
    }

    fun share(id: Long) {
        viewModelScope.launch {
            dao.share(id)
        }
    }

    fun saveSongs() {
        viewModelScope.launch {
            val listFiles = repository.saveSong(tracks)

            val job = launch {
                listFiles.forEach {
                    launch {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(it.absolutePath)
                        val durationStr =
                            retriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_DURATION
                            )
                        val duration = durationStr?.toIntOrNull() ?: 0
                        val title = retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_TITLE
                        ) ?: "noName"
                        retriever.release()

                        song = song.copy(
                            title = title,
                            time = duration.toDuration(DurationUnit.MILLISECONDS)
                                .toDouble(DurationUnit.MINUTES),
                            file = it
                        )
                        dao.saveSong(SongEntity.fromDto(song))
                    }
                }
            }

            job.join()
            playSong()
        }
    }

    fun playSong() {
        viewModelScope.launch {
            var listSong = emptyList<Song>()
            val job = CoroutineScope(Dispatchers.IO).launch {
                listSong = dao.getSongs().toDto()
            }
            job.join()
            if (checked && isPaused) {
                isPaused = false
                listSong.forEach {
                    if (it.beingPlayed) {
                        play(it.id)

                        mediaObserver.apply {
                            mediaPlayer?.setDataSource(
                                it.file?.absolutePath
                            )
                        }.play()

                        song = it.copy(beingPlayed = false)
                        dao.saveSong(SongEntity.fromDto(song))
                    } else {
                        song = it.copy(beingPlayed = true)

                        saveSong(song)
                        play(song.id)

                        mediaObserver.apply {
                            mediaPlayer?.setDataSource(
                                it.file?.absolutePath
                            )
                        }.play()

                        song = it.copy(beingPlayed = false)
                        dao.saveSong(SongEntity.fromDto(song))
                    }
                }
            }

            if (checked && !isPaused) {
                listSong.forEach {
                    song = it.copy(beingPlayed = true)

                    saveSong(song)
                    play(song.id)

                    mediaObserver.apply {
                        mediaPlayer?.setDataSource(
                            it.file?.absolutePath
                        )
                    }.play()

                    song = it.copy(beingPlayed = false)
                    dao.saveSong(SongEntity.fromDto(song))
                }
            }
        }
    }

    fun pauseSong() {
        viewModelScope.launch {
            mediaObserver.pause()
        }
    }

    suspend fun isEmpty(): Boolean {
        var isEmpty = false
        val job = viewModelScope.launch {
            isEmpty = dao.isEmpty()
        }

        job.join()
        return isEmpty
    }

    suspend fun getSongs(): List<Song> {
        var listSongs = emptyList<Song>()
        val job = viewModelScope.launch {
            CoroutineScope(Dispatchers.IO).launch {
                listSongs = dao.getSongs().toDto()
            }
        }

        job.join()
        return listSongs
    }

//    suspend fun getSongs(): List<Song> = withContext(Dispatchers.IO) {
//        var listSongs: List<Song>
//            async {
//                listSongs = dao.getSongs().toDto()
//                listSongs
//            }
//                .await()
//
//    }

    fun saveSong(song: Song) {
        viewModelScope.launch {
                dao.saveSong(SongEntity.fromDto(song))
        }
    }
}