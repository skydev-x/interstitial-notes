package com.skydev.canvastest.data.objectbox

import android.content.Context
import io.objectbox.BoxStore
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class NoteObject(
    @Id var id: Long = 0,
    var name: String? = null,
    var data: String? = null,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)
object ObjectBox {
    lateinit var store: BoxStore
        private set

    fun init(context: Context) {
        store = MyObjectBox.builder()
                .androidContext(context)
                .build()
    }
}