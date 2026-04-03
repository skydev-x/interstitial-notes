package com.skydev.canvastest.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.skydev.canvastest.domain.llm.LlmService
import com.skydev.canvastest.domain.repo.NoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LlmWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val noteRepository: NoteRepository
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getString(KEY_NOTE_ID) ?: return Result.failure()

        return try {
            setForeground(createForegroundInfo(noteId))
            setProgress(workDataOf(KEY_STATUS to STATUS_RUNNING))
            val result = LlmService.process(noteId, applicationContext)
            val note = noteRepository.getNoteById(noteId) ?: return Result.failure()
            noteRepository.insertNote(note.copy(description = result))
            setProgress(workDataOf(KEY_STATUS to STATUS_DONE))
            Log.d(TAG, "RAG complete for $noteId")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "RAG failed for $noteId: ${e.message}", e)
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private fun createForegroundInfo(noteId: String): ForegroundInfo {
        createChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Processing Note")
            .setContentText("Running AI analysis...")
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setOngoing(true)
            .setSilent(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                noteId.hashCode(),
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(noteId.hashCode(), notification)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "AI Processing",
                NotificationManager.IMPORTANCE_LOW
            )
            applicationContext.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "RagWorker"
        private const val CHANNEL_ID = "rag_channel"
        private const val MAX_RETRIES = 2

        const val KEY_NOTE_ID = "note_id"
        const val KEY_STATUS = "status"
        const val STATUS_RUNNING = "running"
        const val STATUS_DONE = "done"
    }
}