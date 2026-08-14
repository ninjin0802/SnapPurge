package com.meita.snapshelf.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM screenshots WHERE isArchived = 0 ORDER BY dateTaken DESC")
    fun observeActive(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE duplicateGroup IS NOT NULL ORDER BY duplicateGroup, dateTaken DESC")
    fun observeDuplicates(): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots WHERE id = :id")
    fun observeById(id: Long): Flow<ScreenshotEntity?>

    @Query("SELECT * FROM screenshots WHERE id = :id")
    suspend fun getById(id: Long): ScreenshotEntity?

    @Query("SELECT * FROM screenshots WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): ScreenshotEntity?

    @Query(
        """
        SELECT screenshots.* FROM screenshots
        JOIN screenshots_fts ON screenshots.id = screenshots_fts.rowid
        WHERE screenshots_fts MATCH :ftsQuery
        AND screenshots.isArchived = 0
        ORDER BY screenshots.dateTaken DESC
        """
    )
    fun search(ftsQuery: String): Flow<List<ScreenshotEntity>>

    @Query("SELECT * FROM screenshots ORDER BY dateTaken DESC")
    suspend fun getAllNow(): List<ScreenshotEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ScreenshotEntity): Long

    @Update
    suspend fun update(entity: ScreenshotEntity)

    @Delete
    suspend fun delete(entity: ScreenshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFts(entity: ScreenshotFtsEntity)

    @Query("DELETE FROM screenshots_fts WHERE rowid = :id")
    suspend fun deleteFts(id: Long)

    @Query("DELETE FROM screenshots")
    suspend fun clearScreenshots()

    @Query("DELETE FROM screenshots_fts")
    suspend fun clearFts()

    @Transaction
    suspend fun clearAll() {
        clearFts()
        clearScreenshots()
    }
}

