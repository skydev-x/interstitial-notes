package com.skydev.canvastest.domain

import com.skydev.canvastest.domain.model.PointF
import com.skydev.canvastest.domain.model.StrokeData
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/*
 * Binary format per file:
 *
 *  [INT]   stroke count
 *  per stroke:
 *    [LONG]  color (ARGB)
 *    [FLOAT] width
 *    [INT]   point count
 *    per point:
 *      [FLOAT] x
 *      [FLOAT] y
 */

private const val BIN_FILE = "drawing.bin"

private const val INT_BYTES   = 4
private const val FLOAT_BYTES = 4
private const val LONG_BYTES  = 8

fun encodeStrokes(strokes: List<StrokeData>): ByteArray {
    // Pre-calculate exact buffer size — zero reallocations
    val size = INT_BYTES + strokes.sumOf {
        LONG_BYTES + FLOAT_BYTES + INT_BYTES + it.points.size * 2 * FLOAT_BYTES
    }
    val buf = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
    buf.putInt(strokes.size)
    strokes.forEach { stroke ->
        buf.putLong(stroke.color)
        buf.putFloat(stroke.width)
        buf.putInt(stroke.points.size)
        stroke.points.forEach { p ->
            buf.putFloat(p.x)
            buf.putFloat(p.y)
        }
    }
    return buf.array()
}

fun decodeStrokes(bytes: ByteArray): List<StrokeData> {
    val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val strokeCount = buf.int
    return List(strokeCount) {
        val color      = buf.long
        val width      = buf.float
        val pointCount = buf.int
        val points     = List(pointCount) { PointF(buf.float, buf.float) }
        StrokeData(points, color, width)
    }
}

suspend fun saveStrokesBinary(context: Context,id : String ,strokes: List<StrokeData>) =
    withContext(Dispatchers.IO) {
        File(context.filesDir, "${id}_$BIN_FILE").writeBytes(encodeStrokes(strokes))
    }

suspend fun loadStrokesBinary(context: Context,id : String): List<StrokeData> =
    withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "${id}_$BIN_FILE")
        if (!file.exists()) return@withContext emptyList()
        try { decodeStrokes(file.readBytes()).also {
            Log.d("loadStrokesBinary", "Loaded ${it} bytes")
        } } catch (e: Exception) { emptyList() }
    }