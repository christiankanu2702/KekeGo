package com.example.ui.screens

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
import com.example.data.repository.KekeGoRepository
import com.example.ui.components.*
import com.example.ui.theme.*
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
    var showPastTripsSheet by remember { mutableStateOf(false) }

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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showRequestBottomSheet = true },
                containerColor = SafetyAmber,
                contentColor = AsphaltDark,
                icon = { Icon(Icons.Default.ElectricRickshaw, contentDescription = null) },
                text = { Text("Request Keke", fontWeight = FontWeight.Black) },
                modifier = Modifier.testTag("request_keke_fab")
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "KekeGo Passenger",
                            style = MaterialTheme.typography.titleLarge,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Abia Transit & Market Waybills",
                            style = MaterialTheme.typography.bodySmall,
                            color = SafetyAmber
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AsphaltDark),
                actions = {
                    // Past Trips History Log Button
                    IconButton(
                        onClick = { showPastTripsSheet = true },
                        modifier = Modifier.testTag("open_past_trips_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Past Trips Log",
                            tint = SafetyAmber
                        )
                    }

                    // Wallet Balance Quick Action
                    TextButton(
                        onClick = onNavigateToWallet,
                        colors = ButtonDefaults.textButtonColors(contentColor = EmeraldGreenLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "₦${userAccount?.walletBalanceNaira ?: 3500}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Emergency SOS Trigger
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Interactive Google Maps SDK View for Abia State Route
                GoogleTransitMapView(
                    originMarket = selectedOriginMarket,
                    originLgaId = selectedOriginLga,
                    destinationMarket = selectedDestMarket,
                    destinationLgaId = selectedDestLga,
                    intermediateStops = intermediateStops,
                    userLiveLat = if (isLiveGpsLocked) liveLat else null,
                    userLiveLng = if (isLiveGpsLocked) liveLng else null,
                    onMarketSelected = { selectedDestMarket = it }
                )

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
                            Text(
                                text = if (isLiveGpsLocked) "Re-Scan" else "Acquire GPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fast Request Bottom Sheet Launcher Banner
                Surface(
                    color = SafetyAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showRequestBottomSheet = true }
                        .testTag("open_request_bottom_sheet_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(SafetyAmber, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsTransit,
                                    contentDescription = null,
                                    tint = AsphaltDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Book Ride / Waybill via Bottom Sheet",
                                    color = HighContrastWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Pick origin, drop-off, stops & estimate fare",
                                    color = SafetyAmberLight,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { showRequestBottomSheet = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SafetyAmber,
                                contentColor = AsphaltDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Open Sheet", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Service Mode Switcher
            item {
                Text(
                    text = "Select Transit Service:",
                    style = MaterialTheme.typography.labelLarge,
                    color = HighContrastWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ServiceMode.PASSENGER to "Passenger Ride",
                        ServiceMode.WAYBILL to "Waybill (OTP)",
                        ServiceMode.DAY_HIRE to "Hourly Day-Hire"
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = selectedServiceMode == mode,
                            onClick = { selectedServiceMode = mode },
                            label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                when (mode) {
                                    ServiceMode.PASSENGER -> Icon(Icons.Default.ElectricRickshaw, contentDescription = null, modifier = Modifier.size(16.dp))
                                    ServiceMode.WAYBILL -> Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                                    ServiceMode.DAY_HIRE -> Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SafetyAmber,
                                selectedLabelColor = AsphaltDark,
                                containerColor = AsphaltCard,
                                labelColor = HighContrastWhite
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Route & Market Selector Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Abia Transit Route (Pick-up & Drop-off)",
                            style = MaterialTheme.typography.titleMedium,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Connected across all 17 LGAs via Google Maps SDK",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Origin Pick-up LGA & Market Quick Selectors
                        Text(
                            text = "PICK-UP POINT (ORIGIN)",
                            color = EmeraldGreenLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Origin LGA Quick Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("aba_south" to "Aba South", "aba_north" to "Aba North", "umuahia_north" to "Umuahia N", "osisioma" to "Osisioma").forEach { (lgaId, lgaLabel) ->
                                FilterChip(
                                    selected = selectedOriginLga == lgaId,
                                    onClick = {
                                        selectedOriginLga = lgaId
                                        val firstMarket = AbiaCorridorData.getLgaById(lgaId)?.keyMarketsAndJunctions?.firstOrNull()
                                        if (firstMarket != null) selectedOriginMarket = firstMarket
                                    },
                                    label = { Text(lgaLabel, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreen,
                                        selectedLabelColor = AsphaltDark,
                                        containerColor = AsphaltCard,
                                        labelColor = HighContrastWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Origin Selector
                        OutlinedTextField(
                            value = selectedOriginMarket,
                            onValueChange = { selectedOriginMarket = it },
                            label = { Text("Pick-up Market/Junction (${AbiaCorridorData.getLgaById(selectedOriginLga)?.name})", color = MutedSilver) },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = EmeraldGreen) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighContrastWhite,
                                unfocusedTextColor = HighContrastWhite,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = AsphaltDivider
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Destination Drop-off LGA & Market Quick Selectors
                        Text(
                            text = "DROP-OFF POINT (DESTINATION)",
                            color = SafetyAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Destination LGA Quick Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("aba_north" to "Aba North", "aba_south" to "Aba South", "umuahia_south" to "Umuahia S", "ikwuano" to "Ikwuano").forEach { (lgaId, lgaLabel) ->
                                FilterChip(
                                    selected = selectedDestLga == lgaId,
                                    onClick = {
                                        selectedDestLga = lgaId
                                        val firstMarket = AbiaCorridorData.getLgaById(lgaId)?.keyMarketsAndJunctions?.firstOrNull()
                                        if (firstMarket != null) selectedDestMarket = firstMarket
                                    },
                                    label = { Text(lgaLabel, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SafetyAmber,
                                        selectedLabelColor = AsphaltDark,
                                        containerColor = AsphaltCard,
                                        labelColor = HighContrastWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Destination Selector
                        OutlinedTextField(
                            value = selectedDestMarket,
                            onValueChange = { selectedDestMarket = it },
                            label = { Text("Drop-off Market/Junction (${AbiaCorridorData.getLgaById(selectedDestLga)?.name})", color = MutedSilver) },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = SafetyAmber) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighContrastWhite,
                                unfocusedTextColor = HighContrastWhite,
                                focusedBorderColor = SafetyAmber,
                                unfocusedBorderColor = AsphaltDivider
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Multi-Stop Intermediate routing
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Intermediate Errands & Market Stops:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (intermediateStops.isNotEmpty()) {
                            intermediateStops.forEachIndexed { index, stop ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AddLocation, contentDescription = null, tint = SafetyAmberDark, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Stop ${index + 1}: $stop", color = HighContrastWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { intermediateStops = intermediateStops.filterIndexed { i, _ -> i != index } },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newStopInput,
                                onValueChange = { newStopInput = it },
                                placeholder = { Text("Add Stop (e.g. Brass Junction)", color = MutedSilver, fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = HighContrastWhite,
                                    unfocusedTextColor = HighContrastWhite,
                                    focusedBorderColor = SafetyAmber,
                                    unfocusedBorderColor = AsphaltDivider
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newStopInput.isNotBlank()) {
                                        intermediateStops = intermediateStops + newStopInput
                                        newStopInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SafetyAmberDark, contentColor = HighContrastWhite),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add", fontSize = 12.sp)
                            }
                        }

                        // Wait and Return Toggle
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Wait-and-Return Errands",
                                    color = HighContrastWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Pause driver while you shop (${waitMinutes}m)",
                                    color = MutedSilver,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isWaitAndReturn,
                                onCheckedChange = { isWaitAndReturn = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SafetyAmber,
                                    checkedTrackColor = SafetyAmberDark,
                                    uncheckedThumbColor = MutedSilver,
                                    uncheckedTrackColor = AsphaltCard
                                )
                            )
                        }
                    }
                }
            }

            // Volumetric Cargo & KekeGo Vault Savings UI
            item {
                VolumetricCargoCard(
                    sacks = cargoSacks,
                    basins = cargoBasins,
                    onSacksChanged = { cargoSacks = it },
                    onBasinsChanged = { cargoBasins = it }
                )
            }

            item {
                KekeGoVaultCard(
                    vaultBalanceNaira = userAccount?.vaultSavingsNaira ?: 450,
                    onSweepClicked = {
                        coroutineScope.launch {
                            repository.sweepVaultToWallet()
                        }
                    }
                )
            }

            // Waybill Specifics (If Waybill mode active)
            if (selectedServiceMode == ServiceMode.WAYBILL) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Digital Waybill Delivery & OTP Protocol",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HighContrastWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "A tamper-evident 6-digit OTP will be generated. The receiver must disclose it to the rider to unlock final parcel release.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSilver
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = recipientName,
                                onValueChange = { recipientName = it },
                                label = { Text("Receiver Full Name", color = MutedSilver) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = HighContrastWhite,
                                    unfocusedTextColor = HighContrastWhite,
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = AsphaltDivider
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = recipientPhone,
                                onValueChange = { recipientPhone = it },
                                label = { Text("Receiver Mobile (SMS OTP dispatch)", color = MutedSilver) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = HighContrastWhite,
                                    unfocusedTextColor = HighContrastWhite,
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = AsphaltDivider
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // P2P Fare Negotiation Bidding Engine
            item {
                FareBiddingSection(
                    proposedFareNaira = proposedFareNaira,
                    onProposedFareChanged = { proposedFareNaira = it },
                    activeBids = activeBids,
                    onAcceptBid = { bid ->
                        coroutineScope.launch {
                            val activeTrip = cachedTrips.firstOrNull { it.status == TripStatus.REQUESTED || it.status == TripStatus.BIDDING }
                            if (activeTrip != null) {
                                repository.acceptBid(activeTrip.id, bid)
                            }
                        }
                    }
                )
            }

            // Scheduled Commute Option
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AsphaltCard, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Daily Recurring Commute",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto-dispatch every morning for market trade",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                    Checkbox(
                        checked = isRecurringDaily,
                        onCheckedChange = { isRecurringDaily = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SafetyAmber,
                            uncheckedColor = MutedSilver
                        )
                    )
                }
            }

            // Action Button: Request Ride / Waybill
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
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
                        imageVector = if (selectedServiceMode == ServiceMode.WAYBILL) Icons.Default.Inventory2 else Icons.Default.ElectricRickshaw,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedServiceMode == ServiceMode.WAYBILL) "Broadcast Waybill (₦$proposedFareNaira)" else "Broadcast Trip Request (₦$proposedFareNaira)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Past Trips History Card
                Surface(
                    color = AsphaltCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showPastTripsSheet = true }
                        .testTag("open_past_trips_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SafetyAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = SafetyAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Past Tricycle Trips & Waybill Logs",
                                    color = HighContrastWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${cachedTrips.size} logged trips synced from Firestore",
                                    color = MutedSilver,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Log",
                            tint = SafetyAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
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

                    if (selectedServiceMode == ServiceMode.WAYBILL && recipientPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                SmsHelper.dispatchNativeWaybillOtpSms(
                                    context = context,
                                    recipientPhone = recipientPhone,
                                    otp = "SECURE_WAYBILL_ACTIVE",
                                    tripOrigin = selectedOriginMarket,
                                    tripDestination = selectedDestMarket
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreenLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SendToMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dispatch OTP via SMS App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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

    if (showPastTripsSheet) {
        PastTripsLogSheet(
            repository = repository,
            onDismissRequest = { showPastTripsSheet = false },
            onBookAgain = { rebookTrip ->
                selectedOriginLga = rebookTrip.originLgaId
                selectedOriginMarket = rebookTrip.originMarket
                selectedDestLga = rebookTrip.destinationLgaId
                selectedDestMarket = rebookTrip.destinationMarket
                selectedServiceMode = rebookTrip.serviceMode
                intermediateStops = rebookTrip.intermediateStops
                proposedFareNaira = rebookTrip.finalAgreedFareNaira
                cargoSacks = rebookTrip.cargoSacks
                cargoBasins = rebookTrip.cargoBasins
                showPastTripsSheet = false

                coroutineScope.launch {
                    val tripId = repository.createTripBooking(
                        serviceMode = rebookTrip.serviceMode,
                        originLga = rebookTrip.originLgaId,
                        originMarket = rebookTrip.originMarket,
                        destLga = rebookTrip.destinationLgaId,
                        destMarket = rebookTrip.destinationMarket,
                        intermediateStops = rebookTrip.intermediateStops,
                        proposedFare = rebookTrip.finalAgreedFareNaira,
                        cargoSacks = rebookTrip.cargoSacks,
                        cargoBasins = rebookTrip.cargoBasins,
                        isWaitAndReturn = rebookTrip.isWaitAndReturn,
                        waitMinutes = rebookTrip.waitMinutes,
                        recipientName = rebookTrip.waybillRecipientName,
                        recipientPhone = rebookTrip.waybillRecipientPhone
                    )
                    showBookingSuccessDialog = tripId
                }
            }
        )
    }
}
