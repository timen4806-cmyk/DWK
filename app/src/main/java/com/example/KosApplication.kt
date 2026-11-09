package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.TokenManager
import com.example.data.remote.CloudSyncManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.WargaKosRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KosApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val tokenManager by lazy { TokenManager(this) }
    val preferencesManager by lazy { PreferencesManager(this) }
    val cloudSyncManager by lazy { CloudSyncManager(this, database.wargaKosDao(), tokenManager) }
    val authRepository by lazy { AuthRepository(tokenManager) }
    val wargaKosRepository by lazy {
        WargaKosRepository(database.wargaKosDao(), database.savedDocumentDao(), cloudSyncManager)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.initNotificationChannel(this)

        // Ensure database starts completely empty with no dummy/sample records
        CoroutineScope(Dispatchers.IO).launch {
            try {
                wargaKosRepository.clearAllData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
