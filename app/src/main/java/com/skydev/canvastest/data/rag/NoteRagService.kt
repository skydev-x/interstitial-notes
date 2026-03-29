package com.skydev.canvastest.data.rag

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object NoteRagService {

    private const val MODEL_PATH = "/sdcard/llm/model.litertlm"
    private const val TAG = "RAG"
    private fun getModelPath(context: Context) =
        "${context.filesDir}/gemma-3n-E2B-it-int4.litertlm"
    suspend fun test(context: Context): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Step 1: Creating engine...")

            Engine(EngineConfig(modelPath = getModelPath(context), backend = Backend.CPU())).use { engine ->

                Log.d(TAG, "Step 2: Initializing (may take ~10s)...")
                engine.initialize()

                Log.d(TAG, "Step 3: Engine ready. Creating conversation...")

                engine.createConversation().use { conversation ->

                    Log.d(TAG, "Step 4: Sending message...")

                    // Simplest possible — synchronous, no streaming
                    val response = conversation.sendMessage("What is binary search")

                    Log.d(TAG, "Step 5: Got response: $response")
                    response.contents.contents.toString()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "FAILED: ${e.message}", e)
            "Error: ${e.message}"
        } finally {
        }
    }
}