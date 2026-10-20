package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class KekeGoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                Log.i("KekeGoApp", "FirebaseApp initialized successfully: ${app?.name}")
                com.example.utils.CrashReportingTree.initialize()
            }
        } catch (e: Exception) {
            Log.e("KekeGoApp", "Failed to initialize FirebaseApp: ${e.message}", e)
        }
    }
}
