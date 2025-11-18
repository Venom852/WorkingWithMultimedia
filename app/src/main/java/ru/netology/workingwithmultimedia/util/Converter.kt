package ru.netology.workingwithmultimedia.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.io.File
import kotlin.jvm.java

object Converter {
    private val gson = Gson()

    @TypeConverter
    fun convertToJson(file: File?): String? = gson.toJson(file)

    @TypeConverter
    fun convertFromJson(string: String): File? = gson.fromJson(string, File::class.java)
}