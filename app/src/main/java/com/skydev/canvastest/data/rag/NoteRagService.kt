package com.skydev.canvastest.data.rag

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import androidx.core.graphics.createBitmap
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import com.skydev.canvastest.domain.loadStrokesBinary
import com.skydev.canvastest.domain.model.StrokeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

object NoteRagService {

    private const val TAG = "RAG"
    private fun getModelPath(context: Context) =
        "${context.filesDir}/gemma-3n-E2B-it-int4.litertlm"

    private fun saveDebugPng(context: Context, bytes: ByteArray, noteId: String) {
        try {
            val file = File(context.filesDir, "${noteId}_debug.png")
            file.writeBytes(bytes)
            Log.d(TAG, "DEBUG PNG saved → ${file.absolutePath}")
            // adb pull /data/data/com.skydev.canvastest/files/<noteId>_debug.png ~/Desktop/
        } catch (e: Exception) {
            Log.w(TAG, "Could not save debug PNG: ${e.message}")
        }
    }
    // ── Generate a test PNG programmatically — no strokes needed ─────────────────
    private fun createTestBitmap(): ByteArray {
        val bitmap = createBitmap(512, 512)
        val canvas = Canvas(bitmap)

        // White background
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.BLACK
        }

        // Draw "Hello AI" text
        paint.textSize = 80f
        paint.style = Paint.Style.FILL
        canvas.drawText("Hello AI", 80f, 150f, paint)

        // Draw a circle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        canvas.drawCircle(256f, 340f, 100f, paint)

        // Draw an arrow
        paint.strokeWidth = 6f
        canvas.drawLine(100f, 420f, 300f, 420f, paint)
        canvas.drawLine(280f, 400f, 300f, 420f, paint)
        canvas.drawLine(280f, 440f, 300f, 420f, paint)

        // Draw "123" number
        paint.style = Paint.Style.FILL
        paint.textSize = 60f
        canvas.drawText("123", 350f, 150f, paint)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    // ── Test function using hardcoded bitmap ──────────────────────────────────────
    suspend fun testWithHardcodedImage(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val imageBytes = createTestBitmap()

            // Save it so you can visually verify what model receives
            File(context.filesDir, "hardcoded_test.png").writeBytes(imageBytes)
            Log.d(TAG, "Test PNG saved → pull with:")
            Log.d(TAG, "adb exec-out run-as com.skydev.canvastest cat files/hardcoded_test.png > ~/Desktop/test.png")

            Engine(
                EngineConfig(
                    modelPath = getModelPath(context),
                    backend = Backend.CPU(),
                    visionBackend = Backend.GPU()
                )
            ).use { engine ->

                engine.initialize()

                engine.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of("You are an OCR assistant."),
                        samplerConfig = SamplerConfig(topK = 1, topP = 0.9, temperature = 0.1),
                    )
                ).use { conversation ->

                    val response = conversation.sendMessage(
                        contents = Contents.of(
                            Content.Text("What text and shapes do you see in this image? List everything."),
                            Content.ImageBytes(bytes = imageBytes)
                        )
                    )

                    val result = response.contents.contents
                        .joinToString("") { it.toString() }
                        .trim()

                    Log.d(TAG, "Hardcoded test response: $result")
                    result
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "FAILED: ${e.message}", e)
            "Error: ${e.message}"
        }
    }

    suspend fun test(id: String, context: Context): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Step 1: Creating engine...")

            val strokes = loadStrokesBinary(context, id)
            Log.d(TAG, "Loaded ${strokes.size} strokes")

            val bitmap = strokesToBitmap(strokes)
            val imageBytes = bitmapToBytes(bitmap)

            // ✅ Save debug PNG — pull with adb to verify what model sees
            saveDebugPng(context, imageBytes, id)
            Log.d(TAG, "Image size: ${imageBytes.size / 1024}KB, ${bitmap.width}x${bitmap.height}")

            Engine(
                EngineConfig(
                    modelPath = getModelPath(context),
                    backend = Backend.CPU(),
                    visionBackend = Backend.GPU()
                )
            ).use { engine ->

                Log.d(TAG, "Step 2: Initializing...")
                engine.initialize()

                Log.d(TAG, "Step 3: Engine ready. Creating conversation...")
                engine.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of(
                            // Role: pure OCR/extraction, no hallucination
                            "You are an OCR and diagram analysis assistant. " +
                                    "Your job is to accurately read and transcribe handwritten text " +
                                    "and describe drawn elements exactly as they appear. " +
                                    "Do NOT invent content. If nothing is visible, say so."
                        ),
                        samplerConfig = SamplerConfig(
                            topK = 1,           // greedy — best for OCR, no creativity
                            topP = 0.9,
                            temperature = 0.1  // near-deterministic for text extraction
                        ),
                    )
                ).use { conversation ->

                    Log.d(TAG, "Step 4: Sending message...")

                    val response = conversation.sendMessage(
                        contents = Contents.of(
                            Content.Text(
                                // Structured OCR prompt — forces separation of text vs drawing
                                """
                                Look at this handwritten note image carefully.
                                
                                1. TRANSCRIBED TEXT: Write out every word or character you can read, exactly as written.
                                2. DRAWINGS/DIAGRAMS: Describe any non-text elements (shapes, arrows, diagrams).
                                3. LAYOUT: Briefly describe how the content is arranged.
                                
                                If the image appears blank or empty, say "No content detected".
                                Do not add any information not visible in the image.
                                """.trimIndent()
                            ),
                            Content.ImageBytes(bytes = imageBytes)
                        )
                    )

                    val result = response.contents.contents
                        .joinToString("") { it.toString() }
                        .trim()

                    Log.d(TAG, "Step 5: Got response: $result")
                    result
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "FAILED: ${e.message}", e)
            "Error: ${e.message}"
        }
    }
    fun strokesToBitmap(
        strokes: List<StrokeData>,
        width: Int = 1024,
        height: Int = 1024
    ): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        strokes.forEach { stroke ->
            paint.color = stroke.color.toInt()
            paint.strokeWidth = stroke.width

            val path = Path()
            val points = stroke.points

            if (points.isNotEmpty()) {
                path.moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    val p = points[i]
                    path.lineTo(p.x, p.y)
                }
            }

            canvas.drawPath(path, paint)
        }

        return bitmap
    }
    fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}