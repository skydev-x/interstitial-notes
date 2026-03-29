package com.skydev.canvastest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.skydev.canvastest.data.model.Notes

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Notes)

    @Query("SELECT * FROM notes")
    suspend fun getNotes(): List<Notes>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Notes?

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    suspend fun doesExist(id: String): Boolean {
        return getNoteById(id) != null
    }

}