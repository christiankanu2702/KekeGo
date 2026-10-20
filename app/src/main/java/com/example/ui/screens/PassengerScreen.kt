package com.example.ui.screens
import com.example.ui.components.BoltRideMapView
import com.example.ui.components.BoltRatingDialog
import com.example.ui.components.BoltRideTierSelector
import com.example.ui.components.BoltRideTier
import com.example.ui.components.BoltSavedPlacesRow
import com.example.ui.components.BoltActiveTripCard

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.data.repository.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.components.GeminiWaybillAssistantDialog
import androidx.compose.material.icons.filled.AutoAwesome
import com.example.utils.LocationTracker
import com.example.utils.SmsHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerScreen(
    repository: KekeGoRepository,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userAccount by repository.currentUserAccount.collectAsState()
    val activeBids by repository.activeBids.collectAsState()
    val cachedTrips by repository.getCachedTripsFlow().collectAsState(initial = emptyList())
    val savedPlaces by repository.getSavedPlacesFlow().collectAsState(initial = emptyList())

    // Live Device GPS status
    var liveLat by remember { mutableDoubleStateOf(5.1118) }
    var liveLng by remember { mutableDoubleStateOf(7.3689) }
    var isLiveGpsLocked by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            coroutineScope.launch {
                val loc = LocationTracker.getLiveDeviceLocation(context)
                if (loc != null) {
                    liveLat = loc.latitude
                    liveLng = loc.longitude
                    isLiveGpsLocked = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (LocationTracker.hasLocationPermission(context)) {
            val loc = LocationTracker.getLiveDeviceLocation(context)
            if (loc != null) {
                liveLat = loc.latitude
                liveLng = loc.longitude
                isLiveGpsLocked = true
            }
        }
        // Seed default saved places if empty
        launch {
            if (savedPlaces.isEmpty()) {
                repository.addOrUpdateSavedPlace("Home", "Bata Junction", "aba_south", 5.1118, 7.3689, "HOME")
                repository.addOrUpdateSavedPlace("Work", "Ariaria International Market", "aba_north", 5.1320, 7.3480, "WORK")
                repository.addOrUpdateSavedPlace("Ahia Ohuru", "New Market Road", "aba_south", 5.1050, 7.3620, "MARKET")
            }
        }
    }

    // Service Mode Switcher
    var selectedServiceMode by remember { mutableStateOf(ServiceMode.PASSENGER) }

    // Route locations
    var selectedOriginLga by remember { mutableStateOf("aba_south") }
    var selectedOriginMarket by remember { mutableStateOf("Bata Junction") }
    var selectedDestLga by remember { mutableStateOf("aba_north") }
    var selectedDestMarket by remember { mutableStateOf("Ariaria International Market") }

    // Intermediate stops & wait-and-return
    var intermediateStops by remember { mutableStateOf(listOf<String>()) }
    var newStopInput by remember { mutableStateOf("") }
    var isWaitAndReturn by remember { mutableStateOf(false) }
    var waitMinutes by remember { mutableIntStateOf(15) }

    // Volumetric cargo
    var cargoSacks by remember { mutableIntStateOf(0) }
    var cargoBasins by remember { mutableIntStateOf(0) }

    // P2P Fare Proposal
    var proposedFareNaira by remember { mutableIntStateOf(500) }

    // Waybill specifics
    var recipientName by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("") }

    // Scheduled ride
    var isRecurringDaily by remember { mutableStateOf(false) }

    // Dialog state
    var showBookingSuccessDialog by remember { mutableStateOf<String?>(null) }
    var showPaystackDialog by remember { mutableStateOf(false) }
    var showRequestBottomSheet by remember { mutableStateOf(false) }
    var showAddSavedPlaceDialog by remember { mutableStateOf(false) }
    var showScheduleModal by remember { mutableStateOf(false) }
    var showTripChatDialog by remember { mutableStateOf(false) }
    var showGeminiAssistantDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var completedTripToRate by remember { mutableStateOf<Trip?>(null) }
    var selectedRideTier by remember { mutableStateOf(BoltRideTier.STANDARD) }
    var scheduledSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Recalculate proposed fare baseline when cargo changes
    LaunchedEffect(cargoSacks, cargoBasins, isWaitAndReturn, waitMinutes) {
        val calculated = CargoCalculator.calculateRecommendedFare(
            baseFare = 400,
            sacks = cargoSacks,
            basins = cargoBasins,
            waitMins = if (isWaitAndReturn) waitMinutes else 0
        )
        proposedFareNaira = calculated
    }

    Scaffold(
        containerColor = AsphaltBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AsphaltDark,
                    titleContentColor = HighContrastWhite
                ),
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KekeGo Passenger",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = SafetyAmber
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = EmeraldGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "KEKEGO ACTIVE",
                                    color = EmeraldGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Abia State Tricycle Transit & Cargo Network",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                },
                actions = {
                    // Paystack digital wallet shortcut
                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { onNavigateToWallet() }
                            .padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = SafetyAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "₦${userAccount?.walletBalanceNaira ?: 0}",
                                color = HighContrastWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Bolt In-App Trip Chat & Call Trigger
                    Surface(
                        color = AsphaltCard,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable { showTripChatDialog = true }
                            .padding(end = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Trip Chat",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // SOS Floating Emergency Trigger
                    SosFloatingTrigger(
                        onTriggerSos = {
                            coroutineScope.launch {
                                repository.triggerEmergencySos(5.1118, 7.3689, selectedOriginLga)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Bolt Quick Saved Places Row
            item {
                Spacer(modifier = Modifier.height(2.dp))
                SavedPlacesQuickRow(
                    savedPlaces = savedPlaces,
                    onSelectPlace = { place ->
                        selectedDestMarket = place.addressOrMarket
                        selectedDestLga = place.lgaId
                    },
                    onAddPlaceClick = { showAddSavedPlaceDialog = true }
                )
            }

            item {
                BoltSavedPlacesRow(
                    onSelectPlace = { market, lga ->
                        selectedDestMarket = market
                        selectedDestLga = lga
                    }
                )
            }

            item {
                val activeAssignedTrip = cachedTrips.firstOrNull { it.status == TripStatus.ACCEPTED || it.status == TripStatus.IN_PROGRESS }
                BoltRideMapView(
                    originMarket = selectedOriginMarket,
                    originLgaId = selectedOriginLga,
                    destinationMarket = selectedDestMarket,
                    destinationLgaId = selectedDestLga,
                    intermediateStops = intermediateStops,
                    userLiveLat = if (isLiveGpsLocked) liveLat else null,
                    userLiveLng = if (isLiveGpsLocked) liveLng else null,
                    activeDriverLat = activeAssignedTrip?.driverLiveLat,
                    activeDriverLng = activeAssignedTrip?.driverLiveLng,
                    activeDriverPlate = activeAssignedTrip?.tricycleRegNo,
                    onMarketSelected = { selectedDestMarket = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                BoltRideTierSelector(
                    baseFareNaira = proposedFareNaira,
                    selectedTier = selectedRideTier,
                    onTierSelected = { selectedRideTier = it }
                )

                // Gemini AI Waybill & Fare Valuator Trigger
                Surface(
                    color = Color(0xFF1E142B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8A2BE2).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFBA68C8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Gemini AI Cargo Valuator", color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Auto-estimate freight volume & fair market price", color = Color(0xFFBA68C8), fontSize = 10.sp)
                            }
                        }
                        Button(
                            onClick = { showGeminiAssistantDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A2BE2), contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Analyze", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val activeTrip = cachedTrips.firstOrNull { it.status == TripStatus.ACCEPTED || it.status == TripStatus.IN_PROGRESS }
                if (activeTrip != null) {
                    BoltActiveTripCard(
                        trip = activeTrip,
                        driverName = activeTrip.driverName ?: "Kalu Driver",
                        driverPhone = activeTrip.driverPhone ?: "08039821033",
                        tricyclePlateNo = activeTrip.tricycleRegNo ?: "ABA-882-KU",
                        onOpenChat = { showTripChatDialog = true },
                        onCancelTrip = {
                            coroutineScope.launch {
                                repository.updateTripStatus(activeTrip.id, TripStatus.CANCELLED)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live Google Play Services GPS Telemetry Bar
                Surface(
                    color = AsphaltSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isLiveGpsLocked) EmeraldGreen else AsphaltDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isLiveGpsLocked) Icons.Default.GpsFixed else Icons.Default.LocationSearching,
                                contentDescription = null,
                                tint = if (isLiveGpsLocked) EmeraldGreen else SafetyAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isLiveGpsLocked) "HARDWARE GPS LOCKED (GOOGLE PLAY SERVICES)" else "CORRIDOR TRANSIT CENTROID",
                                    color = if (isLiveGpsLocked) EmeraldGreenLight else MutedSilver,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${String.format("%.4f", liveLat)}° N, ${String.format("%.4f", liveLng)}° E (${AbiaCorridorData.getLgaById(selectedOriginLga)?.name})",
                                    color = HighContrastWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = SafetyAmber)
                        ) {
                            Text(if (isLiveGpsLocked) "Refresh" else "Lock GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Schedule Button & Bottom Sheet Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showScheduleModal = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SafetyAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Schedule Ride", color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Pick advance time", color = MutedSilver, fontSize = 10.sp)
                            }
                        }
                    }

                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showRequestBottomSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Trip Sheet", color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Full route options", color = MutedSilver, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Route & Action Overview
            item {
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
                            Text(
                                text = "ACTIVE TRIP SELECTION",
                                style = MaterialTheme.typography.labelSmall,
                                color = SafetyAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₦$proposedFareNaira",
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TripOrigin, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Pickup Location", color = MutedSilver, fontSize = 10.sp)
                                Text(selectedOriginMarket, color = HighContrastWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Drop-off Destination", color = MutedSilver, fontSize = 10.sp)
                                Text(selectedDestMarket, color = HighContrastWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (intermediateStops.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Via: ${intermediateStops.joinToString(" ➔ ")}",
                                color = MutedSilver,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Volumetric Cargo Calculator Card
            item {
                VolumetricCargoCard(
                    sacks = cargoSacks,
                    basins = cargoBasins,
                    onSacksChanged = { cargoSacks = it },
                    onBasinsChanged = { cargoBasins = it }
                )
            }

            // Live P2P Bidding Section
            item {
                FareBiddingSection(
                    proposedFareNaira = proposedFareNaira,
                    onProposedFareChanged = { newFare ->
                        proposedFareNaira = newFare
                    },
                    activeBids = activeBids,
                    onAcceptBid = { bid ->
                        coroutineScope.launch {
                            repository.acceptBid(bid.tripId, bid)
                        }
                    }
                )
            }

            // Submit Request Button
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            com.example.utils.TransitSoundBeepHelper.playDispatchBeep(context)
                            val tripId = repository.createTripBooking(
                                serviceMode = selectedServiceMode,
                                originLga = selectedOriginLga,
                                originMarket = selectedOriginMarket,
                                destLga = selectedDestLga,
                                destMarket = selectedDestMarket,
                                intermediateStops = intermediateStops,
                                proposedFare = proposedFareNaira,
                                cargoSacks = cargoSacks,
                                cargoBasins = cargoBasins,
                                isWaitAndReturn = isWaitAndReturn,
                                waitMinutes = waitMinutes,
                                recipientName = recipientName,
                                recipientPhone = recipientPhone,
                                isRecurring = isRecurringDaily
                            )
                            showBookingSuccessDialog = tripId
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (selectedServiceMode == ServiceMode.WAYBILL) Icons.Default.Inventory2 else Icons.Default.DirectionsTransit,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedServiceMode == ServiceMode.WAYBILL) "Broadcast Waybill (₦$proposedFareNaira)" else "Broadcast Trip Request (₦$proposedFareNaira)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showBookingSuccessDialog != null) {
        val tripId = showBookingSuccessDialog ?: ""
        AlertDialog(
            onDismissRequest = { showBookingSuccessDialog = null },
            containerColor = AsphaltDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trip Broadcasted to Corridor", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Booking ID: $tripId",
                        color = SafetyAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nearby commercial kekes in $selectedOriginMarket are receiving your fare offer of ₦$proposedFareNaira. Counter-offers will appear in real time.",
                        color = MutedSilver,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBookingSuccessDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark)
                ) {
                    Text("Understood")
                }
            }
        )
    }

    if (showAddSavedPlaceDialog) {
        AddSavedPlaceDialog(
            onDismiss = { showAddSavedPlaceDialog = false },
            onSavePlace = { title, address, lga, iconType ->
                coroutineScope.launch {
                    repository.addOrUpdateSavedPlace(title, address, lga, iconType = iconType)
                    showAddSavedPlaceDialog = false
                }
            }
        )
    }

    if (showScheduleModal) {
        BoltScheduleModal(
            originMarket = selectedOriginMarket,
            originLgaId = selectedOriginLga,
            destinationMarket = selectedDestMarket,
            destinationLgaId = selectedDestLga,
            baseFareNaira = proposedFareNaira,
            onDismiss = { showScheduleModal = false },
            onConfirmSchedule = { epoch, isRec, rule, mode ->
                coroutineScope.launch {
                    val schedId = repository.scheduleRide(
                        originMarket = selectedOriginMarket,
                        originLgaId = selectedOriginLga,
                        destinationMarket = selectedDestMarket,
                        destinationLgaId = selectedDestLga,
                        pickupEpoch = epoch,
                        isRecurring = isRec,
                        recurringRule = rule,
                        serviceMode = mode,
                        estimatedFareNaira = proposedFareNaira
                    )
                    showScheduleModal = false
                    scheduledSuccessMessage = "Ride successfully scheduled! (#$schedId)"
                }
            }
        )
    }

    if (scheduledSuccessMessage != null) {
        AlertDialog(
            onDismissRequest = { scheduledSuccessMessage = null },
            containerColor = AsphaltDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventAvailable, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scheduled Ride Confirmed", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = scheduledSuccessMessage ?: "",
                    color = MutedSilver,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { scheduledSuccessMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark)
                ) {
                    Text("View in Rides Tab")
                }
            }
        )
    }

        val allChatMessages by repository.tripChatMessages.collectAsState()
    val currentTripChatList = allChatMessages["TRIP_DEMO_01"] ?: emptyList()

    if (showTripChatDialog) {
        BoltChatDialog(
            tripId = "TRIP_DEMO_01",
            targetName = "Kalu Driver",
            targetPhone = "08039821033",
            targetRoleLabel = "Assigned Rider",
            tricyclePlateNo = "ABA-882-KU",
            currentUserId = userAccount?.uid ?: "PASSENGER",
            messages = currentTripChatList,
            onSendMessage = { text ->
                coroutineScope.launch {
                    repository.sendTripChatMessage("TRIP_DEMO_01", text)
                }
            },
            onDismiss = { showTripChatDialog = false }
        )
    }

    if (showRequestBottomSheet) {
        KekeRequestBottomSheet(
            onDismissRequest = { showRequestBottomSheet = false },
            initialOriginLga = selectedOriginLga,
            initialOriginMarket = selectedOriginMarket,
            initialDestLga = selectedDestLga,
            initialDestMarket = selectedDestMarket,
            initialServiceMode = selectedServiceMode,
            initialIntermediateStops = intermediateStops,
            userLiveLat = if (isLiveGpsLocked) liveLat else null,
            userLiveLng = if (isLiveGpsLocked) liveLng else null,
            onConfirmRequest = { mode, origLga, origMarket, dstLga, dstMarket, stops, fare, note ->
                selectedServiceMode = mode
                selectedOriginLga = origLga
                selectedOriginMarket = origMarket
                selectedDestLga = dstLga
                selectedDestMarket = dstMarket
                intermediateStops = stops
                proposedFareNaira = fare
                showRequestBottomSheet = false
                coroutineScope.launch {
                    val tripId = repository.createTripBooking(
                        serviceMode = mode,
                        originLga = origLga,
                        originMarket = origMarket,
                        destLga = dstLga,
                        destMarket = dstMarket,
                        intermediateStops = stops,
                        proposedFare = fare,
                        cargoSacks = cargoSacks,
                        cargoBasins = cargoBasins,
                        isWaitAndReturn = isWaitAndReturn,
                        waitMinutes = waitMinutes,
                        recipientName = recipientName,
                        recipientPhone = recipientPhone,
                        isRecurring = isRecurringDaily
                    )
                    showBookingSuccessDialog = tripId
                }
            }
        )
    }
}
