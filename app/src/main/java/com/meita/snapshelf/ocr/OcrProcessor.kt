package com.meita.snapshelf.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OcrProcessor(private val context: Context) {
    suspend fun recognize(uri: Uri): String = withContext(Dispatchers.IO) {
        runCatching {
            val image = InputImage.fromFilePath(context, uri)
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .await()
                .text
        }.getOrElse { "" }
    }
}

