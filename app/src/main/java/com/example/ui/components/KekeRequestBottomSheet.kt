package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AbiaCorridorData
import com.example.data.models.LgaZone
import com.example.data.models.ServiceMode
import com.example.ui.theme.*
import com.example.utils.LocationTracker
import kotlinx.coroutines.launch

/**
 * High-fidelity ModalBottomSheet component for requesting a new Keke tricycle ride or waybill.
 * Allows entering/selecting current location (origin) with 1-tap live GPS lock and destination,
 * intermediate stops, LGA corridor auto-detection, quick market chips, and estimated fare calculation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KekeRequestBottomSheet(
    onDismissRequest: () -> Unit,
    initialOriginLga: String = "aba_south",
    initialOriginMarket: String = "Bata Junction",
    initialDestLga: String = "aba_north",
    initialDestMarket: String = "Ariaria International Market",
    initialServiceMode: ServiceMode = ServiceMode.PASSENGER,
    initialIntermediateStops: List<String> = emptyList(),
    userLiveLat: Double? = null,
    userLiveLng: Double? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    onConfirmRequest: (
        serviceMode: ServiceMode,
        originLga: String,
        originMarket: String,
        destLga: String,
        destMarket: String,
        intermediateStops: List<String>,
        proposedFareNaira: Int,
        note: String
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var serviceMode by remember { mutableStateOf(initialServiceMode) }
    var originLgaId by remember { mutableStateOf(initialOriginLga) }
    var originMarketInput by remember { mutableStateOf(initialOriginMarket) }
    var destLgaId by remember { mutableStateOf(initialDestLga) }
    var destMarketInput by remember { mutableStateOf(initialDestMarket) }

    var intermediateStops by remember { mutableStateOf(initialIntermediateStops) }
    var newStopInput by remember { mutableStateOf("") }
    var showAddStopField by remember { mutableStateOf(false) }

    var rideNote by remember { mutableStateOf("") }

    // Live GPS state
    var currentLat by remember { mutableDoubleStateOf(userLiveLat ?: 5.1118) }
    var currentLng by remember { mutableDoubleStateOf(userLiveLng ?: 7.3689) }
    var isGpsAcquiring by remember { mutableStateOf(false) }
    var isGpsLocked by remember { mutableStateOf(userLiveLat != null && userLiveLng != null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            isGpsAcquiring = true
            coroutineScope.launch {
                val loc = LocationTracker.getLiveDeviceLocation(context)
                if (loc != null) {
                    currentLat = loc.latitude
                    currentLng = loc.longitude
                    isGpsLocked = true
                    // Auto-resolve closest LGA and update origin
                    val closest = AbiaCorridorData.findNearestLga(loc.latitude, loc.longitude)
                    if (closest != null) {
                        originLgaId = closest.id
                        originMarketInput = "Current Location (${closest.name})"
                    }
                }
                isGpsAcquiring = false
            }
        }
    }

    // Dynamic Fare Calculation based on LGA base fares and stops
    val originLgaObj = remember(originLgaId) { AbiaCorridorData.getLgaById(originLgaId) }
    val destLgaObj = remember(destLgaId) { AbiaCorridorData.getLgaById(destLgaId) }

    val calculatedBaseFare = remember(originLgaId, destLgaId, intermediateStops.size, serviceMode) {
        val oFare = originLgaObj?.baseFareNaira ?: 350
        val dFare = destLgaObj?.baseFareNaira ?: 350
        val isInterCity = originLgaObj?.zoneCategory != destLgaObj?.zoneCategory
        val interCityMultiplier = if (originLgaId != destLgaId && isInterCity) 1.4 else 1.1
        val stopsCost = intermediateStops.size * 150
        val modeBonus = when (serviceMode) {
            ServiceMode.PASSENGER -> 0
            ServiceMode.WAYBILL -> 150
            ServiceMode.DAY_HIRE -> 1200
        }
        val raw = (((oFare + dFare) / 2.0) * interCityMultiplier).toInt() + stopsCost + modeBonus
        ((raw + 49) / 50) * 50
    }

    var customFareOffer by remember { mutableIntStateOf(calculatedBaseFare) }

    LaunchedEffect(calculatedBaseFare) {
        customFareOffer = calculatedBaseFare
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AsphaltDark,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MutedSilver.copy(alpha = 0.5f))
                )
            }
        },
        modifier = Modifier.testTag("keke_request_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SafetyAmber.copy(alpha = 0.18f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ElectricRickshaw,
                                    contentDescription = null,
                                    tint = SafetyAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Request a Keke",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = HighContrastWhite
                            )
                            Text(
                                text = "Abia Transit Express • 17 LGAs",
                                style = MaterialTheme.typography.bodySmall,
                                color = SafetyAmber
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .size(32.dp)
                        .background(AsphaltSurface, CircleShape)
                        .testTag("close_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MutedSilver,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Service Mode Toggle Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ServiceMode.PASSENGER to "Passenger Ride",
                    ServiceMode.WAYBILL to "Waybill Delivery",
                    ServiceMode.DAY_HIRE to "Day-Hire"
                ).forEach { (mode, label) ->
                    val isSelected = serviceMode == mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { serviceMode = mode }
                            .border(
                                width = 1.dp,
                                color = if (isSelected) SafetyAmber else AsphaltDivider,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .testTag("service_mode_${mode.name.lowercase()}"),
                        color = if (isSelected) SafetyAmber.copy(alpha = 0.15f) else AsphaltSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    ServiceMode.PASSENGER -> Icons.Default.ElectricRickshaw
                                    ServiceMode.WAYBILL -> Icons.Default.Inventory2
                                    ServiceMode.DAY_HIRE -> Icons.Default.AccessTime
                                },
                                contentDescription = null,
                                tint = if (isSelected) SafetyAmber else MutedSilver,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) HighContrastWhite else MutedSilver,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // ORIGIN & DESTINATION CARD
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = AsphaltSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // ORIGIN HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(EmeraldGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PICK-UP (CURRENT LOCATION)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenLight,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Use My GPS Button
                        TextButton(
                            onClick = {
                                if (LocationTracker.hasLocationPermission(context)) {
                                    isGpsAcquiring = true
                                    coroutineScope.launch {
                                        val loc = LocationTracker.getLiveDeviceLocation(context)
                                        if (loc != null) {
                                            currentLat = loc.latitude
                                            currentLng = loc.longitude
                                            isGpsLocked = true
                                            val closest = AbiaCorridorData.findNearestLga(loc.latitude, loc.longitude)
                                            if (closest != null) {
                                                originLgaId = closest.id
                                                originMarketInput = "Current Location (${closest.name})"
                                            }
                                        }
                                        isGpsAcquiring = false
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("use_my_location_button")
                        ) {
                            Icon(
                                imageVector = if (isGpsLocked) Icons.Default.GpsFixed else Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = if (isGpsLocked) EmeraldGreen else SafetyAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGpsAcquiring) "Locating..." else if (isGpsLocked) "GPS Locked" else "Use My GPS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGpsLocked) EmeraldGreenLight else SafetyAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Origin Input TextField
                    OutlinedTextField(
                        value = originMarketInput,
                        onValueChange = { originMarketInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("origin_location_input"),
                        placeholder = { Text("Enter pick-up spot or landmark", color = MutedSilver.copy(alpha = 0.7f), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = EmeraldGreen)
                        },
                        trailingIcon = {
                            if (originMarketInput.isNotEmpty()) {
                                IconButton(onClick = { originMarketInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MutedSilver, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = AsphaltDivider,
                            focusedContainerColor = AsphaltCard,
                            unfocusedContainerColor = AsphaltCard
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Origin LGA Selector & Quick Market Chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "LGA Corridor: ${originLgaObj?.name ?: "Abia"}",
                        fontSize = 11.sp,
                        color = MutedSilver
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("aba_south" to "Aba South", "aba_north" to "Aba North", "umuahia_north" to "Umuahia N", "osisioma" to "Osisioma", "obingwa" to "Obingwa").forEach { (id, label) ->
                            val isSel = originLgaId == id
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    originLgaId = id
                                    val firstMarket = AbiaCorridorData.getLgaById(id)?.keyMarketsAndJunctions?.firstOrNull()
                                    if (firstMarket != null) originMarketInput = firstMarket
                                },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldGreen,
                                    selectedLabelColor = AsphaltDark,
                                    containerColor = AsphaltCard,
                                    labelColor = HighContrastWhite
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    // Origin Market Quick Selection Chips
                    val currentOriginMarkets = originLgaObj?.keyMarketsAndJunctions ?: emptyList()
                    if (currentOriginMarkets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currentOriginMarkets.take(4).forEach { market ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (originMarketInput == market) EmeraldGreen.copy(alpha = 0.2f) else AsphaltCard,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 0.5.dp,
                                        color = if (originMarketInput == market) EmeraldGreen else AsphaltDivider
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { originMarketInput = market }
                                ) {
                                    Text(
                                        text = market,
                                        fontSize = 10.sp,
                                        color = if (originMarketInput == market) EmeraldGreenLight else HighContrastWhite,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Connector Arrow & Intermediate Stops
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(AsphaltCard, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap",
                                tint = MutedSilver,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        val tempLga = originLgaId
                                        val tempMarket = originMarketInput
                                        originLgaId = destLgaId
                                        originMarketInput = destMarketInput
                                        destLgaId = tempLga
                                        destMarketInput = tempMarket
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = AsphaltDivider
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { showAddStopField = !showAddStopField },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(
                                imageVector = if (showAddStopField) Icons.Default.RemoveCircleOutline else Icons.Default.AddLocationAlt,
                                contentDescription = null,
                                tint = SafetyAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showAddStopField) "Cancel Stop" else "+ Add Stop",
                                fontSize = 10.sp,
                                color = SafetyAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Added intermediate stops chips
                    if (intermediateStops.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            intermediateStops.forEachIndexed { idx, stop ->
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
                                            imageVector = Icons.Default.PinDrop,
                                            contentDescription = null,
                                            tint = SafetyAmberDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Stop ${idx + 1}: $stop",
                                            fontSize = 11.sp,
                                            color = HighContrastWhite,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            intermediateStops = intermediateStops.filterIndexed { i, _ -> i != idx }
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove stop",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Expandable Intermediate Stop Input
                    AnimatedVisibility(visible = showAddStopField) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newStopInput,
                                    onValueChange = { newStopInput = it },
                                    placeholder = { Text("e.g. Brass Junction, Aba", color = MutedSilver, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = HighContrastWhite,
                                        unfocusedTextColor = HighContrastWhite,
                                        focusedBorderColor = SafetyAmber,
                                        unfocusedBorderColor = AsphaltDivider,
                                        focusedContainerColor = AsphaltCard,
                                        unfocusedContainerColor = AsphaltCard
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (newStopInput.isNotBlank()) {
                                            intermediateStops = intermediateStops + newStopInput.trim()
                                            newStopInput = ""
                                            showAddStopField = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // DESTINATION HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(SafetyAmber, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DROP-OFF (DESTINATION)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyAmber,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Destination Input TextField
                    OutlinedTextField(
                        value = destMarketInput,
                        onValueChange = { destMarketInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_location_input"),
                        placeholder = { Text("Where to? Market, Junction, or Street", color = MutedSilver.copy(alpha = 0.7f), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null, tint = SafetyAmber)
                        },
                        trailingIcon = {
                            if (destMarketInput.isNotEmpty()) {
                                IconButton(onClick = { destMarketInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MutedSilver, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite,
                            focusedBorderColor = SafetyAmber,
                            unfocusedBorderColor = AsphaltDivider,
                            focusedContainerColor = AsphaltCard,
                            unfocusedContainerColor = AsphaltCard
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Destination LGA Selector & Quick Market Chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Destination LGA: ${destLgaObj?.name ?: "Abia"}",
                        fontSize = 11.sp,
                        color = MutedSilver
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("aba_north" to "Aba North", "aba_south" to "Aba South", "umuahia_south" to "Umuahia S", "ikwuano" to "Ikwuano", "osisioma" to "Osisioma").forEach { (id, label) ->
                            val isSel = destLgaId == id
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    destLgaId = id
                                    val firstMarket = AbiaCorridorData.getLgaById(id)?.keyMarketsAndJunctions?.firstOrNull()
                                    if (firstMarket != null) destMarketInput = firstMarket
                                },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SafetyAmber,
                                    selectedLabelColor = AsphaltDark,
                                    containerColor = AsphaltCard,
                                    labelColor = HighContrastWhite
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    // Destination Market Quick Selection Chips
                    val currentDestMarkets = destLgaObj?.keyMarketsAndJunctions ?: emptyList()
                    if (currentDestMarkets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currentDestMarkets.take(4).forEach { market ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (destMarketInput == market) SafetyAmber.copy(alpha = 0.2f) else AsphaltCard,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 0.5.dp,
                                        color = if (destMarketInput == market) SafetyAmber else AsphaltDivider
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { destMarketInput = market }
                                ) {
                                    Text(
                                        text = market,
                                        fontSize = 10.sp,
                                        color = if (destMarketInput == market) SafetyAmberLight else HighContrastWhite,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note to Keke Driver / Special Instructions
            OutlinedTextField(
                value = rideNote,
                onValueChange = { rideNote = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ride_note_input"),
                placeholder = { Text("Note for rider (e.g. Near the chemist, carrying small carton)", color = MutedSilver.copy(alpha = 0.7f), fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = MutedSilver) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = HighContrastWhite,
                    unfocusedTextColor = HighContrastWhite,
                    focusedBorderColor = SafetyAmber,
                    unfocusedBorderColor = AsphaltDivider,
                    focusedContainerColor = AsphaltSurface,
                    unfocusedContainerColor = AsphaltSurface
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // FARE ESTIMATION & P2P BID BAR
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = AsphaltSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "RECOMMENDED FARE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedSilver
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₦$customFareOffer",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreenLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${destLgaObj?.name ?: "Corridor"})",
                                fontSize = 10.sp,
                                color = MutedSilver
                            )
                        }
                    }

                    // Fare adjustment stepper (- / +)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledIconButton(
                            onClick = { if (customFareOffer > 200) customFareOffer -= 50 },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = AsphaltCard),
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("decrease_fare_button")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease fare", tint = HighContrastWhite, modifier = Modifier.size(16.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AsphaltCard,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "₦$customFareOffer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyAmber,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        FilledIconButton(
                            onClick = { customFareOffer += 50 },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = AsphaltCard),
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("increase_fare_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase fare", tint = HighContrastWhite, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // PRIMARY ACTION BUTTON: BROADCAST REQUEST
            // ==========================================
            Button(
                onClick = {
                    val finalOrigin = originMarketInput.ifBlank { "Bata Junction, Aba" }
                    val finalDest = destMarketInput.ifBlank { "Ariaria Market, Aba" }
                    onConfirmRequest(
                        serviceMode,
                        originLgaId,
                        finalOrigin,
                        destLgaId,
                        finalDest,
                        intermediateStops,
                        customFareOffer,
                        rideNote
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_keke_request_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafetyAmber,
                    contentColor = AsphaltDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = when (serviceMode) {
                        ServiceMode.PASSENGER -> Icons.Default.ElectricRickshaw
                        ServiceMode.WAYBILL -> Icons.Default.Inventory2
                        ServiceMode.DAY_HIRE -> Icons.Default.AccessTime
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (serviceMode) {
                        ServiceMode.PASSENGER -> "Broadcast Keke Request • ₦$customFareOffer"
                        ServiceMode.WAYBILL -> "Dispatch Waybill Keke • ₦$customFareOffer"
                        ServiceMode.DAY_HIRE -> "Book Day-Hire Keke • ₦$customFareOffer"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
