package com.meita.snapshelf.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meita.snapshelf.data.local.SnapShelfDatabase
import com.meita.snapshelf.data.repository.ScreenshotRepository
import com.meita.snapshelf.media.ScreenshotScanner
import com.meita.snapshelf.notifications.NotificationHelper
import com.meita.snapshelf.ocr.OcrProcessor

class IndexScreenshotsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val db = Room.databaseBuilder(
                applicationContext,
                SnapShelfDatabase::class.java,
                "snapshelf.db"
            ).fallbackToDestructiveMigration().build()
            val repository = ScreenshotRepository(
                dao = db.screenshotDao(),
                context = applicationContext,
                notificationHelper = NotificationHelper(applicationContext)
            )
            val scanner = ScreenshotScanner(
                context = applicationContext,
                repository = repository,
                ocrProcessor = OcrProcessor(applicationContext)
            )
            scanner.scanDeviceScreenshots(limit = 150)
            Result.success()
        }.getOrElse { Result.retry() }
    }
}

