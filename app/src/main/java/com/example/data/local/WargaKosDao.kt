package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WargaKosEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WargaKosDao {

    @Query("SELECT * FROM warga_kos WHERE syncStatus != :deletedStatus ORDER BY id ASC")
    fun getAllWarga(deletedStatus: String = WargaKosEntity.SYNC_STATUS_PENDING_DELETE): Flow<List<WargaKosEntity>>

    @Query("SELECT * FROM warga_kos WHERE id = :id LIMIT 1")
    suspend fun getWargaById(id: Long): WargaKosEntity?

    @Query("SELECT * FROM warga_kos WHERE cloudId = :cloudId LIMIT 1")
    suspend fun getWargaByCloudId(cloudId: String): WargaKosEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarga(warga: WargaKosEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<WargaKosEntity>)

    @Update
    suspend fun updateWarga(warga: WargaKosEntity)

    @Query("DELETE FROM warga_kos WHERE id = :id")
    suspend fun deleteWargaPermanent(id: Long)

    @Query("DELETE FROM warga_kos")
    suspend fun clearAllWarga()

    @Query("UPDATE warga_kos SET syncStatus = :deletedStatus, updatedAt = :timestamp WHERE id = :id")
    suspend fun markDeleted(
        id: Long,
        deletedStatus: String = WargaKosEntity.SYNC_STATUS_PENDING_DELETE,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM warga_kos WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<WargaKosEntity>

    @Query("UPDATE warga_kos SET syncStatus = 'SYNCED', cloudId = :cloudId WHERE id = :id")
    suspend fun markSynced(id: Long, cloudId: String)

    @Query("SELECT COUNT(*) FROM warga_kos WHERE syncStatus != :deletedStatus")
    fun countWarga(deletedStatus: String = WargaKosEntity.SYNC_STATUS_PENDING_DELETE): Flow<Int>

    @Query("""
        SELECT * FROM warga_kos 
        WHERE syncStatus != :deletedStatus 
        AND (nama LIKE '%' || :query || '%' 
             OR noKamar LIKE '%' || :query || '%' 
             OR pemilik LIKE '%' || :query || '%' 
             OR alamat LIKE '%' || :query || '%')
        ORDER BY id ASC
    """)
    fun searchWarga(
        query: String,
        deletedStatus: String = WargaKosEntity.SYNC_STATUS_PENDING_DELETE
    ): Flow<List<WargaKosEntity>>
}
