package com.example.data.models

data class SavedPlace(
    val id: String,
    val userId: String,
    val title: String,
    val addressOrMarket: String,
    val lgaId: String,
    val lat: Double,
    val lng: Double,
    val iconType: String // HOME, WORK, MARKET, CUSTOM
)

data class ScheduledRide(
    val id: String,
    val userId: String,
    val originMarket: String,
    val originLgaId: String,
    val destinationMarket: String,
    val destinationLgaId: String,
    val pickupEpoch: Long,
    val isRecurring: Boolean,
    val recurringRule: String, // DAILY, WEEKDAYS, WEEKLY
    val serviceMode: ServiceMode,
    val estimatedFareNaira: Int,
    val status: String
)

data class TripRating(
    val id: String,
    val tripId: String,
    val passengerId: String,
    val driverId: String?,
    val ratingStars: Int,
    val complimentTags: List<String>,
    val comment: String,
    val tipNaira: Int,
    val createdAtEpoch: Long
)

data class ItemizedReceipt(
    val tripId: String,
    val baseFareNaira: Int,
    val distanceSurchargeNaira: Int,
    val cargoSurchargeNaira: Int,
    val waitingFeeNaira: Int,
    val discountPromoNaira: Int,
    val totalPaidNaira: Int,
    val paymentMethod: String,
    val paymentRef: String?,
    val driverName: String,
    val tricyclePlate: String,
    val completedAtEpoch: Long
)
