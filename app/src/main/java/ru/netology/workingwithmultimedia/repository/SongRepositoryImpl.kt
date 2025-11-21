package ru.netology.workingwithmultimedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.workingwithmultimedia.dto.Tracks
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor() : SongRepository {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    companion object {
        const val BASE_URL =
            "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data"
    }

    override suspend fun saveSong(track: List<Tracks>): List<File> = withContext(Dispatchers.IO) {
        track.map {
            async {
                val file = it.file
                val request: Request = Request.Builder()
                    .url("${BASE_URL}/${file}")
                    .build()
                val body = client.newCall(request)
                    .execute().body.bytes()
                val fileSong = File.createTempFile("files", "index")
                fileSong.writeBytes(body)

                fileSong
            }
        }
            .awaitAll()
    }
}