package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_documents")
data class SavedDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val fileName: String,
    val filePath: String,
    val fileType: String, // "PDF" or "EXCEL"
    val fileSizeBytes: Long = 0,
    val isEncrypted: Boolean = false,
    val passwordHint: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
