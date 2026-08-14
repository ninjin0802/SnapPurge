package com.meita.snapshelf.data.repository

import android.content.Context
import android.net.Uri
import android.content.ContentValues
import android.provider.MediaStore
import com.meita.snapshelf.data.local.ScreenshotDao
import com.meita.snapshelf.data.local.ScreenshotEntity
import com.meita.snapshelf.data.local.ScreenshotFtsEntity
import com.meita.snapshelf.domain.CategoryClassifier
import com.meita.snapshelf.domain.DateExtractor
import com.meita.snapshelf.domain.DuplicateDetector
import com.meita.snapshelf.domain.ImageHash
import com.meita.snapshelf.domain.SummaryExtractor
import com.meita.snapshelf.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScreenshotRepository(
    private val dao: ScreenshotDao,
    private val context: Context,
    private val notificationHelper: NotificationHelper
) {
    fun observeActive(): Flow<List<ScreenshotEntity>> = dao.observeActive()

    fun observeDuplicates(): Flow<List<ScreenshotEntity>> = dao.observeDuplicates()

    fun observeById(id: Long): Flow<ScreenshotEntity?> = dao.observeById(id)

    suspend fun getAllActiveNow(): List<ScreenshotEntity> = withContext(Dispatchers.IO) {
        dao.getAllNow().filterNot { it.isArchived }
    }

    fun search(rawQuery: String): Flow<List<ScreenshotEntity>> {
        val query = rawQuery
            .split(Regex("\\s+"))
            .mapNotNull { token ->
                token.trim()
                    .replace("\"", "")
                    .replace("*", "")
                    .takeIf { it.isNotBlank() }
            }
            .joinToString(separator = " ") { "$it*" }
        return if (query.isBlank()) observeActive() else dao.search(query)
    }

    suspend fun upsertScanned(
        uri: Uri,
        displayName: String,
        dateTaken: Long,
        width: Int?,
        height: Int?,
        ocrText: String
    ): ScreenshotEntity = withContext(Dispatchers.IO) {
        val existing = dao.getByUri(uri.toString())
        val summary = SummaryExtractor.summarize(ocrText)
        val category = CategoryClassifier.classify(ocrText, displayName)
        val tags = CategoryClassifier.suggestedTags(ocrText).joinToString(",")
        val reminderAt = DateExtractor.extractFirstDeadlineMillis(ocrText)
        val hash = ImageHash.averageHash(context, uri)

        val base = existing ?: ScreenshotEntity(
            uri = uri.toString(),
            displayName = displayName,
            dateTaken = dateTaken,
            addedAt = System.currentTimeMillis(),
            width = width,
            height = height
        )
        val enriched = base.copy(
            displayName = displayName,
            dateTaken = dateTaken,
            width = width,
            height = height,
            ocrText = ocrText,
            summary = summary,
            category = category,
            tags = tags,
            reminderAt = reminderAt,
            imageHash = hash
        )

        val id = if (existing == null) dao.insert(enriched) else {
            dao.update(enriched)
            enriched.id
        }
        val saved = enriched.copy(id = id)
        dao.upsertFts(saved.toFts())
        reminderAt?.let { notificationHelper.scheduleReminder(saved.id, saved.displayName, it) }
        saved
    }

    suspend fun addManualUri(uri: Uri) {
        upsertScanned(
            uri = uri,
            displayName = uri.lastPathSegment ?: "Selected screenshot",
            dateTaken = System.currentTimeMillis(),
            width = null,
            height = null,
            ocrText = ""
        )
    }

    suspend fun toggleFavorite(id: Long) {
        dao.getById(id)?.let { dao.update(it.copy(isFavorite = !it.isFavorite)) }
    }

    suspend fun toggleArchive(id: Long) {
        dao.getById(id)?.let { dao.update(it.copy(isArchived = !it.isArchived)) }
    }

    suspend fun setThemeTag(id: Long, tag: String) {
        dao.getById(id)?.let {
            val tags = (it.tags.split(",").filter { value -> value.isNotBlank() } + tag)
                .distinct()
                .joinToString(",")
            val updated = it.copy(tags = tags)
            dao.update(updated)
            dao.upsertFts(updated.toFts())
        }
    }

    suspend fun deleteIndexRecord(id: Long) {
        dao.getById(id)?.let {
            dao.deleteFts(id)
            dao.delete(it)
        }
    }

    suspend fun deleteIndexRecords(ids: Set<Long>) {
        ids.forEach { deleteIndexRecord(it) }
    }

    suspend fun moveToCategoryFolders(items: List<ScreenshotEntity>): Int = withContext(Dispatchers.IO) {
        var moved = 0
        items.forEach { item ->
            val folder = when (item.category) {
                "買い物", "予定", "仕事", "学習", "SNS", "旅行", "金融" -> item.category
                else -> "メモ"
            }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SnapPurge/$folder/")
            }
            val changed = runCatching {
                context.contentResolver.update(Uri.parse(item.uri), values, null, null)
            }.getOrDefault(0)
            if (changed > 0) moved++
        }
        moved
    }

    suspend fun clearIndex() = dao.clearAll()

    suspend fun refreshDuplicateGroups() = withContext(Dispatchers.IO) {
        val items = dao.getAllNow()
        val known = mutableListOf<Pair<Long, String>>()
        items.forEach { item ->
            val hash = item.imageHash
            if (hash == null) {
                dao.update(item.copy(duplicateGroup = null))
            } else {
                val group = DuplicateDetector.duplicateGroupFor(hash, known)
                dao.update(item.copy(duplicateGroup = group))
                known += item.id to hash
            }
        }
    }

    private fun ScreenshotEntity.toFts() = ScreenshotFtsEntity(
        rowId = id,
        displayName = displayName,
        ocrText = ocrText,
        summary = summary,
        tags = tags,
        category = category
    )
}
