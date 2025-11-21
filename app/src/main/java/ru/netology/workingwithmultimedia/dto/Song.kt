package ru.netology.workingwithmultimedia.dto

import java.io.File

data class Song (
    val id: Long,
    val title: String,
    val time: Double,
    val play: Boolean,
    val liked: Boolean,
    val share: Boolean,
    val beingPlayed: Boolean,
    val file: File?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song

        if (id != other.id) return false
        if (time != other.time) return false
        if (play != other.play) return false
        if (liked != other.liked) return false
        if (share != other.share) return false
        if (beingPlayed != other.beingPlayed) return false
        if (title != other.title) return false
        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + play.hashCode()
        result = 31 * result + liked.hashCode()
        result = 31 * result + share.hashCode()
        result = 31 * result + beingPlayed.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (file?.hashCode() ?: 0)
        return result
    }
}