package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ServiceMode
import com.example.data.models.Trip
import com.example.data.models.TripStatus
import com.example.data.repository.KekeGoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class TripFilterTab(val title: String) {
    ALL("All Trips"),
    COMPLETED("Completed"),
    ACTIVE("Active / Transit"),
    WAYBILLS("Waybills")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastTripsLogSheet(
    repository: KekeGoRepository,
    onDismissRequest: () -> Unit,
    onBookAgain: ((Trip) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val trips by repository.getCachedTripsFlow().collectAsState(initial = emptyList())
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshNotice by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf(TripFilterTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTripDetails by remember { mutableStateOf<Trip?>(null) }

    // Filter trips
    val filteredTrips = remember(trips, selectedFilter, searchQuery) {
        trips.filter { trip ->
            val matchesFilter = when (selectedFilter) {
                TripFilterTab.ALL -> true
                TripFilterTab.COMPLETED -> trip.status == TripStatus.COMPLETED
                TripFilterTab.ACTIVE -> trip.status == TripStatus.REQUESTED ||
                        trip.status == TripStatus.BIDDING ||
                        trip.status == TripStatus.ACCEPTED ||
                        trip.status == TripStatus.IN_PROGRESS
                TripFilterTab.WAYBILLS -> trip.serviceMode == ServiceMode.WAYBILL
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                trip.originMarket.lowercase().contains(q) ||
                        trip.destinationMarket.lowercase().contains(q) ||
                        trip.id.lowercase().contains(q) ||
                        trip.passengerName.lowercase().contains(q) ||
                        (trip.driverName?.lowercase()?.contains(q) == true) ||
                        (trip.tricycleRegNo?.lowercase()?.contains(q) == true)
            }

            matchesFilter && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = AsphaltDark,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MutedSilver.copy(alpha = 0.5f))
                )
            }
        },
        modifier = modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SafetyAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Past Trips Log",
                            tint = SafetyAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Past Tricycle Trips",
                                style = MaterialTheme.typography.titleLarge,
                                color = HighContrastWhite,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = EmeraldGreenDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "FIRESTORE SYNCED",
                                    color = HighContrastWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Abia State Keke Transit Ledger (${trips.size} total trips logged)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            isRefreshing = true
                            val count = repository.syncPastTripsFromFirestore()
                            isRefreshing = false
                            refreshNotice = "Synced $count trips from Firestore"
                        }
                    },
                    modifier = Modifier.testTag("refresh_trips_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SafetyAmber,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Refresh from Firestore",
                            tint = SafetyAmber
                        )
                    }
                }
            }

            AnimatedVisibility(visible = refreshNotice != null) {
                Surface(
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = EmeraldGreenLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(refreshNotice ?: "", color = EmeraldGreenLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { refreshNotice = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MutedSilver, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by market, corridor, driver or Trip ID...", color = MutedSilver, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MutedSilver, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MutedSilver, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = HighContrastWhite,
                    unfocusedTextColor = HighContrastWhite,
                    focusedBorderColor = SafetyAmber,
                    unfocusedBorderColor = AsphaltDivider,
                    focusedContainerColor = AsphaltSurface,
                    unfocusedContainerColor = AsphaltSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trips_search_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TripFilterTab.entries) { tab ->
                    FilterChip(
                        selected = selectedFilter == tab,
                        onClick = { selectedFilter = tab },
                        label = {
                            val count = when (tab) {
                                TripFilterTab.ALL -> trips.size
                                TripFilterTab.COMPLETED -> trips.count { it.status == TripStatus.COMPLETED }
                                TripFilterTab.ACTIVE -> trips.count {
                                    it.status == TripStatus.REQUESTED ||
                                            it.status == TripStatus.BIDDING ||
                                            it.status == TripStatus.ACCEPTED ||
                                            it.status == TripStatus.IN_PROGRESS
                                }
                                TripFilterTab.WAYBILLS -> trips.count { it.serviceMode == ServiceMode.WAYBILL }
                            }
                            Text("${tab.title} ($count)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SafetyAmber,
                            selectedLabelColor = AsphaltDark,
                            containerColor = AsphaltCard,
                            labelColor = HighContrastWhite
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Empty state or List of Trips
            if (filteredTrips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AsphaltCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsTransitFilled,
                                contentDescription = null,
                                tint = MutedSilver,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No trips match \"$searchQuery\"" else "No trips recorded in this view",
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New ride requests and completed waybills will automatically record to Firestore and cache locally.",
                            color = MutedSilver,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("past_trips_lazy_column"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredTrips, key = { it.id }) { trip ->
                        PastTripCard(
                            trip = trip,
                            onClick = { selectedTripDetails = trip },
                            onBookAgain = onBookAgain
                        )
                    }
                }
            }
        }
    }

    // Trip Detail Modal Dialog
    if (selectedTripDetails != null) {
        val trip = selectedTripDetails!!
        TripDetailDialog(
            trip = trip,
            onDismiss = { selectedTripDetails = null },
            onBookAgain = {
                onBookAgain?.invoke(trip)
                selectedTripDetails = null
                onDismissRequest()
            }
        )
    }
}

@Composable
fun PastTripCard(
    trip: Trip,
    onClick: () -> Unit,
    onBookAgain: ((Trip) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(trip.createdAtEpoch) {
        dateFormatter.format(Date(trip.createdAtEpoch))
    }

    Surface(
        color = AsphaltSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (trip.status) {
                TripStatus.COMPLETED -> EmeraldGreen.copy(alpha = 0.4f)
                TripStatus.IN_PROGRESS -> SafetyAmber.copy(alpha = 0.7f)
                TripStatus.ACCEPTED -> EmeraldGreen.copy(alpha = 0.6f)
                TripStatus.CANCELLED -> ErrorRed.copy(alpha = 0.4f)
                else -> AsphaltDivider
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("trip_card_${trip.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Status, Service Mode, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Service Mode Tag
                    Surface(
                        color = when (trip.serviceMode) {
                            ServiceMode.PASSENGER -> SafetyAmber.copy(alpha = 0.2f)
                            ServiceMode.WAYBILL -> EmeraldGreen.copy(alpha = 0.2f)
                            ServiceMode.DAY_HIRE -> Color(0xFF64B5F6).copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (trip.serviceMode) {
                                    ServiceMode.PASSENGER -> Icons.Default.ElectricRickshaw
                                    ServiceMode.WAYBILL -> Icons.Default.Inventory2
                                    ServiceMode.DAY_HIRE -> Icons.Default.AccessTime
                                },
                                contentDescription = null,
                                tint = when (trip.serviceMode) {
                                    ServiceMode.PASSENGER -> SafetyAmber
                                    ServiceMode.WAYBILL -> EmeraldGreenLight
                                    ServiceMode.DAY_HIRE -> Color(0xFF90CAF9)
                                },
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trip.serviceMode.name,
                                color = when (trip.serviceMode) {
                                    ServiceMode.PASSENGER -> SafetyAmber
                                    ServiceMode.WAYBILL -> EmeraldGreenLight
                                    ServiceMode.DAY_HIRE -> Color(0xFF90CAF9)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formattedDate,
                        color = MutedSilver,
                        fontSize = 11.sp
                    )
                }

                // Status Badge
                Surface(
                    color = when (trip.status) {
                        TripStatus.COMPLETED -> EmeraldGreenDark
                        TripStatus.IN_PROGRESS -> SafetyAmberDark
                        TripStatus.ACCEPTED -> EmeraldGreenDark
                        TripStatus.CANCELLED -> ErrorRed.copy(alpha = 0.3f)
                        TripStatus.BIDDING -> SafetyAmber.copy(alpha = 0.2f)
                        TripStatus.REQUESTED -> AsphaltCard
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = trip.status.name,
                        color = when (trip.status) {
                            TripStatus.COMPLETED -> HighContrastWhite
                            TripStatus.IN_PROGRESS -> HighContrastWhite
                            TripStatus.ACCEPTED -> HighContrastWhite
                            TripStatus.CANCELLED -> ErrorRed
                            TripStatus.BIDDING -> SafetyAmber
                            TripStatus.REQUESTED -> MutedSilver
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Route: Origin -> Destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SafetyAmber)
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(20.dp)
                            .background(AsphaltDivider)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.originMarket,
                        color = HighContrastWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = trip.destinationMarket,
                        color = HighContrastWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Fare Column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₦${trip.finalAgreedFareNaira}",
                        color = SafetyAmber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (trip.isPaidDigitally) "Digital Paid" else "Cash/P2P",
                        color = if (trip.isPaidDigitally) EmeraldGreenLight else MutedSilver,
                        fontSize = 10.sp
                    )
                }
            }

            // Driver or Details Footer
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = AsphaltDivider.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MutedSilver,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trip.driverName ?: "Keke Rider Unassigned",
                        color = if (trip.driverName != null) HighContrastWhite else MutedSilver,
                        fontSize = 11.sp,
                        fontWeight = if (trip.driverName != null) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (trip.tricycleRegNo != null) {
                        Text(
                            text = " • ${trip.tricycleRegNo}",
                            color = SafetyAmberLight,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (trip.serviceMode == ServiceMode.WAYBILL && trip.waybillOtp.isNotBlank()) {
                        Surface(
                            color = EmeraldGreenDark,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "OTP: ${trip.waybillOtp}",
                                color = HighContrastWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (onBookAgain != null && trip.status == TripStatus.COMPLETED) {
                        TextButton(
                            onClick = { onBookAgain(trip) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Rebook", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Details",
                            tint = MutedSilver,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripDetailDialog(
    trip: Trip,
    onDismiss: () -> Unit,
    onBookAgain: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(trip.createdAtEpoch) {
        dateFormatter.format(Date(trip.createdAtEpoch))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trip Summary",
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "ID: ${trip.id}",
                        color = SafetyAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = when (trip.status) {
                        TripStatus.COMPLETED -> EmeraldGreenDark
                        TripStatus.IN_PROGRESS -> SafetyAmberDark
                        else -> AsphaltCard
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = trip.status.name,
                        color = HighContrastWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formattedDate,
                    color = MutedSilver,
                    fontSize = 11.sp
                )

                Divider(color = AsphaltDivider)

                // Route Segment
                Text(text = "Corridor Route:", color = MutedSilver, fontSize = 11.sp)
                Surface(
                    color = AsphaltCard,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SafetyAmber))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("From: ${trip.originMarket} (${trip.originLgaId})", color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (trip.intermediateStops.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            trip.intermediateStops.forEachIndexed { i, stop ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = MutedSilver, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop ${i + 1}: $stop", color = MutedSilver, fontSize = 11.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldGreen))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("To: ${trip.destinationMarket} (${trip.destinationLgaId})", color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Fare Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Final Agreed Fare:", color = MutedSilver, fontSize = 12.sp)
                    Text("₦${trip.finalAgreedFareNaira}", color = SafetyAmber, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                if (trip.cargoSacks > 0 || trip.cargoBasins > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cargo Volumetric:", color = MutedSilver, fontSize = 12.sp)
                        Text("${trip.cargoSacks} sacks, ${trip.cargoBasins} basins", color = HighContrastWhite, fontSize = 12.sp)
                    }
                }

                if (trip.driverName != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rider Assigned:", color = MutedSilver, fontSize = 12.sp)
                        Text("${trip.driverName} (${trip.tricycleRegNo ?: "Keke"})", color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (trip.serviceMode == ServiceMode.WAYBILL) {
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Waybill Security Verification", color = EmeraldGreenLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Recipient: ${trip.waybillRecipientName.ifBlank { "Registered Receiver" }} (${trip.waybillRecipientPhone})", color = HighContrastWhite, fontSize = 11.sp)
                            Text("Security OTP: ${trip.waybillOtp} (Verified: ${trip.isOtpVerified})", color = SafetyAmber, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onBookAgain,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark)
            ) {
                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Repeat This Trip", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MutedSilver)
            }
        }
    )
}
