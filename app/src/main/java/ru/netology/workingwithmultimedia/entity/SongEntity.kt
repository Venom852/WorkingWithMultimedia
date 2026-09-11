package ru.netology.workingwithmultimedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.workingwithmultimedia.dto.Song
import kotlin.collections.map

// Здесь нет смысла хранить признак play: Boolean т.к. он связан напрямую с плеером, а плеер хранится в оперативной памяти
@Entity
data class SongEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val timeMillis: Long,
    val liked: Boolean,
    val share: Boolean,
    val url: String, // Убрал хранение файлов, чтобы не усложнять. Плеер умеет сразу по url играть музыку
) {
    fun toDto() = Song(
        id = id,
        title = title,
        timeMillis = timeMillis,
        liked = liked,
        share = share,
        url = url,
    )

    companion object {
        fun fromDto(song: Song) = SongEntity(
            id = song.id,
            title = song.title,
            timeMillis = song.timeMillis,
            liked = song.liked,
            share = song.share,
            url = song.url,
        )
    }
}

fun List<SongEntity>.toDto(): List<Song> = map(SongEntity::toDto)