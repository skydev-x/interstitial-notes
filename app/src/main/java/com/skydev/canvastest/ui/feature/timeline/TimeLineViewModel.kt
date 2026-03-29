package com.skydev.canvastest.ui.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydev.canvastest.data.model.Notes
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
            _state.value = state.value.copy(
                items = noteRepository.getNotes()
            )
        }
    }

}

data class TimeLineState(
    val items: List<Notes> = emptyList(),
)