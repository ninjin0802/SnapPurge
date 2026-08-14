package com.meita.snapshelf.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageHash {
    suspend fun averageHash(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input) ?: return@use null
                val scaled = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
                val values = IntArray(64)
                var total = 0
                var index = 0
                for (y in 0 until 8) {
                    for (x in 0 until 8) {
                        val pixel = scaled.getPixel(x, y)
                        val gray = ((pixel shr 16 and 0xff) + (pixel shr 8 and 0xff) + (pixel and 0xff)) / 3
                        values[index++] = gray
                        total += gray
                    }
                }
                val average = total / 64
                values.joinToString(separator = "") { if (it >= average) "1" else "0" }
            }
        }.getOrNull()
    }
}

