package com.example.rygg.core.database.converters

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTags(v: List<String>): String = v.joinToString("\u001F")

    @TypeConverter
    fun toTags(v: String): List<String> = if (v.isEmpty()) emptyList() else v.split("\u001F")
}
