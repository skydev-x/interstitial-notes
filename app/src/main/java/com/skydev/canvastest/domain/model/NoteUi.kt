package com.skydev.canvastest.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class NoteUi(
    val id: String,
    val title: String,
    val description: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val strokes: List<StrokeData>
)
