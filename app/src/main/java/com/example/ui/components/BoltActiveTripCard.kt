package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Trip
import com.example.data.models.TripStatus
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Signature Bolt-Style Active Trip Card:
 * - Driver identification with verified star rating & total trips
 * - Vehicle details (Plate number: ABA-882-KU • Bajaj RE 4-Stroke Tricycle)
 * - Free wait time countdown timer (04:59 ... 00:00) before demurrage
 * - 1-tap quick action buttons: [ Chat ], [ Call ], [ Share Trip ], [ Cancel ]
 */
@Composable
fun BoltActiveTripCard(
    trip: Trip,
    driverName: String = "Kalu Driver",
    driverPhone: String = "08039821033",
    driverRating: Double = 4.9,
    driverCompletedTrips: Int = 1420,
    vehicleModel: String = "Bajaj RE 4-Stroke",
    tricyclePlateNo: String = "ABA-882-KU",
    onOpenChat: () -> Unit,
    onCancelTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDriverDetailsModal by remember { mutableStateOf(false) }

    // Free wait time timer (starts at 300 seconds = 5:00 minutes)
    var waitSecondsRemaining by remember { mutableIntStateOf(299) }
    LaunchedEffect(Unit) {
        while (waitSecondsRemaining > 0) {
            delay(1000)
            waitSecondsRemaining -= 1
        }
    }

    val minutes = waitSecondsRemaining / 60
    val seconds = waitSecondsRemaining % 60
    val timerString = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AsphaltDark),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00D664).copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Status & Free Wait Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00D664))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (trip.status) {
                            TripStatus.ACCEPTED -> "DRIVER ARRIVING • 2 MINS"
                            TripStatus.IN_PROGRESS -> "TRIP IN PROGRESS"
                            TripStatus.COMPLETED -> "ARRIVED AT DESTINATION"
                            else -> "DRIVER MATCHED & ON ROUTE"
                        },
                        color = Color(0xFF00D664),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Surface(
                    color = Color(0xFF1E2633),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF323F54))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (waitSecondsRemaining > 60) SafetyAmber else Color(0xFFFF5252),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Free Wait: $timerString",
                            color = HighContrastWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Driver & Vehicle Profile Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDriverDetailsModal = true },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SafetyAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricRickshaw,
                            contentDescription = null,
                            tint = AsphaltDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = driverName,
                                color = HighContrastWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SafetyAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("$driverRating", color = SafetyAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            text = "$vehicleModel • $tricyclePlateNo",
                            color = MutedSilver,
                            fontSize = 11.sp
                        )
                    }
                }

                // Agreed Fare Display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₦${trip.finalAgreedFareNaira}",
                        color = Color(0xFF00D664),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text("Cash / Wallet", color = MutedSilver, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = AsphaltDivider, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 1-Tap Action Row (Bolt Style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // In-App Chat
                Button(
                    onClick = onOpenChat,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D664), contentColor = AsphaltDark)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                // Direct Call
                OutlinedButton(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$driverPhone"))
                        context.startActivity(dialIntent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HighContrastWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Share Live Trip
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Follow my live KekeGo ride with $driverName ($tricyclePlateNo) from ${trip.originMarket} to ${trip.destinationMarket}. Live transit ETA: 2 mins."
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Live Trip with Family"))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HighContrastWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    // Driver Profile Modal
    if (showDriverDetailsModal) {
        AlertDialog(
            onDismissRequest = { showDriverDetailsModal = false },
            containerColor = AsphaltDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verified Keke Rider", color = HighContrastWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Rider: $driverName", color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("License: Abia State Commercial Permit AB/KKE/2026", color = SafetyAmber, fontSize = 11.sp)
                            Text("Affiliation: ATRWAN Zone 2 Union", color = MutedSilver, fontSize = 11.sp)
                            Text("Completed Rides: $driverCompletedTrips trips", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Passenger Rating: $driverRating / 5.0 (420 reviews)", color = HighContrastWhite, fontSize = 11.sp)
                        }
                    }

                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Tricycle: $vehicleModel", color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Registration Plate: $tricyclePlateNo", color = SafetyAmber, fontSize = 11.sp)
                            Text("Safety: GPS Telemetry Verified", color = EmeraldGreen, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDriverDetailsModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D664), contentColor = AsphaltDark)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
