package com.skydev.canvastest.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PointF(val x: Float, val y: Float)

@Serializable
data class StrokeData(
    val points: List<PointF>,
    val color: Long = 0xFFFFFFFF,
    val width: Float = 5f
)

@Serializable
data class DrawingData(val strokes: List<StrokeData>)

fun StrokeData.toPath(): androidx.compose.ui.graphics.Path {
    val path = androidx.compose.ui.graphics.Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        path.lineTo(points[i].x, points[i].y)
    }
    return path
}