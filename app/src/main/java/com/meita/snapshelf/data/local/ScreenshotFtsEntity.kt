package com.meita.snapshelf.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Fts4
@Entity(tableName = "screenshots_fts")
data class ScreenshotFtsEntity(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Long,
    val displayName: String,
    val ocrText: String,
    val summary: String,
    val tags: String,
    val category: String
)

