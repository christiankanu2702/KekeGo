package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    fun ensureInitialized(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error initializing Firebase: ${e.message}")
        }
    }

    fun getAuth(): FirebaseAuth {
        return try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            // Fallback: initialize default app if not already initialized
            FirebaseApp.getInstance()
            FirebaseAuth.getInstance()
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }
    }

    fun getFirestore(context: Context): FirebaseFirestore {
        ensureInitialized(context)
        return try {
            val databaseId = context.getString(R.string.firestore_database_id)
            val app = FirebaseApp.getInstance()
            FirebaseFirestore.getInstance(app, databaseId)
        } catch (_: Exception) {
            FirebaseFirestore.getInstance()
        }
    }
}
