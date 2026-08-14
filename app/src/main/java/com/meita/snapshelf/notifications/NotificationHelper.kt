package com.meita.snapshelf.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.meita.snapshelf.R
import com.meita.snapshelf.worker.ReminderWorker
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {
    fun scheduleReminder(screenshotId: Long, title: String, atMillis: Long) {
        val delay = atMillis - System.currentTimeMillis()
        if (delay <= 0) return
        val data = Data.Builder()
            .putLong(ReminderWorker.KEY_SCREENSHOT_ID, screenshotId)
            .putString(ReminderWorker.KEY_TITLE, title)
            .build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun showReminder(screenshotId: Long, title: String) {
        ensureChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("スクショの期限候補")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText("保存したスクリーンショットに期限候補があります。"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(screenshotId.toInt(), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "SnapPurge reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "snapshelf_reminders"
    }
}
