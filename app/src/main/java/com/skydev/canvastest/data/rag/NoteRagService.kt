package com.skydev.canvastest.data.rag

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object NoteRagService {

    private const val TAG = "RAG"
    private fun getModelPath(context: Context) =
        "${context.filesDir}/gemma-3n-E2B-it-int4.litertlm"

    private fun getPng(context: Context, noteId: String): ByteArray {
        try {
            val file = File(context.filesDir, "${noteId}_canvas.png")
            return file.inputStream().readBytes()
        } catch (e: Exception) {
            Log.w(TAG, "Could not save debug PNG: ${e.message}")
            return ByteArray(0)
        }
    }


    suspend fun test(id: String, context: Context): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Step 1: Creating engine...")
            val png = getPng(context, id)

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
                            topK = 1,
                            topP = 0.9,
                            temperature = 0.1
                        ),
                    )
                ).use { conversation ->

                    Log.d(TAG, "Step 4: Sending message...")

                    val response = conversation.sendMessage(
                        contents = Contents.of(
                            Content.Text(
                                """
            Analyze this handwritten note image and return ONLY a TOML response with no additional text.

            If the image is blank or unreadable, return:
```toml
            empty = true
            summary = "No content detected"
```

            Otherwise, return this exact TOML structure:
```toml
            empty = false
            summary = "One sentence overview of the entire note"

            [ocr]
            transcribed_text = "Every readable word, preserving line breaks with \\n"
            confidence = "high | medium | low"
            language = "detected language or 'unknown'"

            [layout]
            orientation = "portrait | landscape"
            structure = "e.g. bullet list, paragraphs, table, freeform"
            regions = "e.g. top-left header, center body, bottom signature"

            [[drawings]]
            type = "arrow | shape | diagram | sketch | underline | other"
            description = "What it looks like and where it appears"
            purpose = "e.g. connects two items, emphasizes text, decorative"
```

            Rules:
            - Output TOML only — no markdown, no explanation, no extra keys
            - Use [[drawings]] as a TOML array — repeat the block for each drawing
            - If no drawings exist, omit the [[drawings]] block entirely
            - Escape special characters in strings
            - Do not infer or add anything not visible in the image
            """.trimIndent()
                            ),
                            Content.ImageBytes(bytes = png)
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

}