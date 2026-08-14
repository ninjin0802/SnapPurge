package com.meita.snapshelf.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScreenshotEntity::class, ScreenshotFtsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SnapShelfDatabase : RoomDatabase() {
    abstract fun screenshotDao(): ScreenshotDao
}
