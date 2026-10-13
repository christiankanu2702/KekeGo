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

    // Bolt Additions: Saved Places
    @Query("SELECT * FROM saved_places WHERE userId = :userId")
    fun getSavedPlacesFlow(userId: String): Flow<List<SavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlace(place: SavedPlaceEntity)

    @Delete
    suspend fun deleteSavedPlace(place: SavedPlaceEntity)

    // Bolt Additions: Scheduled Rides
    @Query("SELECT * FROM scheduled_rides WHERE userId = :userId ORDER BY pickupEpoch ASC")
    fun getScheduledRidesFlow(userId: String): Flow<List<ScheduledRideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledRide(ride: ScheduledRideEntity)

    @Query("UPDATE scheduled_rides SET status = :newStatus WHERE id = :rideId")
    suspend fun updateScheduledRideStatus(rideId: String, newStatus: String)

    @Query("DELETE FROM scheduled_rides WHERE id = :rideId")
    suspend fun deleteScheduledRide(rideId: String)

    // Bolt Additions: Trip Ratings
    @Query("SELECT * FROM trip_ratings WHERE tripId = :tripId")
    suspend fun getTripRating(tripId: String): TripRatingEntity?

    @Query("SELECT * FROM trip_ratings WHERE passengerId = :userId")
    fun getUserGivenRatingsFlow(userId: String): Flow<List<TripRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripRating(rating: TripRatingEntity)

    // Bolt Additions: Device Telemetry Audit
    @Query("SELECT * FROM device_telemetry_logs ORDER BY capturedAtEpoch DESC LIMIT 50")
    fun getAllTelemetryLogsFlow(): Flow<List<DeviceTelemetryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetryLog(log: DeviceTelemetryEntity)
}
