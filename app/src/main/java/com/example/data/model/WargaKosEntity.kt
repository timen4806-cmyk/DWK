package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "warga_kos")
data class WargaKosEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cloudId: String = UUID.randomUUID().toString(),
    val pemilik: String = "",
    val nama: String = "",
    val noKamar: String = "",
    val jumlahOrang: String = "",
    val alamat: String = "",
    val statusNikah: String = "",
    val statusBelumNikah: String = "",
    val keterangan: String = "",
    val syncStatus: String = SYNC_STATUS_SYNCED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SYNC_STATUS_SYNCED = "SYNCED"
        const val SYNC_STATUS_PENDING_INSERT = "PENDING_INSERT"
        const val SYNC_STATUS_PENDING_UPDATE = "PENDING_UPDATE"
        const val SYNC_STATUS_PENDING_DELETE = "PENDING_DELETE"
    }

    val isMarried: Boolean
        get() = statusNikah.equals("Ya", ignoreCase = true) || statusNikah == "✓"

    val isSingle: Boolean
        get() = statusBelumNikah.equals("Ya", ignoreCase = true) || statusBelumNikah == "✓"
}
