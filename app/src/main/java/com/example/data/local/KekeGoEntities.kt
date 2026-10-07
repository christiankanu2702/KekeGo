package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val passengerId: String,
    val passengerName: String,
    val passengerPhone: String,
    val driverId: String?,
    val driverName: String?,
    val driverPhone: String?,
    val tricycleRegNo: String?,
    val serviceMode: String,
    val status: String,
    val originLgaId: String,
    val originMarket: String,
    val destinationLgaId: String,
    val destinationMarket: String,
    val intermediateStopsRaw: String, // comma separated
    val proposedFareNaira: Int,
    val finalAgreedFareNaira: Int,
    val cargoSacks: Int,
    val cargoBasins: Int,
    val isWaitAndReturn: Boolean,
    val waitMinutes: Int,
    val waybillRecipientName: String,
    val waybillRecipientPhone: String,
    val waybillOtp: String,
    val isOtpVerified: Boolean,
    val isPaidDigitally: Boolean,
    val paymentRef: String?,
    val createdAtEpoch: Long,
    val isSyncedWithCloud: Boolean = true
)

@Entity(tableName = "cached_lga_zones")
data class LgaZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val zoneCategory: String,
    val marketsRaw: String,
    val centerLat: Double,
    val centerLng: Double,
    val baseFareNaira: Int,
    val activeKekeFleetCount: Int
)

@Entity(tableName = "cached_ledger")
data class LedgerEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String,
    val amountNaira: Int,
    val referenceToken: String,
    val description: String,
    val timestampEpoch: Long
)

@Entity(tableName = "cached_disputes")
data class DisputeEntity(
    @PrimaryKey val id: String,
    val tripId: String,
    val reportedByUserId: String,
    val reporterName: String,
    val reasonCategory: String,
    val description: String,
    val locationLga: String,
    val timestampEpoch: Long,
    val status: String,
    val photoUriString: String? = null
)

@Entity(tableName = "offline_sync_queue")
data class OfflineQueueEntity(
    @PrimaryKey(autoGenerate = true) val queueId: Long = 0,
    val actionType: String, // CREATE_TRIP, ACCEPT_BID, COMPLETE_TRIP, DISPUTE_LOG, LEDGER_TRANSACTION
    val payloadJson: String,
    val createdAtEpoch: Long = System.currentTimeMillis()
)
