package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TripEntity::class,
        LgaZoneEntity::class,
        LedgerEntity::class,
        DisputeEntity::class,
        OfflineQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KekeGoDatabase : RoomDatabase() {
    abstract fun kekeGoDao(): KekeGoDao

    companion object {
        @Volatile
        private var INSTANCE: KekeGoDatabase? = null

        fun getInstance(context: Context): KekeGoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KekeGoDatabase::class.java,
                    "kekego_offline_transit.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
