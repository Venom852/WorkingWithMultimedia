package ru.netology.workingwithmultimedia.repository

import ru.netology.workingwithmultimedia.dto.Tracks
import java.io.File

interface SongRepository {
    suspend fun saveSong(track: List<Tracks>): List<File>
}