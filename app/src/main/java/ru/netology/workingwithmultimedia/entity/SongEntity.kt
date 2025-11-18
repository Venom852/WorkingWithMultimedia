package ru.netology.workingwithmultimedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.workingwithmultimedia.dto.Song
import java.io.File
import kotlin.collections.map

@Entity
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val time: Double,
    val play: Boolean,
    val liked: Boolean,
    val share: Boolean,
    val beingPlayed: Boolean,
    val file: File?
) {
    fun toDto() = Song(
        id,
        title,
        time,
        play,
        liked,
        share,
        beingPlayed,
        file
    )

    companion object {
        fun fromDto(song: Song) = SongEntity(
            song.id,
            song.title,
            song.time,
            song.play,
            song.liked,
            song.share,
            song.beingPlayed,
            song.file
        )
    }
}

fun List<SongEntity>.toDto(): List<Song> = map(SongEntity::toDto)
fun List<Song>.toEntity(): List<SongEntity> = map(SongEntity::fromDto)