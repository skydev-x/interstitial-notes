package com.skydev.canvastest.data.repo

import com.skydev.canvastest.data.dao.NoteDao
import com.skydev.canvastest.data.model.Notes
import com.skydev.canvastest.domain.repo.NoteRepository
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {
    override suspend fun insertNote(note: Notes) {
        noteDao.insertNote(note)
    }

    override suspend fun getNotes(): List<Notes> {
        return noteDao.getNotes()
    }

    override suspend fun getNoteById(id: String): Notes? {
        return noteDao.getNoteById(id)
    }

}