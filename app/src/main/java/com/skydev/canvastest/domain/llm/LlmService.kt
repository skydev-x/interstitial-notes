package com.skydev.canvastest.domain.llm


import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LlmService {

    private const val TAG = "RAG"

    private fun getPng(context: Context, noteId: String): ByteArray {
        return try {
            File(context.filesDir, "${noteId}_canvas.png").readBytes()
        } catch (e: Exception) {
            Log.w(TAG, "PNG not found for $noteId: ${e.message}")
            ByteArray(0)
        }
    }

    suspend fun process(id: String, context: Context): String = withContext(Dispatchers.IO) {
        val png = getPng(context, id)
        val engine = LlmEngineHolder.get(context)

        engine.createConversation(
            ConversationConfig(
                systemInstruction = Contents.of(
                    "You are an OCR and diagram analysis assistant. " +
                            "Accurately read and transcribe handwritten text and describe drawn elements. " +
                            "Do NOT invent content. If nothing is visible, say so."
                ),
                samplerConfig = SamplerConfig(topK = 1, topP = 0.9, temperature = 0.1)
            )
        ).use { conversation ->
            val response = conversation.sendMessage(
                contents = Contents.of(
                    Content.Text(RAG_PROMPT.trimIndent()),
                    Content.ImageBytes(bytes = png)
                )
            )
            response.contents.contents.joinToString("") { it.toString() }.trim()
        }
    }

    private const val RAG_PROMPT = """
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
        - Do not infer or add anything not visible in the image
    """
}