package com.example.data.models

data class TripChatMessage(
    val id: String = "",
    val tripId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: UserRole = UserRole.PASSENGER,
    val messageText: String = "",
    val timestampEpoch: Long = System.currentTimeMillis()
)
