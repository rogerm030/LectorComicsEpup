package com.example.lectorcomicsylibros.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_items",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val fileUri: String,
    val format: String, // "CBZ", "CBR", "EPUB", "PDF"
    val coverPath: String? = null,
    val lastReadPage: Int = 0,
    val totalPages: Int = 0,
    val categoryId: Long? = null,
    val addedDate: Long = System.currentTimeMillis(),
    val readingProgress: Float = 0f
)
