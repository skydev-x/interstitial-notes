package com.skydev.canvastest.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class NoteUi(
    val id: String,
    val title: String,
    val transcribedText: String = "",
    val summary: String = "",
    val isEmpty: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val strokes: List<StrokeData>
)
