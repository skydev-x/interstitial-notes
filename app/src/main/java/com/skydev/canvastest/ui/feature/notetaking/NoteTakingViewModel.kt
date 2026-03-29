package com.skydev.canvastest.ui.feature.notetaking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.skydev.canvastest.data.model.Notes
import com.skydev.canvastest.domain.loadStrokesBinary
import com.skydev.canvastest.domain.model.NoteUi
import com.skydev.canvastest.domain.model.StrokeData
import com.skydev.canvastest.domain.repo.NoteRepository
import com.skydev.canvastest.domain.saveStrokesBinary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val stack = ArrayDeque<StrokeData>()
    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()

    private val id = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val noteUi: StateFlow<NoteUi?> = id
        .flatMapLatest { noteId ->
            if (noteId == null) return@flatMapLatest flowOf(null)

            flow {
                val result = loadStrokesBinary(app, noteId)

                val note = noteRepository.getNoteById(noteId)

                if (note != null) {
                    _strokes.value = result
                    emit(
                        NoteUi(
                            id = note.id,
                            title = note.title,
                            createdAt = note.createdAt,
                            updatedAt = note.updatedAt,
                            strokes = result
                        )
                    )
                } else {
                    emit(null)
                }
            }.flowOn(Dispatchers.IO)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun load(id: String) {
        this.id.value = id
    }

    fun onStrokeComplete(stroke: StrokeData) {
        stack.clear()
        _canRedo.value = false
        _strokes.update { it + stroke }
        persist()
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

        val stroke = stack.removeLast()
        _strokes.update { it + stroke }
        _canRedo.value = stack.isNotEmpty()
        persist()
    }

    fun clear() {
        stack.clear()
        _canRedo.value = false
        _strokes.value = emptyList()
        persist()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun persist() {
        val snapshot = _strokes.value
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val existingId = id.value ?: Uuid.generateV7().toString()
            val note = Notes(
                id = existingId,
                title = "Untitled_$currentTime",
                createdAt = currentTime,
                updatedAt = currentTime,
                strokeData = snapshot
            )
            val newId = noteRepository.insertNote(note)
            id.value = newId
            saveStrokesBinary(app, newId, snapshot)
        }
    }
}