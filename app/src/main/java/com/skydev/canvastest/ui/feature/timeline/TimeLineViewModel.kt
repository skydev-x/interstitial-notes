package com.skydev.canvastest.ui.feature.timeline

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydev.canvastest.data.rag.NoteRagService
import com.skydev.canvastest.domain.model.NoteUi
import com.skydev.canvastest.domain.repo.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimeLineViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    val refreshKey = MutableStateFlow(0)

    fun onRefresh() {
        refreshKey.value++
    }




    @OptIn(ExperimentalCoroutinesApi::class)
    val notes = refreshKey.flatMapLatest {
        flow {
            val notes = noteRepository.getNotes()
            emit(notes)
        }
            .map { list ->
                list.map {
                    NoteUi(
                        id = it.id,
                        title = it.title,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        strokes = it.strokeData
                    )
                }.sortedByDescending { it.createdAt }
            }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

}