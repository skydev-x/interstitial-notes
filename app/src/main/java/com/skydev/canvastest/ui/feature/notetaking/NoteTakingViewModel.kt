package com.skydev.canvastest.ui.feature.notetaking

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.skydev.canvastest.data.model.Notes
import com.skydev.canvastest.data.model.toUi
import com.skydev.canvastest.data.rag.NoteRagService
import com.skydev.canvastest.domain.loadStrokesBinary
import com.skydev.canvastest.domain.model.NoteUi
import com.skydev.canvastest.domain.model.StrokeData
import com.skydev.canvastest.domain.repo.NoteRepository
import com.skydev.canvastest.domain.saveStrokesBinary
import com.skydev.canvastest.ui.feature.timeline.toFormattedDate
import com.skydev.canvastest.worker.LlmWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class NoteTakingViewModel @Inject constructor(
    private val app: Application,
    private val noteRepository: NoteRepository,
) : AndroidViewModel(app) {

    private val _strokes = MutableStateFlow<List<StrokeData>>(emptyList())
    val strokes = _strokes.asStateFlow()
    private var _bitmap = MutableStateFlow<Bitmap?>(null)

    private val stack = ArrayDeque<StrokeData>()
    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()

    private val _noteUi = MutableStateFlow<NoteUi?>(null)
    val noteUi: StateFlow<NoteUi?> = _noteUi.asStateFlow()

    private var cachedNote: Notes? = null
    private var currentId: String? = null


    fun testRag(context: Context) {
        viewModelScope.launch (Dispatchers.IO){
            if (cachedNote != null) {
                val answer = currentId?.let { NoteRagService.test(it, context) }
                Log.d("RAG", "Answer: $answer")
                withContext(Dispatchers.Main){
                    cachedNote?.let { noteRepository.insertNote(it.copy(description = answer.toString())) }
                }
            }
        }
    }


    fun triggerLlmProcessing(context: Context) {
        val noteId = currentId ?: return

        val request = OneTimeWorkRequestBuilder<LlmWorker>()
            .setInputData(workDataOf(LlmWorker.KEY_NOTE_ID to noteId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "llm_$noteId",
                ExistingWorkPolicy.KEEP,
                request
            )

        viewModelScope.launch(Dispatchers.Main) {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkLiveData("rag_$noteId")
                .observeForever { infos ->
                    val info = infos?.firstOrNull() ?: return@observeForever
                    val status = info.progress.getString(LlmWorker.KEY_STATUS)
                    _ragStatus.value = status
                }
        }
    }

    private val _ragStatus = MutableStateFlow<String?>(null)
    val ragStatus: StateFlow<String?> = _ragStatus.asStateFlow()

    fun load(id: String, context: Context) {
        if (currentId == id) return
        currentId = id
        viewModelScope.launch(Dispatchers.IO) {
            val note = noteRepository.getNoteById(id) ?: return@launch
            val strokes = loadStrokesBinary(app, id)
            cachedNote = note
            _strokes.value = strokes
            _noteUi.value = note.toUi(strokes)
            triggerLlmProcessing(context)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun rename(title: String) {
        viewModelScope.launch {
            val snapshot = _strokes.value
            val existingId = cachedNote?.id ?: Uuid.generateV7().toString()
            val updated = cachedNote?.copy(
                title = title,
                updatedAt = System.currentTimeMillis(),
            ) ?: Notes(
                id = existingId,
                title = title,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                strokeData = snapshot,
            )
            val newId = noteRepository.insertNote(updated)
            cachedNote = updated.copy(id = newId)
            currentId = newId
            _noteUi.value = cachedNote!!.toUi(snapshot)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun persist() {
        val snapshot = _strokes.value
        viewModelScope.launch(Dispatchers.IO) {
            val existingId = cachedNote?.id ?: Uuid.generateV7().toString()
            currentId = existingId
            val note = cachedNote?.copy(
                strokeData = snapshot,
                updatedAt = System.currentTimeMillis(),
            ) ?: Notes(
                id = existingId,
                title = System.currentTimeMillis().toFormattedDate(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                strokeData = snapshot,
            )
            val newId = noteRepository.insertNote(note)
            cachedNote = note.copy(id = newId)
            currentId = newId
            saveStrokesBinary(app, newId, snapshot)
            withContext(Dispatchers.IO) {
                saveCanvasPng()
            }.runCatching {
                Log.d("VM", "Canvas PNG saved: fail")
            }
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            cachedNote?.let { noteRepository.deleteNote(it.id) }
            withContext(Dispatchers.Main) {
                cachedNote = null
                currentId = null
                _noteUi.value = null
                _strokes.value = emptyList()
                stack.clear()
                onDone()
            }
        }
    }

    fun onStrokeComplete(stroke: StrokeData, bitmap: Bitmap) {
        viewModelScope.launch {
            stack.clear()
            _canRedo.value = false
            _strokes.update { it + stroke }
            _bitmap.update { bitmap }
            persist()
        }
    }

    private fun scaleBitmap(src: Bitmap, maxPx: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w <= maxPx && h <= maxPx) return src
        val ratio = minOf(maxPx.toFloat() / w, maxPx.toFloat() / h)
        return src.scale((w * ratio).toInt(), (h * ratio).toInt())
    }

    private fun saveCanvasPng() {
        val bitmap = _bitmap.value ?: return
        val src = scaleBitmap(bitmap, maxPx = 768)
        val noteId = _noteUi.value?.id ?: return
        val file = File(app.filesDir, "${noteId}_canvas.png")
        file.outputStream().use { out ->
            src.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        Log.d("VM", "Canvas PNG saved: ${file.length() / 1024}KB → ${file.absolutePath}")
    }

    fun undo() {
        if (_strokes.value.isEmpty()) return
        stack.addLast(_strokes.value.last())
        _strokes.update { it.dropLast(1) }
        _canRedo.value = true
        persist()
    }

    fun redo() {
        if (stack.isEmpty()) return
        _strokes.update { it + stack.removeLast() }
        _canRedo.value = stack.isNotEmpty()
        persist()
    }

    fun clear() {
        stack.clear()
        _canRedo.value = false
        _strokes.value = emptyList()
        persist()
    }


    override fun onCleared() {
        super.onCleared()
        persist()
    }
}