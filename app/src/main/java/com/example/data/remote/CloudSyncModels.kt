package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WargaKosRemoteDto(
    val cloudId: String,
    val pemilik: String,
    val nama: String,
    val noKamar: String,
    val jumlahOrang: String,
    val alamat: String,
    val statusNikah: String,
    val statusBelumNikah: String,
    val keterangan: String,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class SyncPushRequest(
    val deviceId: String,
    val clientTimestamp: Long,
    val items: List<WargaKosRemoteDto>
)

@JsonClass(generateAdapter = true)
data class SyncResponse(
    val status: String,
    val message: String,
    val serverTimestamp: Long,
    val syncedCount: Int,
    val remoteItems: List<WargaKosRemoteDto> = emptyList()
)
