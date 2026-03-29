package com.skydev.canvastest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skydev.canvastest.domain.model.StrokeData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity
data class Notes @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey val id: String = Uuid.generateV7().toString(),
    val title: String,
    val strokeData: List<StrokeData>,
    val createdAt: Long,
    val updatedAt: Long,
)