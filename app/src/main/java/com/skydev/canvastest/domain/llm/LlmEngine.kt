package com.skydev.canvastest.domain.llm

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig

object LlmEngineHolder {
    @Volatile private var engine: Engine? = null

    fun get(context: Context): Engine {
        return engine ?: synchronized(this) {
            engine ?: Engine(
                EngineConfig(
                    modelPath = "${context.filesDir}/gemma-3n-E2B-it-int4.litertlm",
                    backend = Backend.CPU(),
                    visionBackend = Backend.GPU()
                )
            ).apply { initialize() }.also { engine = it }
        }
    }

    fun release() {
        engine?.close()
        engine = null
    }
}