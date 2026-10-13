package com.example.data.models

data class KekeFleetVehicle(
    val driverId: String,
    val driverName: String,
    val driverPhone: String,
    val tricyclePlateNo: String,
    val lgaId: String,
    val currentMarketLocation: String,
    val lat: Double,
    val lng: Double,
    val speedKmh: Int,
    val isOnline: Boolean,
    val currentTripStatus: String, // IDLE, EN_ROUTE_PICKUP, ON_TRIP, MAINTENANCE
    val ratingScore: Double,
    val totalTripsCompleted: Int,
    val isSuspended: Boolean = false,
    val activeWarningCount: Int = 0
)

data class SosEmergencyAlert(
    val id: String,
    val triggeredByUserId: String,
    val senderName: String,
    val senderPhone: String,
    val role: String,
    val locationLga: String,
    val lat: Double,
    val lng: Double,
    val timestampEpoch: Long,
    val status: String, // ACTIVE, DISPATCHED, RESOLVED
    val dispatchNote: String = ""
)

data class DisputeTicket(
    val id: String,
    val tripId: String,
    val passengerId: String,
    val passengerName: String,
    val driverId: String,
    val driverName: String,
    val category: String, // OVERCHARGING, RUDE_BEHAVIOR, WAYBILL_LOST, RECKLESS_DRIVING, ROUTE_DEVIATION
    val description: String,
    val amountInDisputeNaira: Int,
    val status: String, // OPEN, INVESTIGATING, REFUNDED, DISMISSED
    val createdAtEpoch: Long
)
