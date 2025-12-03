package ru.netology.workingwithmultimedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.workingwithmultimedia.dto.Song
import ru.netology.workingwithmultimedia.dto.SongId

// Вся логика по скачиванию и хранению треков здесь
interface SongRepository {
    val data: Flow<List<Song>>
    suspend fun loadSongs()
    suspend fun like(id: SongId)
    suspend fun share(id: SongId)
}