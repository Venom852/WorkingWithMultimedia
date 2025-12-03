package ru.netology.workingwithmultimedia.dto

import ru.netology.workingwithmultimedia.repository.SongRepositoryImpl

data class Album(
    val id: Long,
    val title: String,
    val subtitle: String,
    val artist: String,
    val published: String,
    val genre: String,
    val tracks: List<Tracks>
)

data class Tracks(
    val id: Long,
    val file: String
) {
    val url: String
        get() = "${SongRepositoryImpl.BASE_URL}/$file"
}