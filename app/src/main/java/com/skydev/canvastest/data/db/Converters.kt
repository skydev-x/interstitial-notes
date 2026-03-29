package com.skydev.canvastest.data.db

import androidx.room.TypeConverter
import com.skydev.canvastest.domain.model.StrokeData
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromStrokeData(data: StrokeData): String {
        return json.encodeToString(StrokeData.serializer(), data)
    }

    @TypeConverter
    fun toStrokeData(data: String): StrokeData {
        return json.decodeFromString(StrokeData.serializer(), data)
    }

    @TypeConverter
    fun fromStrokeList(data: List<StrokeData>?): String? {
        return data?.let {
            json.encodeToString(ListSerializer(StrokeData.serializer()), it)
        }
    }

    @TypeConverter
    fun toStrokeList(data: String?): List<StrokeData>? {
        return data?.let {
            json.decodeFromString(ListSerializer(StrokeData.serializer()), it)
        }
    }
}