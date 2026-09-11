package ru.netology.workingwithmultimedia.dto

typealias SongId = Long

data class Song(
    val id: SongId = 0,
    val title: String,
    val timeMillis: Long,
    val currentPositionMillis: Long = 0,
    val liked: Boolean = false,
    val share: Boolean = false,
    val play: Boolean = false,
    val url: String,
) {
    val currentPositionFormatted = formatMillisToMmSs(
        currentPositionMillis,
    )

    private fun formatMillisToMmSs(millis: Long): String {
        if (millis < 0) return "00:00"

        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}