package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.models.*
import com.example.data.repository.KekeGoRepository
import com.example.ui.components.SosFloatingTrigger
import com.example.ui.theme.*
import com.example.utils.LocationTracker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    repository: KekeGoRepository,
    onNavigateToWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userAccount by repository.currentUserAccount.collectAsState()
    val trips by repository.getCachedTripsFlow().collectAsState(initial = emptyList())

    // Driver Live GPS
    var driverLat by remember { mutableDoubleStateOf(5.1118) }
    var driverLng by remember { mutableDoubleStateOf(7.3689) }
    var isDriverGpsLocked by remember { mutableStateOf(false) }

    val driverLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            coroutineScope.launch {
                val loc = LocationTracker.getLiveDeviceLocation(context)
                if (loc != null) {
                    driverLat = loc.latitude
                    driverLng = loc.longitude
                    isDriverGpsLocked = true
                }
            }
        }
    }

    // Driver Service Filters
    var currentFilter by remember { mutableStateOf(DriverServiceFilter.BOTH) }
    var isOnlineDuty by remember { mutableStateOf(true) }
    var selectedLgaFilter by remember { mutableStateOf("aba_south") }
    var isBatterySaverMode by remember { mutableStateOf(false) }

    // Change deposit dialog state
    var showDepositChangeDialog by remember { mutableStateOf(false) }
    var changeDepositAmount by remember { mutableIntStateOf(50) }
    var changeSuccessToken by remember { mutableStateOf<String?>(null) }

    // Float advance state
    var showFloatDialog by remember { mutableStateOf(false) }
    var floatAmountInput by remember { mutableIntStateOf(5000) }

    // Waybill OTP verify dialog
    var activeWaybillTripId by remember { mutableStateOf<String?>(null) }
    var enteredOtp by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }

    // Counter offer input
    var counterTripTarget by remember { mutableStateOf<Trip?>(null) }
    var counterFareAmount by remember { mutableIntStateOf(600) }

    Scaffold(
        containerColor = AsphaltBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KekeGo Driver Terminal",
                                style = MaterialTheme.typography.titleLarge,
                                color = HighContrastWhite,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Verified State Compliance Badge
                            Surface(
                                color = EmeraldGreen,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = AsphaltDark, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("VERIFIED", color = AsphaltDark, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Text(
                            text = "Abia Commercial Riders Permit: ${userAccount?.stateRiderPermitId ?: "AB/KKE/2026/8941"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SafetyAmber,
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AsphaltDark),
                actions = {
                    SosFloatingTrigger(
                        onTriggerSos = {
                            coroutineScope.launch {
                                repository.triggerEmergencySos(5.1118, 7.3689, selectedLgaFilter)
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
                // Duty Status & Spatial Dispatch LGA Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isOnlineDuty) EmeraldGreen else AsphaltDivider)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isOnlineDuty) "ON DUTY (DISPATCH ACTIVE)" else "OFF DUTY",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isOnlineDuty) EmeraldGreenLight else MutedSilver,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Zonal GPS: ${AbiaCorridorData.getLgaById(selectedLgaFilter)?.name ?: "Aba South"} Sector",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HighContrastWhite
                                )
                            }
                            Switch(
                                checked = isOnlineDuty,
                                onCheckedChange = { isOnlineDuty = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldGreen,
                                    checkedTrackColor = EmeraldGreenDark
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Battery-Saver Google Batched GPS Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AsphaltCard, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isBatterySaverMode) Icons.Default.BatteryChargingFull else Icons.Default.BatterySaver,
                                    contentDescription = null,
                                    tint = if (isBatterySaverMode) EmeraldGreen else SafetyAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Google Batched GPS (Battery-Saver)",
                                        color = HighContrastWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isBatterySaverMode) "20s batched polling active (saves ~70% battery)" else "High-precision continuous stream",
                                        color = MutedSilver,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Switch(
                                checked = isBatterySaverMode,
                                onCheckedChange = { isBatterySaverMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldGreen,
                                    checkedTrackColor = EmeraldGreenDark,
                                    uncheckedThumbColor = MutedSilver,
                                    uncheckedTrackColor = AsphaltDark
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Compliance & Vehicle Credentials Bar
                        Surface(
                            color = AsphaltCard,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Union Branch:", color = MutedSilver, fontSize = 10.sp)
                                    Text(userAccount?.unionBranchCode ?: "ATRWAN-ABA-2", color = HighContrastWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Plate No:", color = MutedSilver, fontSize = 10.sp)
                                    Text(userAccount?.tricyclePlateNo ?: "ABA-882-KU", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Chassis No:", color = MutedSilver, fontSize = 10.sp)
                                    Text(userAccount?.chassisNumber?.takeLast(8) ?: "RE-782103", color = HighContrastWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Service Filter Toggles ("Passengers Only", "Waybill Only", "Both")
            item {
                Text(
                    text = "Dispatch Service Filter:",
                    style = MaterialTheme.typography.labelLarge,
                    color = HighContrastWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        DriverServiceFilter.PASSENGERS_ONLY to "Passengers Only",
                        DriverServiceFilter.WAYBILL_ONLY to "Waybill Only",
                        DriverServiceFilter.BOTH to "Both Services"
                    ).forEach { (filter, label) ->
                        FilterChip(
                            selected = currentFilter == filter,
                            onClick = { currentFilter = filter },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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

            // KekeGo Credit Module & Commission Ledger Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PriceCheck, contentDescription = null, tint = SafetyAmber)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "KekeGo Credit & Union Float",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HighContrastWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { showFloatDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Request Float", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Float Loan Box
                            Surface(
                                color = AsphaltCard,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Fuel Float Balance", color = MutedSilver, fontSize = 11.sp)
                                    Text("₦${userAccount?.creditFloatBalanceNaira ?: 0}", color = HighContrastWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    Text("Limit: ₦15,000", color = EmeraldGreenLight, fontSize = 10.sp)
                                }
                            }

                            // Commission Deductions Box
                            Surface(
                                color = AsphaltCard,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Commission Accrued", color = MutedSilver, fontSize = 11.sp)
                                    Text("₦${userAccount?.commissionAccruedNaira ?: 300}", color = SafetyAmber, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    Text("Daily Levy: ₦200 (Paid)", color = HighContrastWhite, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Spatial Dispatch Radar: Incoming Passenger & Waybill Requests
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Spatial Dispatch Radar",
                        style = MaterialTheme.typography.titleMedium,
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${trips.filter { it.status == TripStatus.REQUESTED || it.status == TripStatus.BIDDING }.size} Pending",
                        color = SafetyAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val pendingTrips = trips.filter {
                (it.status == TripStatus.REQUESTED || it.status == TripStatus.BIDDING) &&
                        (currentFilter == DriverServiceFilter.BOTH ||
                                (currentFilter == DriverServiceFilter.PASSENGERS_ONLY && it.serviceMode == ServiceMode.PASSENGER) ||
                                (currentFilter == DriverServiceFilter.WAYBILL_ONLY && it.serviceMode == ServiceMode.WAYBILL))
            }

            if (pendingTrips.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AsphaltCard,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Radar, contentDescription = null, tint = MutedSilver, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Scanning Aba & Umuahia Corridors...", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                            Text("Requests from Ariaria, Bata, and Ubani will appear instantly", color = MutedSilver, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(pendingTrips) { trip ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AsphaltCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (trip.serviceMode == ServiceMode.WAYBILL) EmeraldGreenDark else SafetyAmberDark,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = trip.serviceMode.name,
                                        color = HighContrastWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Offered: ₦${trip.proposedFareNaira}",
                                    color = EmeraldGreenLight,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${trip.originMarket} ➔ ${trip.destinationMarket}",
                                color = HighContrastWhite,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (trip.cargoSacks > 0 || trip.cargoBasins > 0) {
                                Text(
                                    text = "Cargo: ${trip.cargoSacks} Sacks, ${trip.cargoBasins} Basins",
                                    color = SafetyAmber,
                                    fontSize = 12.sp
                                )
                            }

                            if (trip.isWaitAndReturn) {
                                Text(
                                    text = "Wait-and-Return: ${trip.waitMinutes} minutes errand",
                                    color = HighContrastWhite,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        counterTripTarget = trip
                                        counterFareAmount = trip.proposedFareNaira + 100
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyAmber),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Counter-Offer")
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.submitDriverBid(trip.id, trip.proposedFareNaira)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Accept ₦${trip.proposedFareNaira}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // In-Progress Trips
            val activeTrips = trips.filter { it.status == TripStatus.ACCEPTED || it.status == TripStatus.IN_PROGRESS }
            if (activeTrips.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Trip in Transit",
                        style = MaterialTheme.typography.titleMedium,
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(activeTrips) { activeTrip ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "TRIP ID: ${activeTrip.id}",
                                color = EmeraldGreenLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${activeTrip.originMarket} ➔ ${activeTrip.destinationMarket}",
                                color = HighContrastWhite,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Agreed Fare: ₦${activeTrip.finalAgreedFareNaira}",
                                color = SafetyAmber,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (activeTrip.status == TripStatus.ACCEPTED) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.startTrip(activeTrip.id)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Start Transit")
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            if (activeTrip.serviceMode == ServiceMode.WAYBILL) {
                                                activeWaybillTripId = activeTrip.id
                                                enteredOtp = ""
                                                otpError = null
                                            } else {
                                                coroutineScope.launch {
                                                    repository.completeTrip(activeTrip.id)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (activeTrip.serviceMode == ServiceMode.WAYBILL) "Verify Waybill OTP & Complete" else "Complete Trip & Collect Cash")
                                    }

                                    // Fractional Change Deposit Button
                                    OutlinedButton(
                                        onClick = {
                                            showDepositChangeDialog = true
                                            changeSuccessToken = null
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyAmber),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("No Physical Change? Deposit to Rider's Vault", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Micro-Advance Float Request Dialog
    if (showFloatDialog) {
        AlertDialog(
            onDismissRequest = { showFloatDialog = false },
            containerColor = AsphaltDark,
            title = {
                Text("Request Keke Fuel/Maintenance Float", color = HighContrastWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "KekeGo Credit advances up to ₦15,000 to active, verified riders. Automated deductions (10%) are recovered seamlessly from upcoming completed trips.",
                        color = MutedSilver,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select Micro-Advance Float:", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3000, 5000, 10000, 15000).forEach { amt ->
                            FilterChip(
                                selected = floatAmountInput == amt,
                                onClick = { floatAmountInput = amt },
                                label = { Text("₦$amt", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SafetyAmber,
                                    selectedLabelColor = AsphaltDark,
                                    containerColor = AsphaltCard,
                                    labelColor = HighContrastWhite
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.requestCreditFloat(floatAmountInput)
                            showFloatDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark)
                ) {
                    Text("Disburse ₦$floatAmountInput", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFloatDialog = false }) {
                    Text("Cancel", color = MutedSilver)
                }
            }
        )
    }

    // Counter Offer Modal
    if (counterTripTarget != null) {
        val target = counterTripTarget!!
        AlertDialog(
            onDismissRequest = { counterTripTarget = null },
            containerColor = AsphaltDark,
            title = {
                Text("Submit Counter-Offer", color = HighContrastWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Route: ${target.originMarket} to ${target.destinationMarket}",
                        color = MutedSilver,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Commuter proposed: ₦${target.proposedFareNaira}",
                        color = HighContrastWhite,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Your Counter-Offer (Naira):", color = SafetyAmber, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledIconButton(
                            onClick = { if (counterFareAmount > 300) counterFareAmount -= 50 },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = AsphaltCard)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = HighContrastWhite)
                        }
                        Text("₦$counterFareAmount", color = SafetyAmber, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        FilledIconButton(
                            onClick = { counterFareAmount += 50 },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = SafetyAmber)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = AsphaltDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.submitDriverBid(target.id, counterFareAmount)
                            counterTripTarget = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark)
                ) {
                    Text("Send Counter-Offer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { counterTripTarget = null }) {
                    Text("Cancel", color = MutedSilver)
                }
            }
        )
    }

    // Waybill OTP Verification Dialog
    if (activeWaybillTripId != null) {
        val tripId = activeWaybillTripId!!
        AlertDialog(
            onDismissRequest = { activeWaybillTripId = null },
            containerColor = AsphaltDark,
            title = {
                Text("Receiver OTP Verification", color = HighContrastWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Ask the receiver at the market terminal to provide the 6-digit release OTP code sent to their phone:",
                        color = MutedSilver,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { enteredOtp = it.take(6) },
                        label = { Text("6-Digit OTP Code", color = MutedSilver) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = AsphaltDivider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (otpError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = otpError ?: "", color = ErrorRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dead-Zone Verified: Handoff verifies locally in Room even with zero signal inside Ariaria/Ubani stalls.",
                                color = MutedSilver,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = repository.completeTrip(tripId, enteredOtp)
                            if (success) {
                                activeWaybillTripId = null
                            } else {
                                otpError = "Invalid OTP code. Please verify with recipient."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark)
                ) {
                    Text("Verify & Release Cargo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeWaybillTripId = null }) {
                    Text("Cancel", color = MutedSilver)
                }
            }
        )
    }

    // Fractional Cash Change Deposit to Vault Modal
    if (showDepositChangeDialog) {
        AlertDialog(
            onDismissRequest = { showDepositChangeDialog = false },
            containerColor = AsphaltDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deposit Change to Vault", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    if (changeSuccessToken == null) {
                        Text(
                            text = "No small physical naira notes? Transfer fractional change directly into the commuter's KekeGo Vault digital ledger:",
                            color = MutedSilver,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Select Change Amount:", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(50, 100, 150, 200).forEach { amt ->
                                FilterChip(
                                    selected = changeDepositAmount == amt,
                                    onClick = { changeDepositAmount = amt },
                                    label = { Text("₦$amt", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreen,
                                        selectedLabelColor = AsphaltDark,
                                        containerColor = AsphaltCard,
                                        labelColor = HighContrastWhite
                                    )
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("₦$changeDepositAmount Deposited!", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                            Text("Token: $changeSuccessToken", color = EmeraldGreenLight, fontSize = 11.sp)
                            Text("Commuter's KekeGo Vault updated instantly", color = MutedSilver, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (changeSuccessToken == null) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val token = repository.depositFractionalChangeToVault(changeDepositAmount)
                                changeSuccessToken = token
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark)
                    ) {
                        Text("Credit ₦$changeDepositAmount to Vault", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { showDepositChangeDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark)
                    ) {
                        Text("Done")
                    }
                }
            },
            dismissButton = {
                if (changeSuccessToken == null) {
                    TextButton(onClick = { showDepositChangeDialog = false }) {
                        Text("Cancel", color = MutedSilver)
                    }
                }
            }
        )
    }
}
