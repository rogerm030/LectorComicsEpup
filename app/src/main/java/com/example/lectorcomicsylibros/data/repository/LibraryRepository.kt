package com.example.lectorcomicsylibros.data.repository

import android.content.Context
import android.net.Uri
import com.example.lectorcomicsylibros.data.local.CategoryDao
import com.example.lectorcomicsylibros.data.local.MediaItemDao
import com.example.lectorcomicsylibros.data.model.Category
import com.example.lectorcomicsylibros.data.model.MediaItem
import com.example.lectorcomicsylibros.util.FileImportHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class LibraryRepository(
    private val mediaItemDao: MediaItemDao,
    private val categoryDao: CategoryDao
) {
    val allItems: Flow<List<MediaItem>> = mediaItemDao.getAllItems()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun getItemsByCategory(categoryId: Long): Flow<List<MediaItem>> =
        mediaItemDao.getItemsByCategory(categoryId)

    suspend fun getItemById(id: Long): MediaItem? =
        mediaItemDao.getItemById(id)

    suspend fun insertMediaItem(item: MediaItem): Long =
        mediaItemDao.insertItem(item)

    suspend fun updateReadingProgress(itemId: Long, lastReadPage: Int, totalPages: Int) {
        val progress = if (totalPages > 0) (lastReadPage.toFloat() / totalPages).coerceIn(0f, 1f) else 0f
        mediaItemDao.updateReadingProgress(
            id = itemId,
            lastReadPage = lastReadPage,
            totalPages = totalPages,
            readingProgress = progress
        )
    }

    suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: Category): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: Category) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: Category) {
        mediaItemDao.removeCategoryFromItems(category.id)
        categoryDao.deleteCategory(category)
    }

    suspend fun assignCategory(itemId: Long, categoryId: Long?) =
        mediaItemDao.updateCategoryForItem(itemId, categoryId)

    suspend fun deleteMediaItem(item: MediaItem) {
        item.coverPath?.let { path ->
            try {
                val file = java.io.File(path)
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaItemDao.deleteItem(item)
    }

    suspend fun importUris(context: Context, uris: List<Uri>): List<MediaItem> {
        val imported = mutableListOf<MediaItem>()
        for (uri in uris) {
            val item = FileImportHelper.processImportUri(context, uri)
            val id = mediaItemDao.insertItem(item)
            imported.add(item.copy(id = id))
        }
        return imported
    }

    suspend fun getAllFileUris(): List<String> = mediaItemDao.getAllFileUris()

    suspend fun seedDefaultCategoriesIfEmpty() {
        val existing = allCategories.first()
        if (existing.isEmpty()) {
            categoryDao.insertCategory(Category(name = "Cómics", color = 0xFFFF5722)) // Vibrant Orange/Coral
            categoryDao.insertCategory(Category(name = "Libros", color = 0xFF2196F3))  // Vibrant Blue
            categoryDao.insertCategory(Category(name = "Favoritos", color = 0xFFE91E63)) // Pink/Magenta
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LibraryRepository? = null

        fun getInstance(context: Context): LibraryRepository {
            return INSTANCE ?: synchronized(this) {
                val db = com.example.lectorcomicsylibros.data.local.AppDatabase.getDatabase(context)
                val instance = LibraryRepository(db.mediaItemDao(), db.categoryDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
