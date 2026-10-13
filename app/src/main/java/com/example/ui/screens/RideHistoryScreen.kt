package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ScheduledRide
import com.example.data.models.Trip
import com.example.data.models.TripStatus
import com.example.data.repository.KekeGoRepository
import com.example.ui.components.BoltRatingDialog
import com.example.ui.theme.*
import com.example.utils.DeviceTelemetryTracker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen(
    repository: KekeGoRepository,
    onRebookTrip: (originMarket: String, destMarket: String, fare: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Past Rides, 1 = Scheduled
    val cachedTrips by repository.getCachedTripsFlow().collectAsState(initial = emptyList())
    val scheduledRides by repository.getScheduledRidesFlow().collectAsState(initial = emptyList())

    // Receipt modal state
    var selectedTripForReceipt by remember { mutableStateOf<Trip?>(null) }
    // Rating modal state
    var selectedTripForRating by remember { mutableStateOf<Trip?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AsphaltBlack)
    ) {
        // Top Header
        Surface(
            color = AsphaltDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rides & Activity",
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Itemized digital receipts, telemetry & bookings",
                            color = MutedSilver,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SafetyAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bolt Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AsphaltCard,
                    contentColor = SafetyAmber,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = SafetyAmber,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Past Rides (${cachedTrips.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) SafetyAmber else MutedSilver
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Scheduled (${scheduledRides.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) SafetyAmber else MutedSilver
                            )
                        }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            if (cachedTrips.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MutedSilver,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No past rides found", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                        Text("Trips you take in Abia State will appear here", color = MutedSilver, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cachedTrips) { trip ->
                        PastTripCard(
                            trip = trip,
                            onViewReceipt = { selectedTripForReceipt = trip },
                            onRateDriver = { selectedTripForRating = trip },
                            onRebook = {
                                onRebookTrip(trip.originMarket, trip.destinationMarket, trip.finalAgreedFareNaira)
                            }
                        )
                    }
                }
            }
        } else {
            if (scheduledRides.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MutedSilver,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No scheduled rides", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                        Text("Book in advance from the main screen", color = MutedSilver, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(scheduledRides) { sched ->
                        ScheduledRideCard(
                            scheduledRide = sched,
                            onCancel = {
                                coroutineScope.launch {
                                    repository.cancelScheduledRide(sched.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Receipt Dialog with Hardware Telemetry Footprint
    selectedTripForReceipt?.let { trip ->
        DigitalReceiptDialog(
            trip = trip,
            onDismiss = { selectedTripForReceipt = null }
        )
    }

    // Rating Dialog
    selectedTripForRating?.let { trip ->
        BoltRatingDialog(
            tripId = trip.id,
            driverName = trip.driverName ?: "Kalu Driver",
            tricyclePlate = trip.tricycleRegNo ?: "ABA-882-KU",
            onDismiss = { selectedTripForRating = null },
            onSubmitRating = { stars, tags, comment, tip ->
                coroutineScope.launch {
                    repository.submitTripRating(
                        tripId = trip.id,
                        driverId = trip.driverId,
                        ratingStars = stars,
                        complimentTags = tags,
                        comment = comment,
                        tipNaira = tip
                    )
                    selectedTripForRating = null
                }
            }
        )
    }
}

@Composable
fun PastTripCard(
    trip: Trip,
    onViewReceipt: () -> Unit,
    onRateDriver: () -> Unit,
    onRebook: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(trip.createdAtEpoch))

    Card(
        colors = CardDefaults.cardColors(containerColor = AsphaltCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SafetyAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = SafetyAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = trip.serviceMode.name + " • " + (trip.driverName ?: "Commercial Rider"),
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(text = dateStr, color = MutedSilver, fontSize = 10.sp)
                    }
                }

                Text(
                    text = "₦${trip.finalAgreedFareNaira}",
                    color = SafetyAmber,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = AsphaltDivider, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Route representation
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TripOrigin, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(trip.originMarket, color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(trip.destinationMarket, color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewReceipt,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HighContrastWhite)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRateDriver,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyAmber)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRebook,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark)
                ) {
                    Text("Rebook", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ScheduledRideCard(
    scheduledRide: ScheduledRide,
    onCancel: () -> Unit
) {
    val dateStr = SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault()).format(Date(scheduledRide.pickupEpoch))

    Card(
        colors = CardDefaults.cardColors(containerColor = AsphaltCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SafetyAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (scheduledRide.isRecurring) "RECURRING • ${scheduledRide.recurringRule}" else "ONE-TIME PICKUP",
                        color = SafetyAmber,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Est. ₦${scheduledRide.estimatedFareNaira}",
                    color = HighContrastWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = dateStr, color = HighContrastWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TripOrigin, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(scheduledRide.originMarket, color = HighContrastWhite, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(scheduledRide.destinationMarket, color = HighContrastWhite, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (scheduledRide.status == "ACTIVE" || scheduledRide.status == "SCHEDULED") {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel Scheduled Booking", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "Status: ${scheduledRide.status}",
                    color = MutedSilver,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DigitalReceiptDialog(
    trip: Trip,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val telemetry = remember { DeviceTelemetryTracker.captureCurrentTelemetry(context) }
    val dateStr = SimpleDateFormat("EEEE, dd MMMM yyyy • hh:mm a", Locale.getDefault()).format(Date(trip.createdAtEpoch))
    val baseFare = (trip.finalAgreedFareNaira * 0.7).toInt()
    val cargoFee = (trip.cargoSacks * 150) + (trip.cargoBasins * 100)
    val waitFee = trip.waitMinutes * 30
    val corridorSurcharge = trip.finalAgreedFareNaira - baseFare - cargoFee - waitFee

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("KekeGo Transit Receipt", color = HighContrastWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(dateStr, color = MutedSilver, fontSize = 10.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = AsphaltCard,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Trip #${trip.id}", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Rider: ${trip.driverName ?: "Chinedu Rider"} (${trip.tricycleRegNo ?: "ABA-882-KU"})", color = HighContrastWhite, fontSize = 12.sp)
                        Text("Route: ${trip.originMarket} ➔ ${trip.destinationMarket}", color = MutedSilver, fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = AsphaltDivider, thickness = 1.dp)

                // Itemized breakdown
                ReceiptLine("Base Distance Fare", "₦$baseFare")
                if (corridorSurcharge > 0) {
                    ReceiptLine("Corridor Crossing Charge", "₦$corridorSurcharge")
                }
                if (cargoFee > 0) {
                    ReceiptLine("Volumetric Cargo (${trip.cargoSacks} sacks, ${trip.cargoBasins} basins)", "₦$cargoFee")
                }
                if (waitFee > 0) {
                    ReceiptLine("Waiting Time (${trip.waitMinutes} mins)", "₦$waitFee")
                }
                ReceiptLine("Abia State Transport Levy", "INCLUDED")

                HorizontalDivider(color = AsphaltDivider, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Paid", color = HighContrastWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("₦${trip.finalAgreedFareNaira}", color = EmeraldGreen, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }

                // Bolt Verified Hardware Telemetry Badge
                Surface(
                    color = AsphaltSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VERIFIED DEVICE TELEMETRY AUDIT", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Device Hardware ID: ${telemetry.deviceHardwareId}", color = MutedSilver, fontSize = 9.sp)
                        Text("Model: ${telemetry.manufacturer} ${telemetry.model} (${telemetry.osVersion})", color = HighContrastWhite, fontSize = 10.sp)
                        Text("Battery: ${telemetry.batteryPercent}% • Network: ${telemetry.networkType}", color = SafetyAmber, fontSize = 9.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun ReceiptLine(title: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = MutedSilver, fontSize = 12.sp)
        Text(amount, color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
