package ru.netology.workingwithmultimedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.workingwithmultimedia.entity.SongEntity

@Dao
interface SongDao {
    @Query("SELECT * FROM SongEntity ORDER BY id DESC")
    fun getAll(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) == 0 FROM SongEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(songs: List<SongEntity>)

    @Query(
        """
            UPDATE SongEntity SET
                share = CASE WHEN share THEN 0 ELSE 1 END
            WHERE id = :id;
        """
    )
    suspend fun share(id: Long)

    @Query(
        """
            UPDATE SongEntity SET
                liked = CASE WHEN liked THEN 0 ELSE 1 END
            WHERE id = :id;
        """
    )
    suspend fun like(id: Long)
}