package com.example.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import java.util.UUID

data class DeviceTelemetryInfo(
    val deviceHardwareId: String,       // Android ID + Persistent UUID
    val androidId: String,              // Settings.Secure.ANDROID_ID
    val manufacturer: String,           // e.g. "Tecno", "Infinix", "Samsung"
    val model: String,                  // e.g. "Spark 10", "Galaxy A14"
    val osVersion: String,              // e.g. "Android 14 (API 34)"
    val batteryPercent: Int,            // e.g. 82%
    val isCharging: Boolean,            // true / false
    val networkType: String,            // "4G / LTE", "3G", "WiFi", "Offline"
    val isMockLocationSpoofed: Boolean, // Anti-cheat flag
    val screenResolution: String,       // e.g. "1080x2400"
    val memoryEstimateMb: Long,         // Runtime free memory
    val capturedAtEpoch: Long = System.currentTimeMillis()
)

object DeviceTelemetryTracker {
    private const val PREF_NAME = "kekego_telemetry_prefs"
    private const val KEY_INSTALL_UUID = "device_install_uuid"

    fun captureCurrentTelemetry(context: Context, isLocationMocked: Boolean = false): DeviceTelemetryInfo {
        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var installUuid = sharedPrefs.getString(KEY_INSTALL_UUID, null)
        if (installUuid == null) {
            installUuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString(KEY_INSTALL_UUID, installUuid).apply()
        }

        val rawAndroidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_ANDROID_ID"
        } catch (_: Exception) {
            "UNKNOWN_ANDROID_ID"
        }

        val compositeHardwareId = "DEV_${rawAndroidId.take(8).uppercase()}_${installUuid.take(6).uppercase()}"

        // Battery Telemetry
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 85
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Network Connectivity Telemetry
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val networkType = when {
            caps == null -> "Offline / No Signal"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "High-Speed WiFi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular (4G / LTE MTN/Airtel)"
            else -> "Cellular Mobile Data"
        }

        // Screen Metrics
        val displayMetrics = context.resources.displayMetrics
        val resolution = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels} (${displayMetrics.densityDpi} dpi)"

        val freeMemMb = Runtime.getRuntime().freeMemory() / (1024 * 1024)

        return DeviceTelemetryInfo(
            deviceHardwareId = compositeHardwareId,
            androidId = rawAndroidId,
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            batteryPercent = batteryPct,
            isCharging = isCharging,
            networkType = networkType,
            isMockLocationSpoofed = isLocationMocked,
            screenResolution = resolution,
            memoryEstimateMb = freeMemMb,
            capturedAtEpoch = System.currentTimeMillis()
        )
    }
}
