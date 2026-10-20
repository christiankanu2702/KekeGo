package com.example.utils

import android.util.Log

object CrashReportingTree {
    private const val TAG = "KekeCrashlytics"

    fun initialize(userId: String? = null, role: String? = null) {
        Log.i(TAG, "Crash reporting tree initialized. Platform: Android Kotlin, Jurisdiction: Abia State 17 LGAs")
        if (userId != null) setUserId(userId)
        if (role != null) setCustomKey("user_role", role)
    }

    fun setUserId(userId: String) {
        Log.i(TAG, "Active user session: $userId")
    }

    fun setCustomKey(key: String, value: String) {
        Log.d(TAG, "Telemetry attribute [$key] = $value")
    }

    fun log(message: String) {
        Log.i(TAG, message)
    }

    fun logException(throwable: Throwable) {
        Log.e(TAG, "Captured Runtime Exception: ${throwable.message}", throwable)
    }
}
