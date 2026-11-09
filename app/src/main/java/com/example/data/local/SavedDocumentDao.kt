package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedDocumentDao {

    @Query("SELECT * FROM saved_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<SavedDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: SavedDocumentEntity): Long

    @Query("DELETE FROM saved_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("SELECT * FROM saved_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): SavedDocumentEntity?
}
