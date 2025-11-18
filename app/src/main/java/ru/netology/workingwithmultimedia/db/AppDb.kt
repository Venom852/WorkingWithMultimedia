package ru.netology.workingwithmultimedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.workingwithmultimedia.dao.SongDao
import ru.netology.workingwithmultimedia.entity.SongEntity
import ru.netology.workingwithmultimedia.util.Converter

@Database(entities = [SongEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class AppDb : RoomDatabase() {
    abstract val postDao: SongDao
}