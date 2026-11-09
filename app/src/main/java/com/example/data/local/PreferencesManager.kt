package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "warga_kos_settings"
        private const val KEY_THEME_MODE = "theme_mode" // "SYSTEM", "LIGHT", "DARK"
        private const val KEY_REPORT_TITLE = "report_title"
        private const val KEY_REALTIME_SYNC = "realtime_sync"
        private const val KEY_NOTIF_ENABLED = "notif_enabled"
        private const val KEY_AUTO_EXPORT_PREVIEW = "auto_export_preview"
        private const val KEY_KETUA_RW = "ketua_rw_title"
        private const val KEY_KETUA_RT1 = "ketua_rt1_title"
        private const val KEY_KETUA_RT2 = "ketua_rt2_title"
    }

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _reportTitle = MutableStateFlow(prefs.getString(KEY_REPORT_TITLE, "DATA WARGA KOS") ?: "DATA WARGA KOS")
    val reportTitle: StateFlow<String> = _reportTitle.asStateFlow()

    private val _realtimeSync = MutableStateFlow(prefs.getBoolean(KEY_REALTIME_SYNC, true))
    val realtimeSync: StateFlow<Boolean> = _realtimeSync.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIF_ENABLED, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setReportTitle(title: String) {
        val finalTitle = title.ifBlank { "DATA WARGA KOS" }
        prefs.edit().putString(KEY_REPORT_TITLE, finalTitle).apply()
        _reportTitle.value = finalTitle
    }

    fun setRealtimeSync(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REALTIME_SYNC, enabled).apply()
        _realtimeSync.value = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun getSignatureTitles(): Triple<String, String, String> {
        val rw = prefs.getString(KEY_KETUA_RW, "Mengetahui,\nKetua RW. 01") ?: "Mengetahui,\nKetua RW. 01"
        val rt1 = prefs.getString(KEY_KETUA_RT1, "Ketua RT. 01") ?: "Ketua RT. 01"
        val rt2 = prefs.getString(KEY_KETUA_RT2, "Ketua RT. 02") ?: "Ketua RT. 02"
        return Triple(rw, rt1, rt2)
    }
}
