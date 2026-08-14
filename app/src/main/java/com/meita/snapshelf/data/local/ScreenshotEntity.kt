package com.meita.snapshelf.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "screenshots",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["category"]),
        Index(value = ["dateTaken"]),
        Index(value = ["duplicateGroup"])
    ]
)
data class ScreenshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val displayName: String,
    val dateTaken: Long,
    val addedAt: Long,
    val width: Int? = null,
    val height: Int? = null,
    val ocrText: String = "",
    val summary: String = "",
    val category: String = "未分類",
    val tags: String = "",
    val reminderAt: Long? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val imageHash: String? = null,
    val duplicateGroup: String? = null
)

