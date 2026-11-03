package com.example.lectorcomicsylibros.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lectorcomicsylibros.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {
    @Query("SELECT * FROM media_items ORDER BY addedDate DESC")
    fun getAllItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE categoryId = :categoryId ORDER BY addedDate DESC")
    fun getItemsByCategory(categoryId: Long): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getItemById(id: Long): MediaItem?

    @Query("SELECT fileUri FROM media_items")
    suspend fun getAllFileUris(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MediaItem): Long

    @Update
    suspend fun updateItem(item: MediaItem)

    @Delete
    suspend fun deleteItem(item: MediaItem)

    @Query("UPDATE media_items SET categoryId = :categoryId WHERE id = :itemId")
    suspend fun updateCategoryForItem(itemId: Long, categoryId: Long?)

    @Query("UPDATE media_items SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun removeCategoryFromItems(categoryId: Long)

    @Query("UPDATE media_items SET lastReadPage = :lastReadPage, totalPages = :totalPages, readingProgress = :readingProgress WHERE id = :id")
    suspend fun updateReadingProgress(id: Long, lastReadPage: Int, totalPages: Int, readingProgress: Float)
}
