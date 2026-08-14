package com.meita.snapshelf.core

import android.content.Context
import androidx.room.Room
import com.meita.snapshelf.data.local.SnapShelfDatabase
import com.meita.snapshelf.data.repository.ScreenshotRepository
import com.meita.snapshelf.media.ScreenshotScanner
import com.meita.snapshelf.notifications.NotificationHelper
import com.meita.snapshelf.ocr.OcrProcessor
import com.meita.snapshelf.settings.UserPreferences

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: SnapShelfDatabase = Room.databaseBuilder(
        appContext,
        SnapShelfDatabase::class.java,
        "snapshelf.db"
    ).fallbackToDestructiveMigration().build()

    val notificationHelper = NotificationHelper(appContext)
    val userPreferences = UserPreferences(appContext)
    val ocrProcessor = OcrProcessor(appContext)
    val repository = ScreenshotRepository(
        dao = database.screenshotDao(),
        context = appContext,
        notificationHelper = notificationHelper
    )
    val screenshotScanner = ScreenshotScanner(
        context = appContext,
        repository = repository,
        ocrProcessor = ocrProcessor
    )
}

