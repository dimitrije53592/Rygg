package com.example.rygg.core.database.converters

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTags(v: List<String>): String {
        return v.joinToString("\u001F")
    }

    @TypeConverter
    fun toTags(v: String): List<String> {
        return if (v.isEmpty()) emptyList() else v.split("\u001F")
    }
}