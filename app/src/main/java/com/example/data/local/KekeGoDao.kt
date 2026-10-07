package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KekeGoDao {

    // Trips
    @Query("SELECT * FROM cached_trips ORDER BY createdAtEpoch DESC")
    fun getAllCachedTripsFlow(): Flow<List<TripEntity>>

    @Query("SELECT * FROM cached_trips WHERE passengerId = :userId OR driverId = :userId ORDER BY createdAtEpoch DESC")
    fun getUserTripsFlow(userId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM cached_trips WHERE id = :tripId")
    suspend fun getTripById(tripId: String): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    // LGA Zones
    @Query("SELECT * FROM cached_lga_zones")
    fun getAllLgaZonesFlow(): Flow<List<LgaZoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLgaZones(zones: List<LgaZoneEntity>)

    // Ledger
    @Query("SELECT * FROM cached_ledger WHERE userId = :userId ORDER BY timestampEpoch DESC")
    fun getLedgerFlow(userId: String): Flow<List<LedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: LedgerEntity)

    // Disputes
    @Query("SELECT * FROM cached_disputes ORDER BY timestampEpoch DESC")
    fun getAllDisputesFlow(): Flow<List<DisputeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: DisputeEntity)

    // Offline Queue
    @Query("SELECT * FROM offline_sync_queue ORDER BY queueId ASC")
    suspend fun getPendingSyncQueue(): List<OfflineQueueEntity>

    @Insert
    suspend fun enqueueOfflineAction(action: OfflineQueueEntity)

    @Delete
    suspend fun removeOfflineAction(action: OfflineQueueEntity)

    @Query("DELETE FROM offline_sync_queue WHERE queueId = :queueId")
    suspend fun deleteQueueItem(queueId: Long)
}
