package com.skydev.canvastest.ui.feature.notetaking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skydev.canvastest.data.model.Notes
import com.skydev.canvastest.domain.loadStrokesBinary
import com.skydev.canvastest.domain.model.StrokeData
import com.skydev.canvastest.domain.repo.NoteRepository
import com.skydev.canvastest.domain.saveStrokesBinary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteTakingViewModel @Inject constructor(
    private val app: Application,
    private val noteRepository: NoteRepository
) : AndroidViewModel(app) {
    private val _strokes = MutableStateFlow<List<StrokeData>>(emptyList())
    private val stack = ArrayDeque<StrokeData>(emptyList())
    private val _canRedo = MutableStateFlow(false)
    val canRedo = _canRedo.asStateFlow()

    val strokes = _strokes.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _strokes.value = loadStrokesBinary(app)
        }
    }

    fun onStrokeComplete(stroke: StrokeData) {
        stack.clear()
        _canRedo.value = false
        _strokes.update { it + stroke }
        persist()
    }

    fun undo() {
        viewModelScope.launch {
            if (_strokes.value.isEmpty()) return@launch
            stack.addLast(_strokes.value.last())
            _canRedo.value = true
            _strokes.update { it.dropLast(1) }
            persist()
        }
    }

    fun redo() {
        viewModelScope.launch {
            if (stack.isEmpty()) return@launch
            _strokes.update { it + stack.last() }
            stack.removeLast()
            _canRedo.value = stack.isNotEmpty()
            persist()
        }
    }

    fun clear() {
        stack.clear()
        _canRedo.value = false
        _strokes.value = emptyList()
        persist()
    }

    fun persist() {
        val snapshot = _strokes.value
        viewModelScope.launch(Dispatchers.IO) {
            saveStrokesBinary(getApplication(), snapshot)
            noteRepository.insertNote(Notes(
                title = "Untitled",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                strokeData = snapshot
            ))
        }
    }

}