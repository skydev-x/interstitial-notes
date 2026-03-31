package com.skydev.canvastest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skydev.canvastest.domain.model.NoteUi
import com.skydev.canvastest.domain.model.StrokeData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity
data class Notes @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey val id: String = Uuid.generateV7().toString(),
    val title: String,
    val strokeData: List<StrokeData>,
    val description: String = "",
    val createdAt: Long,
    val updatedAt: Long,
)
fun Notes.toUi(strokes: List<StrokeData> = emptyList()) = NoteUi(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    strokes = strokes.ifEmpty { strokeData },
    transcribedText = parseTomlDescription(description).first,
    summary = parseTomlDescription(description).second,
    isEmpty = parseTomlDescription(description).third
)
fun parseTomlDescription(raw: String): Triple<String, String, Boolean> {
    val cleaned = raw
        .removePrefix("```toml").removePrefix("```")
        .removeSuffix("```")
        .trim()

    fun extractString(key: String): String =
        Regex("""$key\s*=\s*"([^"]*)"""")   // ← closing " is escaped, no stray )
            .find(cleaned)?.groupValues?.get(1)?.trim() ?: ""

    val text    = extractString("transcribed_text")
    val summary = extractString("summary")
    val empty   = Regex("""empty\s*=\s*(true|false)""")
        .find(cleaned)?.groupValues?.get(1) != "false"

    return Triple(text, summary, empty)
}