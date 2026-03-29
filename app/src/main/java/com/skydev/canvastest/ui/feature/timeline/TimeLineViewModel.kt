package com.skydev.canvastest.ui.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydev.canvastest.domain.model.NoteUi
import com.skydev.canvastest.domain.repo.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TimeLineViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    val notes = flow {
        val notes = noteRepository.getNotes()
        emit(notes)
    }
        .map {
            it.map {
                NoteUi(
                    id = it.id,
                    title = it.title,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    strokes = it.strokeData
                )
            }.sortedByDescending { it.updatedAt }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}