package com.meita.snapshelf.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.meita.snapshelf.data.repository.ScreenshotRepository
import com.meita.snapshelf.ocr.OcrProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScreenshotScanner(
    private val context: Context,
    private val repository: ScreenshotRepository,
    private val ocrProcessor: OcrProcessor
) {
    suspend fun scanDeviceScreenshots(limit: Int = 500): Int = withContext(Dispatchers.IO) {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = mutableListOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection += MediaStore.Images.Media.RELATIVE_PATH
        }

        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ? OR ${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
        }
        val args = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf("%Screenshot%", "%Screenshots%")
        } else {
            arrayOf("%Screenshot%")
        }
        val sort = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        var count = 0
        context.contentResolver.query(collection, projection.toTypedArray(), selection, args, sort)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            while (cursor.moveToNext() && count < limit) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                val displayName = cursor.getString(nameColumn) ?: "Screenshot"
                val dateTaken = cursor.getLong(dateColumn).takeIf { it > 0 } ?: System.currentTimeMillis()
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val ocrText = ocrProcessor.recognize(uri)
                repository.upsertScanned(uri, displayName, dateTaken, width, height, ocrText)
                count++
            }
        }
        repository.refreshDuplicateGroups()
        count
    }

    suspend fun importSelectedUris(uris: List<Uri>): Int {
        uris.forEach { uri ->
            val text = ocrProcessor.recognize(uri)
            repository.upsertScanned(
                uri = uri,
                displayName = uri.lastPathSegment ?: "Selected screenshot",
                dateTaken = System.currentTimeMillis(),
                width = null,
                height = null,
                ocrText = text
            )
        }
        repository.refreshDuplicateGroups()
        return uris.size
    }
}

