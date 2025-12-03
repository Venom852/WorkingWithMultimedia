package ru.netology.workingwithmultimedia.repository

import android.media.MediaMetadataRetriever
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.workingwithmultimedia.dao.SongDao
import ru.netology.workingwithmultimedia.dto.Album
import ru.netology.workingwithmultimedia.dto.Song
import ru.netology.workingwithmultimedia.dto.SongId
import ru.netology.workingwithmultimedia.entity.SongEntity
import ru.netology.workingwithmultimedia.entity.toDto
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.use

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val dao: SongDao,
) : SongRepository {
    companion object {
        const val BASE_URL =
            "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data"
    }
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val gson = Gson()

    override val data: Flow<List<Song>> = dao.getAll().map {
        it.toDto()
    }

    // Начальная загрузка, если в БД пусто, либо ничего не произойдёт
    override suspend fun loadSongs() {
        if (dao.isEmpty()) {
            val listFiles = withContext(Dispatchers.IO) {
                val request: Request = Request.Builder()
                    .url("${BASE_URL}/album.json")
                    .build()

                gson.fromJson(
                    client.newCall(request)
                        .execute().body.byteStream()
                        .bufferedReader(),
                    Album::class.java,
                ).tracks
            }

            dao.insert(
                coroutineScope {
                    listFiles.map { track ->
                        async {
                            MediaMetadataRetriever().use { retriever ->
                                retriever.setDataSource(track.url)
                                val durationStr =
                                    retriever.extractMetadata(
                                        MediaMetadataRetriever.METADATA_KEY_DURATION
                                    )
                                val duration = durationStr?.toLongOrNull() ?: 0L
                                val title = retriever.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_TITLE
                                ) ?: track.file

                                SongEntity.fromDto(
                                    Song(
                                        id = track.id,
                                        title = title,
                                        timeMillis = duration,
                                        url = track.url,
                                    )
                                )
                            }
                        }
                    }
                        .awaitAll()
                }
            )
        }
    }

    override suspend fun like(id: SongId) {
        dao.like(id)
    }

    override suspend fun share(id: SongId) {
        dao.share(id)
    }
}