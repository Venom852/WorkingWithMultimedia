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
)