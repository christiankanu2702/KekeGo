package com.example.data.models

enum class ServiceMode {
    PASSENGER,
    WAYBILL,
    DAY_HIRE
}

enum class TripStatus {
    REQUESTED,
    BIDDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

data class Trip(
    val id: String = "",
    val passengerId: String = "",
    val passengerName: String = "Commuter",
    val passengerPhone: String = "08012345678",
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val tricycleRegNo: String? = null,
    val serviceMode: ServiceMode = ServiceMode.PASSENGER,
    val status: TripStatus = TripStatus.REQUESTED,
    val originLgaId: String = "aba_south",
    val originMarket: String = "Bata Junction",
    val destinationLgaId: String = "aba_north",
    val destinationMarket: String = "Ariaria International Market",
    val intermediateStops: List<String> = emptyList(),
    val proposedFareNaira: Int = 500,
    val finalAgreedFareNaira: Int = 500,
    val cargoSacks: Int = 0,
    val cargoBasins: Int = 0,
    val passengerSeatsOccupied: Int = 1,
    val isWaitAndReturn: Boolean = false,
    val waitMinutes: Int = 0,
    val waybillRecipientName: String = "",
    val waybillRecipientPhone: String = "",
    val waybillOtp: String = "",
    val isOtpVerified: Boolean = false,
    val isRecurring: Boolean = false,
    val scheduledTimeEpoch: Long? = null,
    val isPaidDigitally: Boolean = false,
    val paymentRef: String? = null,
    val createdAtEpoch: Long = System.currentTimeMillis()
)

data class BidOffer(
    val id: String = "",
    val tripId: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val driverRating: Double = 4.8,
    val tricyclePlate: String = "ABA-419-KU",
    val counterFareNaira: Int = 600,
    val etaMinutes: Int = 4,
    val createdAtEpoch: Long = System.currentTimeMillis()
)

object CargoCalculator {
    // Standard Keke Napep has 3 passenger seats in the rear
    fun calculateAvailableSeats(sacks: Int, basins: Int): Int {
        val cargoPenalty = (sacks * 1) + (basins / 2)
        val remaining = 3 - cargoPenalty
        return remaining.coerceIn(0, 3)
    }

    fun calculateRecommendedFare(baseFare: Int, sacks: Int, basins: Int, waitMins: Int): Int {
        val cargoSurcharge = (sacks * 150) + (basins * 100)
        val waitSurcharge = waitMins * 30 // 30 Naira per minute waiting fee
        return baseFare + cargoSurcharge + waitSurcharge
    }
}
