package com.skydev.canvastest.ui.feature.notetaking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skydev.canvastest.domain.loadStrokesBinary
import com.skydev.canvastest.domain.model.StrokeData
import com.skydev.canvastest.domain.saveStrokesBinary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteTakingViewModel(
    private val app: Application
) : AndroidViewModel(app) {
    private val _strokes = MutableStateFlow<List<StrokeData>>(emptyList())
    val strokes = _strokes.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _strokes.value = loadStrokesBinary(app)
        }
    }

    /** Called by Canvas on every pen-up */
    fun onStrokeComplete(stroke: StrokeData) {
        _strokes.update { it + stroke }
        persist()
    }

    fun undo() {
        if (_strokes.value.isEmpty()) return
        _strokes.update { it.dropLast(1) }
        persist()
    }

    fun clear() {
        _strokes.value = emptyList()
        persist()
    }

    private fun persist() {
        val snapshot = _strokes.value
        viewModelScope.launch(Dispatchers.IO) {
            saveStrokesBinary(getApplication(), snapshot)
        }
    }

}