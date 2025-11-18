package ru.netology.workingwithmultimedia.viewModel

import android.app.Application
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
import java.io.File
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@HiltViewModel
class SongViewModel @Inject constructor(
    private val dao: SongDao,
    private val repository: SongRepository,
    application: Application
) : AndroidViewModel(application) {
    private val empty = Song(
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
    private var listFiles = emptyList<File>()
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
            CoroutineScope(Dispatchers.Default).launch {
                listFiles = repository.saveSong(tracks)
            }

            var duration: Int

            listFiles.forEach {
                mediaObserver.apply {
                    mediaPlayer?.setDataSource(
                        it.path
                    )
                    mediaPlayer?.prepareAsync()
                    duration = mediaPlayer?.duration ?: 0
                    mediaPlayer?.release()
                }

                val song = empty.copy(
                    title = it.name,
                    time = duration.toDuration(DurationUnit.MINUTES)
                        .toDouble(DurationUnit.MINUTES),
                    file = it
                )
                dao.saveSong(SongEntity.fromDto(song))
            }
        }
    }

    fun playSong(id: Long) {
        viewModelScope.launch {
            var song = empty
            CoroutineScope(Dispatchers.Default).launch {
                song = dao.getSong(id).toDto()
            }
            mediaObserver.apply {
                mediaPlayer?.setDataSource(
                    song.file?.path
                )
            }.play()
            song = song.copy(beingPlayed = false)
            dao.saveSong(SongEntity.fromDto(song))
        }
    }

    fun pauseSong() {
        viewModelScope.launch {
            mediaObserver.pause()
        }
    }
}