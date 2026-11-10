package com.example.ticket_scanner.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object HistoryStorageManager {
    private const val PREFS_NAME = "ticket_scanner_prefs"
    private const val KEY_SCAN_HISTORY = "scan_history_json"
    private const val KEY_DARK_MODE = "dark_mode_enabled"
    private const val KEY_FAST_MODE = "fast_mode_enabled"
    private const val KEY_DEFAULT_ZOOM = "default_zoom_ratio"
    private const val KEY_AUTO_SYNC = "auto_sync_enabled"
    private const val KEY_SOUND_VIBRATION = "sound_vibration_enabled"
    private const val KEY_PUSH_NOTIFICATIONS = "push_notifications_enabled"

    fun saveHistory(context: Context, items: List<ScanItem>) {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("attendeeName", item.attendeeName)
                put("ticketCode", item.ticketCode)
                put("time", item.time)
                put("status", item.status.name)
                put("isSynced", item.isSynced)
                put("event", item.event)
            }
            jsonArray.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCAN_HISTORY, jsonArray.toString()).apply()
    }

    fun loadHistory(context: Context): List<ScanItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_SCAN_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<ScanItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val statusStr = obj.optString("status", "VALID")
                val status = try {
                    ScanStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    ScanStatus.VALID
                }
                list.add(
                    ScanItem(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        attendeeName = obj.optString("attendeeName", ""),
                        ticketCode = obj.optString("ticketCode", ""),
                        time = obj.optString("time", ""),
                        status = status,
                        isSynced = obj.optBoolean("isSynced", true),
                        event = obj.optString("event", "")
                    )
                )
            }
        } catch (ignored: Exception) {
        }
        return list
    }

    fun saveDarkMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun loadDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun saveFastMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FAST_MODE, enabled).apply()
    }

    fun loadFastMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FAST_MODE, false)
    }

    fun saveDefaultZoom(context: Context, zoom: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_DEFAULT_ZOOM, zoom).apply()
    }

    fun loadDefaultZoom(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_DEFAULT_ZOOM, 1.0f)
    }

    fun saveAutoSyncEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    fun loadAutoSyncEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_SYNC, true)
    }

    fun saveSoundVibrationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SOUND_VIBRATION, enabled).apply()
    }

    fun loadSoundVibrationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SOUND_VIBRATION, true)
    }

    fun savePushNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, enabled).apply()
    }

    fun loadPushNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true)
    }
}
