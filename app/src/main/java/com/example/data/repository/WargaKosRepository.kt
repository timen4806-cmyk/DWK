package com.example.data.repository

import com.example.data.local.SavedDocumentDao
import com.example.data.local.WargaKosDao
import com.example.data.model.SavedDocumentEntity
import com.example.data.model.WargaKosEntity
import com.example.data.remote.CloudSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WargaKosRepository(
    private val wargaKosDao: WargaKosDao,
    private val savedDocumentDao: SavedDocumentDao,
    private val cloudSyncManager: CloudSyncManager
) {

    val allWarga: Flow<List<WargaKosEntity>> = wargaKosDao.getAllWarga()

    val savedDocuments: Flow<List<SavedDocumentEntity>> = savedDocumentDao.getAllDocuments()

    fun searchWarga(query: String): Flow<List<WargaKosEntity>> {
        return if (query.isBlank()) {
            wargaKosDao.getAllWarga()
        } else {
            wargaKosDao.searchWarga(query.trim())
        }
    }

    suspend fun getWargaById(id: Long): WargaKosEntity? = withContext(Dispatchers.IO) {
        wargaKosDao.getWargaById(id)
    }

    suspend fun insertWarga(warga: WargaKosEntity): Long = withContext(Dispatchers.IO) {
        val toInsert = warga.copy(
            syncStatus = WargaKosEntity.SYNC_STATUS_PENDING_INSERT,
            updatedAt = System.currentTimeMillis()
        )
        val id = wargaKosDao.insertWarga(toInsert)
        cloudSyncManager.triggerSyncNow()
        id
    }

    suspend fun updateWarga(warga: WargaKosEntity) = withContext(Dispatchers.IO) {
        val toUpdate = warga.copy(
            syncStatus = WargaKosEntity.SYNC_STATUS_PENDING_UPDATE,
            updatedAt = System.currentTimeMillis()
        )
        wargaKosDao.updateWarga(toUpdate)
        cloudSyncManager.triggerSyncNow()
    }

    suspend fun deleteWarga(id: Long) = withContext(Dispatchers.IO) {
        wargaKosDao.markDeleted(id)
        cloudSyncManager.triggerSyncNow()
    }

    suspend fun recordSavedDocument(
        title: String,
        fileName: String,
        filePath: String,
        fileType: String,
        fileSizeBytes: Long,
        isEncrypted: Boolean,
        passwordHint: String
    ): Long = withContext(Dispatchers.IO) {
        savedDocumentDao.insertDocument(
            SavedDocumentEntity(
                title = title,
                fileName = fileName,
                filePath = filePath,
                fileType = fileType,
                fileSizeBytes = fileSizeBytes,
                isEncrypted = isEncrypted,
                passwordHint = passwordHint
            )
        )
    }

    suspend fun deleteSavedDocument(id: Long) = withContext(Dispatchers.IO) {
        savedDocumentDao.deleteDocumentById(id)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        wargaKosDao.clearAllWarga()
    }
}
