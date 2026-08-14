package com.meita.snapshelf.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meita.snapshelf.notifications.NotificationHelper

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_SCREENSHOT_ID, -1)
        val title = inputData.getString(KEY_TITLE) ?: "SnapPurge"
        if (id <= 0) return Result.success()
        NotificationHelper(applicationContext).showReminder(id, title)
        return Result.success()
    }

    companion object {
        const val KEY_SCREENSHOT_ID = "screenshot_id"
        const val KEY_TITLE = "title"
    }
}
