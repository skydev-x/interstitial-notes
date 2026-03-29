package com.skydev.canvastest.ui.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydev.canvastest.data.model.Notes
import com.skydev.canvastest.domain.model.NoteUi
import com.skydev.canvastest.domain.repo.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeLineViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TimeLineState())
    val state = _state.asStateFlow()

    fun loadNotes() {
        viewModelScope.launch {
            val notes = noteRepository.getNotes()
            _state.value = state.value.copy(
                items = notes.map {
                    NoteUi(
                        id = it.id,
                        title = it.title,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        strokes = it.strokeData
                    )
                }
            )
        }
    }

}

data class TimeLineState(
    val items: List<NoteUi> = emptyList(),
)