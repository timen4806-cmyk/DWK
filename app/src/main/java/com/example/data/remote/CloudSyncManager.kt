package com.example.data.remote

import android.content.Context
import com.example.data.local.TokenManager
import com.example.data.local.WargaKosDao
import com.example.data.model.WargaKosEntity
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class CloudSyncManager(
    private val context: Context,
    private val wargaKosDao: WargaKosDao,
    private val tokenManager: TokenManager
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var periodicJob: Job? = null

    private val deviceId = UUID.randomUUID().toString().take(8)

    // Remote mock cloud server memory store (simulating third-party cloud backend)
    private val cloudStore = mutableMapOf<String, WargaKosRemoteDto>()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _syncStatus = MutableStateFlow("Tersinkron")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    init {
        // Start periodic sync worker (runs every 30 seconds when enabled)
        startPeriodicSync()
    }

    fun startPeriodicSync() {
        periodicJob?.cancel()
        periodicJob = scope.launch {
            while (isActive) {
                delay(30_000)
                performSync()
            }
        }
    }

    suspend fun performSync(): Boolean = withContext(Dispatchers.IO) {
        if (_isSyncing.value) return@withContext false
        _isSyncing.value = true
        _syncStatus.value = "Menyinkronkan..."

        try {
            val session = tokenManager.getSession()
            val token = session.accessToken
            // Simulate network latency
            delay(1200)

            // 1. Gather pending local changes
            val pendingItems = wargaKosDao.getPendingSync()
            _pendingCount.value = pendingItems.size

            var pushedCount = 0
            for (local in pendingItems) {
                val isDeleted = local.syncStatus == WargaKosEntity.SYNC_STATUS_PENDING_DELETE
                val dto = WargaKosRemoteDto(
                    cloudId = local.cloudId,
                    pemilik = local.pemilik,
                    nama = local.nama,
                    noKamar = local.noKamar,
                    jumlahOrang = local.jumlahOrang,
                    alamat = local.alamat,
                    statusNikah = local.statusNikah,
                    statusBelumNikah = local.statusBelumNikah,
                    keterangan = local.keterangan,
                    isDeleted = isDeleted,
                    updatedAt = local.updatedAt
                )

                // Push to cloud store
                cloudStore[local.cloudId] = dto

                if (isDeleted) {
                    wargaKosDao.deleteWargaPermanent(local.id)
                } else {
                    wargaKosDao.markSynced(local.id, local.cloudId)
                }
                pushedCount++
            }

            // 2. Pull remote changes from cloud store
            var pulledCount = 0
            for ((cloudId, remoteDto) in cloudStore) {
                val existing = wargaKosDao.getWargaByCloudId(cloudId)
                if (existing == null) {
                    if (!remoteDto.isDeleted) {
                        wargaKosDao.insertWarga(
                            WargaKosEntity(
                                cloudId = remoteDto.cloudId,
                                pemilik = remoteDto.pemilik,
                                nama = remoteDto.nama,
                                noKamar = remoteDto.noKamar,
                                jumlahOrang = remoteDto.jumlahOrang,
                                alamat = remoteDto.alamat,
                                statusNikah = remoteDto.statusNikah,
                                statusBelumNikah = remoteDto.statusBelumNikah,
                                keterangan = remoteDto.keterangan,
                                syncStatus = WargaKosEntity.SYNC_STATUS_SYNCED,
                                updatedAt = remoteDto.updatedAt
                            )
                        )
                        pulledCount++
                    }
                } else {
                    // Conflict resolution: Last-Write-Wins
                    if (remoteDto.updatedAt > existing.updatedAt) {
                        if (remoteDto.isDeleted) {
                            wargaKosDao.deleteWargaPermanent(existing.id)
                        } else {
                            wargaKosDao.updateWarga(
                                existing.copy(
                                    pemilik = remoteDto.pemilik,
                                    nama = remoteDto.nama,
                                    noKamar = remoteDto.noKamar,
                                    jumlahOrang = remoteDto.jumlahOrang,
                                    alamat = remoteDto.alamat,
                                    statusNikah = remoteDto.statusNikah,
                                    statusBelumNikah = remoteDto.statusBelumNikah,
                                    keterangan = remoteDto.keterangan,
                                    syncStatus = WargaKosEntity.SYNC_STATUS_SYNCED,
                                    updatedAt = remoteDto.updatedAt
                                )
                            )
                        }
                        pulledCount++
                    }
                }
            }

            _lastSyncTime.value = System.currentTimeMillis()
            _pendingCount.value = 0
            _syncStatus.value = "Tersinkron (Push: $pushedCount, Pull: $pulledCount)"

            if (pulledCount > 0) {
                NotificationHelper.showNotification(
                    context = context,
                    notificationId = 1001,
                    title = "Sinkronisasi Cloud Selesai",
                    message = "$pulledCount perubahan data warga kos berhasil disinkronkan dari server awan."
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _syncStatus.value = "Gagal: ${e.localizedMessage}"
            false
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Simulate a change from another device on the cloud backend to test real-time cross-device sync!
     */
    suspend fun simulateRemoteDeviceChange(residentName: String = "Budi Santoso"): String = withContext(Dispatchers.IO) {
        val newCloudId = UUID.randomUUID().toString()
        val remoteResident = WargaKosRemoteDto(
            cloudId = newCloudId,
            pemilik = "Ibu Hj. Aminah (Kos Melati)",
            nama = "$residentName [Remote Device]",
            noKamar = "B-${(10..99).random()}",
            jumlahOrang = "1",
            alamat = "Jl. Sukabumi No. 45, Bandung",
            statusNikah = "",
            statusBelumNikah = "Ya",
            keterangan = "Disinkronkan otomatis dari perangkat tablet lain",
            updatedAt = System.currentTimeMillis()
        )
        cloudStore[newCloudId] = remoteResident

        // Trigger sync
        performSync()

        return@withContext "Berhasil disinkronkan: ${remoteResident.nama}"
    }

    fun triggerSyncNow() {
        scope.launch {
            performSync()
        }
    }
}
