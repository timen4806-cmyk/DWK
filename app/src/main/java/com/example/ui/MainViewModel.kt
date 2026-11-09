package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.KosApplication
import com.example.data.model.SavedDocumentEntity
import com.example.data.model.UserSession
import com.example.data.model.WargaKosEntity
import com.example.util.EncryptionHelper
import com.example.util.ExcelExporter
import com.example.util.FileShareHelper
import com.example.util.NotificationHelper
import com.example.util.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as KosApplication
    private val wargaRepo = app.wargaKosRepository
    private val authRepo = app.authRepository
    private val syncManager = app.cloudSyncManager
    private val prefsManager = app.preferencesManager

    // Auth state
    val currentUser: StateFlow<UserSession> = authRepo.currentUser
    val isLoginLoading = MutableStateFlow(false)

    // Current Screen Navigation State: Form login ditampilkan pertama kali membuka aplikasi
    val currentScreen = MutableStateFlow(
        if (authRepo.currentUser.value.isLoggedIn) SCREEN_HOME else SCREEN_LOGIN
    )

    // Search and Filter
    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow("ALL") // "ALL", "NIKAH", "BELUM_NIKAH"

    // Raw list from Room
    private val rawWargaList = wargaRepo.allWarga

    // Filtered Warga List
    val wargaList: StateFlow<List<WargaKosEntity>> = combine(
        rawWargaList,
        searchQuery,
        selectedFilter
    ) { list, query, filter ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.nama.contains(query, ignoreCase = true) ||
                    item.pemilik.contains(query, ignoreCase = true) ||
                    item.noKamar.contains(query, ignoreCase = true) ||
                    item.alamat.contains(query, ignoreCase = true) ||
                    item.keterangan.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "NIKAH" -> item.isMarried
                "BELUM_NIKAH" -> item.isSingle
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics
    val stats = rawWargaList.combine(selectedFilter) { list, _ ->
        val totalWarga = list.size
        val uniqueRooms = list.map { it.noKamar.trim() }.filter { it.isNotEmpty() }.distinct().size
        val nikahCount = list.count { it.isMarried }
        val belumNikahCount = list.count { it.isSingle }
        StatsData(totalWarga, uniqueRooms, nikahCount, belumNikahCount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsData())

    // Saved Documents from Room
    val savedDocuments: StateFlow<List<SavedDocumentEntity>> = wargaRepo.savedDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form editing state
    val editingId = MutableStateFlow<Long?>(null)
    val formPemilik = MutableStateFlow("")
    val formNama = MutableStateFlow("")
    val formNoKamar = MutableStateFlow("")
    val formJumlahOrang = MutableStateFlow("1")
    val formAlamat = MutableStateFlow("")
    val formStatusNikah = MutableStateFlow("") // "Ya", "Tidak", ""
    val formStatusBelumNikah = MutableStateFlow("") // "Ya", "Tidak", ""
    val formKeterangan = MutableStateFlow("")



    // Sync State
    val isSyncing = syncManager.isSyncing
    val lastSyncTime = syncManager.lastSyncTime
    val syncStatus = syncManager.syncStatus
    val pendingCount = syncManager.pendingCount

    // Theme & Preferences
    val themeMode = prefsManager.themeMode
    val reportTitle = prefsManager.reportTitle
    val realtimeSync = prefsManager.realtimeSync

    // Document Preview State
    val previewDoc = MutableStateFlow<SavedDocumentEntity?>(null)
    val previewFile = MutableStateFlow<File?>(null)
    val previewPdfPages = MutableStateFlow<List<Bitmap>>(emptyList())
    val isPreviewLoading = MutableStateFlow(false)

    // Snackbars / Toast events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }

    fun navigateTo(screen: String) {
        currentScreen.value = screen
    }

    // FORM ACTIONS
    fun startCreateNew() {
        clearForm()
        currentScreen.value = SCREEN_FORM
    }

    fun startEdit(warga: WargaKosEntity) {
        editingId.value = warga.id
        formPemilik.value = warga.pemilik
        formNama.value = warga.nama
        formNoKamar.value = warga.noKamar
        formJumlahOrang.value = warga.jumlahOrang
        formAlamat.value = warga.alamat
        formStatusNikah.value = warga.statusNikah
        formStatusBelumNikah.value = warga.statusBelumNikah
        formKeterangan.value = warga.keterangan
        currentScreen.value = SCREEN_FORM
    }

    fun clearForm() {
        editingId.value = null
        formPemilik.value = ""
        formNama.value = ""
        formNoKamar.value = ""
        formJumlahOrang.value = "1"
        formAlamat.value = ""
        formStatusNikah.value = ""
        formStatusBelumNikah.value = ""
        formKeterangan.value = ""
    }

    fun saveWarga() {
        val nama = formNama.value.trim()
        if (nama.isEmpty()) {
            showMessage("Nama warga kos wajib diisi!")
            return
        }

        viewModelScope.launch {
            val entity = WargaKosEntity(
                id = editingId.value ?: 0,
                pemilik = formPemilik.value.trim(),
                nama = nama,
                noKamar = formNoKamar.value.trim(),
                jumlahOrang = formJumlahOrang.value.trim().ifEmpty { "1" },
                alamat = formAlamat.value.trim(),
                statusNikah = formStatusNikah.value,
                statusBelumNikah = formStatusBelumNikah.value,
                keterangan = formKeterangan.value.trim()
            )

            if (editingId.value == null) {
                wargaRepo.insertWarga(entity)
                showMessage("Data warga kos berhasil ditambahkan.")
            } else {
                wargaRepo.updateWarga(entity)
                showMessage("Data warga kos berhasil diperbarui.")
            }

            clearForm()
            currentScreen.value = SCREEN_HOME
        }
    }

    fun deleteWarga(warga: WargaKosEntity) {
        viewModelScope.launch {
            wargaRepo.deleteWarga(warga.id)
            showMessage("Data warga ${warga.nama} berhasil dihapus.")
        }
    }

    // EXPORT & DOCUMENT ACTIONS
    fun exportDocument(
        type: String, // "PDF" or "EXCEL"
        customTitle: String,
        targetDir: String = "Download",
        isEncrypted: Boolean = false,
        password: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentData = wargaList.value
            val title = customTitle.ifBlank { reportTitle.value }

            val exportedFile: File? = if (type == "PDF") {
                PdfExporter.generatePdf(
                    context = app,
                    title = title,
                    data = currentData,
                    isEncrypted = isEncrypted,
                    password = password,
                    targetDir = targetDir
                )
            } else {
                ExcelExporter.generateExcel(
                    context = app,
                    title = title,
                    data = currentData,
                    isEncrypted = isEncrypted,
                    password = password,
                    targetDir = targetDir
                )
            }

            if (exportedFile != null && exportedFile.exists()) {
                val docId = wargaRepo.recordSavedDocument(
                    title = title,
                    fileName = exportedFile.name,
                    filePath = exportedFile.absolutePath,
                    fileType = type,
                    fileSizeBytes = exportedFile.length(),
                    isEncrypted = isEncrypted,
                    passwordHint = if (isEncrypted) "Dilindungi password (${password.take(2)}***)" else ""
                )

                val savedDoc = SavedDocumentEntity(
                    id = docId,
                    title = title,
                    fileName = exportedFile.name,
                    filePath = exportedFile.absolutePath,
                    fileType = type,
                    fileSizeBytes = exportedFile.length(),
                    isEncrypted = isEncrypted,
                    passwordHint = if (isEncrypted) "Dilindungi password" else ""
                )

                NotificationHelper.showNotification(
                    context = app,
                    notificationId = 2001,
                    title = "Ekspor $type Berhasil!",
                    message = "File ${exportedFile.name} berhasil disimpan di direktori $targetDir."
                )

                withContext(Dispatchers.Main) {
                    showMessage("Laporan $type berhasil disimpan di $targetDir/DataWargaKos")
                    // Automatically open preview so user can review before sharing!
                    loadDocumentForPreview(savedDoc, password)
                }
            } else {
                withContext(Dispatchers.Main) {
                    showMessage("Gagal mengekspor laporan $type")
                }
            }
        }
    }

    fun loadDocumentForPreview(doc: SavedDocumentEntity, passwordInput: String = "") {
        previewDoc.value = doc
        isPreviewLoading.value = true
        previewPdfPages.value = emptyList()
        currentScreen.value = SCREEN_PREVIEW

        viewModelScope.launch(Dispatchers.IO) {
            val file = File(doc.filePath)
            if (!file.exists()) {
                withContext(Dispatchers.Main) {
                    isPreviewLoading.value = false
                    showMessage("File tidak ditemukan pada path penyimpanan.")
                }
                return@launch
            }

            previewFile.value = file

            if (doc.fileType == "PDF") {
                val pdfToRender: File = if (doc.isEncrypted) {
                    val decryptedTemp = File(app.cacheDir, "temp_preview_${System.currentTimeMillis()}.pdf")
                    val ok = EncryptionHelper.decryptFile(file, decryptedTemp, passwordInput)
                    if (ok) decryptedTemp else file
                } else {
                    file
                }

                try {
                    val pfd = ParcelFileDescriptor.open(pdfToRender, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    val bitmaps = mutableListOf<Bitmap>()

                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        // Scale for crisp rendering
                        val width = page.width * 2
                        val height = page.height * 2
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        bitmaps.add(bitmap)
                    }
                    renderer.close()
                    pfd.close()

                    withContext(Dispatchers.Main) {
                        previewPdfPages.value = bitmaps
                        isPreviewLoading.value = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        isPreviewLoading.value = false
                        showMessage("Gagal merender pratinjau PDF: ${e.localizedMessage}")
                    }
                }
            } else {
                // Excel preview
                withContext(Dispatchers.Main) {
                    isPreviewLoading.value = false
                }
            }
        }
    }

    fun shareCurrentPreview() {
        val file = previewFile.value
        if (file != null && file.exists()) {
            FileShareHelper.shareFile(app, file, previewDoc.value?.title ?: "Dokumen Warga Kos")
        } else {
            showMessage("File tidak tersedia untuk dibagikan")
        }
    }

    fun shareSavedDoc(doc: SavedDocumentEntity) {
        val file = File(doc.filePath)
        if (file.exists()) {
            FileShareHelper.shareFile(app, file, doc.title)
        } else {
            showMessage("File tidak ditemukan")
        }
    }

    fun openSavedDocExternal(doc: SavedDocumentEntity) {
        val file = File(doc.filePath)
        if (file.exists()) {
            FileShareHelper.openFile(app, file)
        } else {
            showMessage("File tidak ditemukan")
        }
    }

    fun deleteSavedDoc(doc: SavedDocumentEntity) {
        viewModelScope.launch {
            val file = File(doc.filePath)
            if (file.exists()) file.delete()
            wargaRepo.deleteSavedDocument(doc.id)
            showMessage("Dokumen ${doc.fileName} dihapus.")
        }
    }

    // SYNC ACTIONS
    fun triggerSyncNow() {
        syncManager.triggerSyncNow()
        showMessage("Sinkronisasi cloud sedang diproses...")
    }

    fun testCrossDeviceSync() {
        viewModelScope.launch {
            showMessage("Mensimulasikan perubahan dari perangkat lain...")
            val result = syncManager.simulateRemoteDeviceChange()
            showMessage(result)
        }
    }

    // AUTH ACTIONS
    fun loginWithEmailPassword(email: String, pass: String) {
        viewModelScope.launch {
            isLoginLoading.value = true
            val res = authRepo.loginWithEmailPassword(email, pass)
            isLoginLoading.value = false
            res.fold(
                onSuccess = {
                    showMessage("Login berhasil! Token terenkripsi tersimpan.")
                    currentScreen.value = SCREEN_HOME
                },
                onFailure = {
                    showMessage("Gagal login: ${it.localizedMessage}")
                }
            )
        }
    }

    fun loginWithOAuth() {
        viewModelScope.launch {
            isLoginLoading.value = true
            val res = authRepo.loginWithOAuth("Google")
            isLoginLoading.value = false
            res.fold(
                onSuccess = {
                    showMessage("Autentikasi OAuth 2.0 Google berhasil!")
                    currentScreen.value = SCREEN_HOME
                },
                onFailure = {
                    showMessage("Gagal autentikasi OAuth: ${it.localizedMessage}")
                }
            )
        }
    }

    fun logout() {
        authRepo.logout()
        currentScreen.value = SCREEN_LOGIN
        showMessage("Anda telah logout.")
    }

    fun testPushNotification() {
        NotificationHelper.showNotification(
            context = app,
            notificationId = 999,
            title = "Pemberitahuan Warga Kos",
            message = "Notifikasi push Firebase Cloud Messaging aktif. Sistem sinkronisasi real-time siap."
        )
        showMessage("Notifikasi push dikirimkan ke perangkat.")
    }

    fun setThemeMode(mode: String) {
        prefsManager.setThemeMode(mode)
    }

    fun setReportTitle(title: String) {
        prefsManager.setReportTitle(title)
    }

    fun setRealtimeSync(enabled: Boolean) {
        prefsManager.setRealtimeSync(enabled)
    }

    companion object {
        const val SCREEN_HOME = "home"
        const val SCREEN_FORM = "form"
        const val SCREEN_EXPORT = "export"
        const val SCREEN_SETTINGS = "settings"
        const val SCREEN_PREVIEW = "preview"
        const val SCREEN_LOGIN = "login"
    }
}

data class StatsData(
    val totalWarga: Int = 0,
    val kamarTerisi: Int = 0,
    val nikahCount: Int = 0,
    val belumNikahCount: Int = 0
)
