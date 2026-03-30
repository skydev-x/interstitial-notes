package com.skydev.canvastest.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skydev.canvastest.data.dao.NoteDao
import com.skydev.canvastest.data.model.Notes

@Database(
    entities = [Notes::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract val noteDao: NoteDao

}