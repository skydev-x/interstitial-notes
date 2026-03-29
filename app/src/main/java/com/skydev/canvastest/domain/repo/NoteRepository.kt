package com.skydev.canvastest.domain.repo

import com.skydev.canvastest.data.model.Notes

interface NoteRepository {

    suspend fun insertNote(note: Notes)

    suspend fun getNotes(): List<Notes>

    suspend fun getNoteById(id: String): Notes?

}