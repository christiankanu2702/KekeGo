package com.example.data.firebase

import android.content.Context
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    fun getAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? = getAuth().currentUser

    fun getFirestore(context: Context): FirebaseFirestore {
        val databaseId = context.getString(R.string.firestore_database_id)
        val app = FirebaseApp.getInstance()
        return FirebaseFirestore.getInstance(app, databaseId)
    }
}
