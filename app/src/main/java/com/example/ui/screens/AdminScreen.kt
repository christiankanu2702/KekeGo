package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import com.example.data.local.DeviceTelemetryEntity
import com.example.data.models.*
import com.example.data.repository.KekeGoRepository
import com.example.ui.theme.*
import com.example.utils.DeviceTelemetryTracker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    repository: KekeGoRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Fleet, 1: SOS Distress, 2: Disputes, 3: Drivers Governance, 4: Device Audit, 5: 17 LGAs Grid

    val fleetVehicles by repository.fleetVehicles.collectAsState()
    val sosAlerts by repository.sosAlerts.collectAsState()
    val disputeTickets by repository.disputeTickets.collectAsState()
    val telemetryLogs by repository.getDeviceTelemetryLogsFlow().collectAsState(initial = emptyList())

    var selectedVehicleForDetail by remember { mutableStateOf<KekeFleetVehicle?>(null) }
    var selectedDisputeForAction by remember { mutableStateOf<DisputeTicket?>(null) }
    var selectedSosForResolution by remember { mutableStateOf<SosEmergencyAlert?>(null) }
    var selectedTelemetryForInspect by remember { mutableStateOf<DeviceTelemetryEntity?>(null) }

    // Proactively capture local device telemetry on screen launch for demonstration
    LaunchedEffect(Unit) {
        repository.recordDeviceAudit(
            userRole = "ADMIN",
            eventId = "OPS_SESSION_INIT",
            isMockLocation = false
        )
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
                                text = "KekeGo Operations Tower",
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
                                    text = "OWNER CONTROL",
                                    color = EmeraldGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Abia Statewide Fleet Dispatch • v${com.example.BuildConfig.VERSION_NAME} (Build #${com.example.BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                    }
                },
                actions = {
                    val activeSosCount = sosAlerts.count { it.status == "ACTIVE" }
                    if (activeSosCount > 0) {
                        Surface(
                            color = Color(0xFFFF5252).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
                            modifier = Modifier
                                .clickable { selectedTab = 1 }
                                .padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Emergency, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$activeSosCount SOS ACTIVE", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High-Level KPIs Bar
            Surface(
                color = AsphaltCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    KpiStatItem("Active Fleet", "${fleetVehicles.count { it.isOnline }} Kekes", EmeraldGreen)
                    KpiStatItem("Avg Rating", "4.8 ★", SafetyAmber)
                    KpiStatItem("Disputes", "${disputeTickets.count { it.status == "OPEN" }} Open", if (disputeTickets.any { it.status == "OPEN" }) Color(0xFFFFB300) else MutedSilver)
                    KpiStatItem("Tracked Devices", "${((telemetryLogs.size + 4))} Devices", EmeraldGreenLight)
                }
            }

            // Scrollable Operations Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = AsphaltDark,
                contentColor = SafetyAmber,
                edgePadding = 14.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SafetyAmber,
                        height = 3.dp
                    )
                }
            ) {
                listOf(
                    "Live Fleet (${fleetVehicles.size})" to Icons.Default.DirectionsCar,
                    "SOS Safety (${sosAlerts.count { it.status == "ACTIVE" }})" to Icons.Default.Emergency,
                    "Disputes (${disputeTickets.count { it.status == "OPEN" }})" to Icons.Default.SupportAgent,
                    "Driver Governance" to Icons.Default.VerifiedUser,
                    "Master Users Registry" to Icons.Default.Groups,
                    "Device Telemetry Audit" to Icons.Default.Smartphone,
                    "17 LGAs Grid" to Icons.Default.Map
                ).forEachIndexed { index, (label, icon) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (selectedTab == index) SafetyAmber else MutedSilver)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) SafetyAmber else MutedSilver
                                )
                            }
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> LiveFleetTrackingTab(
                    fleetVehicles = fleetVehicles,
                    onInspectVehicle = { selectedVehicleForDetail = it }
                )
                1 -> SosSafetyAlertsTab(
                    sosAlerts = sosAlerts,
                    onDispatchPolice = { alert ->
                        repository.dispatchEmergencyUnits(alert.id)
                    },
                    onResolveAlert = { selectedSosForResolution = it }
                )
                2 -> CustomerDisputesTab(
                    disputeTickets = disputeTickets,
                    onInspectDispute = { selectedDisputeForAction = it }
                )
                3 -> DriverGovernanceTab(
                    fleetVehicles = fleetVehicles,
                    onToggleSuspension = { driverId ->
                        repository.toggleDriverSuspension(driverId)
                    },
                    onIssueWarning = { driverId ->
                        repository.issueDriverWarning(driverId)
                    }
                )
                4 -> MasterUsersRegistryTab(onCallUser = { phone -> val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")); context.startActivity(dialIntent) })
                5 -> DeviceTelemetryAuditTab(
                    logs = telemetryLogs,
                    onInspect = { selectedTelemetryForInspect = it }
                )
                6 -> LgasCorridorGridTab()
            }
        }
    }

    // Vehicle Detail Modal
    selectedVehicleForDetail?.let { vehicle ->
        VehicleTelemetryDetailModal(
            vehicle = vehicle,
            onDismiss = { selectedVehicleForDetail = null },
            onToggleSuspension = {
                repository.toggleDriverSuspension(vehicle.driverId)
                selectedVehicleForDetail = null
            }
        )
    }

    // Dispute Resolution Modal
    selectedDisputeForAction?.let { ticket ->
        DisputeResolutionModal(
            ticket = ticket,
            onDismiss = { selectedDisputeForAction = null },
            onIssueRefund = {
                coroutineScope.launch {
                    repository.resolveDisputeWithRefund(ticket.id)
                    selectedDisputeForAction = null
                }
            },
            onDismissTicket = {
                repository.dismissDispute(ticket.id)
                selectedDisputeForAction = null
            }
        )
    }

    // SOS Resolution Modal
    selectedSosForResolution?.let { alert ->
        SosResolutionModal(
            alert = alert,
            onDismiss = { selectedSosForResolution = null },
            onConfirmResolve = { note ->
                repository.resolveSosAlert(alert.id, note)
                selectedSosForResolution = null
            }
        )
    }

    // Telemetry Inspect Modal
    selectedTelemetryForInspect?.let { log ->
        DeviceTelemetryDetailModal(
            log = log,
            onDismiss = { selectedTelemetryForInspect = null }
        )
    }
}

@Composable
private fun KpiStatItem(title: String, value: String, valueColor: Color) {
    Column {
        Text(title, color = MutedSilver, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

// ----------------------------------------------------
// TAB 0: LIVE FLEET TRACKING
// ----------------------------------------------------
@Composable
private fun LiveFleetTrackingTab(
    fleetVehicles: List<KekeFleetVehicle>,
    onInspectVehicle: (KekeFleetVehicle) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Radar, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Real-Time Corridor Telemetry", color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("LIVE GPS PING", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Tracking all registered commercial tricycles across Aba North/South, Umuahia, and arterial corridors.",
                        color = MutedSilver,
                        fontSize = 11.sp
                    )
                }
            }
        }

        items(fleetVehicles) { vehicle ->
            Surface(
                color = AsphaltCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (vehicle.isSuspended) Color(0xFFFF5252).copy(alpha = 0.5f) else AsphaltDivider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInspectVehicle(vehicle) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (vehicle.isOnline) EmeraldGreen.copy(alpha = 0.2f) else AsphaltDivider),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricRickshaw,
                                contentDescription = null,
                                tint = if (vehicle.isOnline) EmeraldGreen else MutedSilver,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(vehicle.driverName, color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (vehicle.isSuspended) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFFF5252).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text("SUSPENDED", color = Color(0xFFFF5252), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Text("${vehicle.tricyclePlateNo} • ${vehicle.currentMarketLocation}", color = MutedSilver, fontSize = 11.sp)
                            Text("Speed: ${vehicle.speedKmh} km/h • Status: ${vehicle.currentTripStatus}", color = SafetyAmber, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("${vehicle.ratingScore} ★", color = SafetyAmber, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("${vehicle.totalTripsCompleted} trips", color = MutedSilver, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: SOS SAFETY ALERTS
// ----------------------------------------------------
@Composable
private fun SosSafetyAlertsTab(
    sosAlerts: List<SosEmergencyAlert>,
    onDispatchPolice: (SosEmergencyAlert) -> Unit,
    onResolveAlert: (SosEmergencyAlert) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (sosAlerts.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No Emergency SOS Triggers Reported", color = MutedSilver, fontSize = 12.sp)
                }
            }
        } else {
            items(sosAlerts) { alert ->
                val timeStr = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(alert.timestampEpoch))
                val isUrgent = alert.status == "ACTIVE"

                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isUrgent) Color(0xFF2A1010) else AsphaltCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isUrgent) Color(0xFFFF5252) else AsphaltDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Emergency, contentDescription = null, tint = if (isUrgent) Color(0xFFFF5252) else SafetyAmber)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EMERGENCY DISTRESS SIGNAL", color = if (isUrgent) Color(0xFFFF5252) else HighContrastWhite, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                            Surface(
                                color = if (isUrgent) Color(0xFFFF5252).copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(alert.status, color = if (isUrgent) Color(0xFFFF5252) else EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(alert.senderName, color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Location: ${alert.locationLga} (${alert.lat}° N, ${alert.lng}° E)", color = SafetyAmber, fontSize = 11.sp)
                        Text("Triggered: $timeStr • Note: ${alert.dispatchNote}", color = MutedSilver, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${alert.senderPhone}"))
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HighContrastWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (alert.status == "ACTIVE") {
                                Button(
                                    onClick = { onDispatchPolice(alert) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = HighContrastWhite),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.LocalPolice, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dispatch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onResolveAlert(alert) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Resolve Incident", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: CUSTOMER DISPUTES & REFUNDS
// ----------------------------------------------------
@Composable
private fun CustomerDisputesTab(
    disputeTickets: List<DisputeTicket>,
    onInspectDispute: (DisputeTicket) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(disputeTickets) { ticket ->
            val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(ticket.createdAtEpoch))
            val isOpen = ticket.status == "OPEN"

            Card(
                colors = CardDefaults.cardColors(containerColor = AsphaltCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isOpen) SafetyAmber.copy(alpha = 0.6f) else AsphaltDivider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInspectDispute(ticket) }
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
                                text = "CATEGORY: ${ticket.category.replace("_", " ")}",
                                color = SafetyAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "₦${ticket.amountInDisputeNaira}",
                            color = if (isOpen) Color(0xFFFF5252) else EmeraldGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(ticket.description, color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Passenger: ${ticket.passengerName} • Driver: ${ticket.driverName} • Trip #${ticket.tripId}",
                        color = MutedSilver,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reported: $dateStr", color = MutedSilver, fontSize = 10.sp)
                        Surface(
                            color = when (ticket.status) {
                                "OPEN" -> SafetyAmber.copy(alpha = 0.2f)
                                "REFUNDED" -> EmeraldGreen.copy(alpha = 0.2f)
                                else -> AsphaltDivider
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = ticket.status,
                                color = when (ticket.status) {
                                    "OPEN" -> SafetyAmber
                                    "REFUNDED" -> EmeraldGreen
                                    else -> MutedSilver
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: DRIVER GOVERNANCE & RATINGS
// ----------------------------------------------------
@Composable
private fun DriverGovernanceTab(
    fleetVehicles: List<KekeFleetVehicle>,
    onToggleSuspension: (String) -> Unit,
    onIssueWarning: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(fleetVehicles) { vehicle ->
            Surface(
                color = AsphaltCard,
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
                        Column {
                            Text(vehicle.driverName, color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Plate: ${vehicle.tricyclePlateNo} • Phone: ${vehicle.driverPhone}", color = MutedSilver, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${vehicle.ratingScore} ★", color = SafetyAmber, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            Text("${vehicle.totalTripsCompleted} rides", color = MutedSilver, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Warnings: ", color = MutedSilver, fontSize = 11.sp)
                        Text("${vehicle.activeWarningCount}", color = if (vehicle.activeWarningCount > 0) Color(0xFFFF5252) else EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Network Status: ", color = MutedSilver, fontSize = 11.sp)
                        Text(if (vehicle.isSuspended) "SUSPENDED" else "APPROVED", color = if (vehicle.isSuspended) Color(0xFFFF5252) else EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onIssueWarning(vehicle.driverId) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyAmber)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Warn (+1)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onToggleSuspension(vehicle.driverId) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (vehicle.isSuspended) EmeraldGreen else Color(0xFFFF5252),
                                contentColor = if (vehicle.isSuspended) AsphaltDark else HighContrastWhite
                            )
                        ) {
                            Text(if (vehicle.isSuspended) "Reinstate" else "Suspend", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 4: DEVICE TELEMETRY AUDIT & FRAUD DETECTION
// ----------------------------------------------------
@Composable
private fun DeviceTelemetryAuditTab(
    logs: List<DeviceTelemetryEntity>,
    onInspect: (DeviceTelemetryEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hardware & Device Forensics", color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("ANTI-FRAUD ACTIVE", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Forensic logs of Android IDs, persistent hardware UUIDs, battery depletion patterns, and mock GPS location spoofing checks.",
                        color = MutedSilver,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    Text("No Telemetry Records Yet. Event triggers will automatically log here.", color = MutedSilver, fontSize = 12.sp)
                }
            }
        } else {
            items(logs) { log ->
                val timeStr = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(log.capturedAtEpoch))
                Surface(
                    color = AsphaltCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (log.isMockLocation) Color(0xFFFF5252) else AsphaltDivider
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInspect(log) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (log.isMockLocation) Icons.Default.GpsOff else Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = if (log.isMockLocation) Color(0xFFFF5252) else SafetyAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.phoneModel,
                                    color = HighContrastWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Surface(
                                color = if (log.isMockLocation) Color(0xFFFF5252).copy(alpha = 0.2f) else EmeraldGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (log.isMockLocation) "SPOOF DETECTED" else "LEGIT HARDWARE",
                                    color = if (log.isMockLocation) Color(0xFFFF5252) else EmeraldGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Device Hardware ID: ${log.deviceHardwareId}", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Android ID: ${log.androidId} • OS: ${log.osVersion}", color = MutedSilver, fontSize = 10.sp)
                        Text("Battery: ${log.batteryPercent}% (${if (log.isCharging) "Charging" else "Battery"}) • Network: ${log.networkType}", color = HighContrastWhite, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Logged on Event: ${log.associatedTripOrEventId} • $timeStr", color = MutedSilver, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 5: 17 LGAS CORRIDOR GRID
// ----------------------------------------------------
@Composable
private fun LgasCorridorGridTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(AbiaCorridorData.ALL_17_LGAS) { lga ->
            Surface(
                color = AsphaltCard,
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
                            Text(lga.name, color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = SafetyAmber.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text(lga.zoneCategory, color = SafetyAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text("${lga.activeKekeFleetCount} Kekes", color = EmeraldGreen, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Markets: ${lga.keyMarketsAndJunctions.joinToString(", ")}", color = MutedSilver, fontSize = 11.sp)
                    Text("Centroid: ${lga.centerLat}° N, ${lga.centerLng}° E • Base Fare: ₦${lga.baseFareNaira}", color = SafetyAmber, fontSize = 10.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// MODAL DIALOGS
// ----------------------------------------------------
@Composable
private fun VehicleTelemetryDetailModal(
    vehicle: KekeFleetVehicle,
    onDismiss: () -> Unit,
    onToggleSuspension: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Column {
                Text(vehicle.driverName, color = HighContrastWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("${vehicle.tricyclePlateNo} • ${vehicle.lgaId.uppercase()}", color = SafetyAmber, fontSize = 12.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = AsphaltCard, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Current Telemetry Position", color = MutedSilver, fontSize = 10.sp)
                        Text(vehicle.currentMarketLocation, color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("GPS: ${vehicle.lat}° N, ${vehicle.lng}° E • Speed: ${vehicle.speedKmh} km/h", color = EmeraldGreen, fontSize = 11.sp)
                    }
                }
                Surface(color = AsphaltCard, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Quality & Compliance", color = MutedSilver, fontSize = 10.sp)
                        Text("Passenger Rating: ${vehicle.ratingScore} / 5.0", color = SafetyAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Lifetime Trips Completed: ${vehicle.totalTripsCompleted}", color = HighContrastWhite, fontSize = 12.sp)
                        Text("Violation Warnings: ${vehicle.activeWarningCount}", color = if (vehicle.activeWarningCount > 0) Color(0xFFFF5252) else EmeraldGreen, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onToggleSuspension,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vehicle.isSuspended) EmeraldGreen else Color(0xFFFF5252),
                    contentColor = if (vehicle.isSuspended) AsphaltDark else HighContrastWhite
                )
            ) {
                Text(if (vehicle.isSuspended) "Reinstate Driver" else "Suspend from Network", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MutedSilver)
            }
        }
    )
}

@Composable
private fun DisputeResolutionModal(
    ticket: DisputeTicket,
    onDismiss: () -> Unit,
    onIssueRefund: () -> Unit,
    onDismissTicket: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Text("Dispute Ticket #${ticket.id}", color = HighContrastWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = AsphaltCard, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Passenger: ${ticket.passengerName}", color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Driver: ${ticket.driverName} • Trip #${ticket.tripId}", color = MutedSilver, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ticket.description, color = SafetyAmber, fontSize = 12.sp)
                    }
                }
                Text("Disputed Amount: ₦${ticket.amountInDisputeNaira}", color = HighContrastWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        },
        confirmButton = {
            if (ticket.status == "OPEN") {
                Button(
                    onClick = onIssueRefund,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark)
                ) {
                    Text("Refund ₦${ticket.amountInDisputeNaira} to Wallet", fontWeight = FontWeight.Bold)
                }
            } else {
                Text(ticket.status, color = EmeraldGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (ticket.status == "OPEN") {
                TextButton(onClick = onDismissTicket) {
                    Text("Dismiss Ticket", color = Color(0xFFFF5252))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = MutedSilver)
                }
            }
        }
    )
}

@Composable
private fun SosResolutionModal(
    alert: SosEmergencyAlert,
    onDismiss: () -> Unit,
    onConfirmResolve: (String) -> Unit
) {
    var note by remember { mutableStateOf("Police confirmed driver safe") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Text("Resolve Incident #${alert.id}", color = HighContrastWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Confirm safety resolution for ${alert.senderName}:", color = MutedSilver, fontSize = 12.sp)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Resolution Log") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HighContrastWhite,
                        unfocusedTextColor = HighContrastWhite,
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = AsphaltDivider
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmResolve(note) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark)
            ) {
                Text("Mark Resolved", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedSilver)
            }
        }
    )
}

@Composable
private fun DeviceTelemetryDetailModal(
    log: DeviceTelemetryEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Column {
                Text("Device Telemetry Forensic Log", color = HighContrastWhite, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(log.phoneModel, color = SafetyAmber, fontSize = 12.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = AsphaltCard, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Hardware Identification", color = MutedSilver, fontSize = 10.sp)
                        Text("Hardware ID: ${log.deviceHardwareId}", color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Android ID: ${log.androidId}", color = HighContrastWhite, fontSize = 11.sp)
                        Text("OS Version: ${log.osVersion}", color = MutedSilver, fontSize = 10.sp)
                        Text("Display: ${log.screenResolution}", color = MutedSilver, fontSize = 10.sp)
                    }
                }
                Surface(color = AsphaltCard, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Device State & Security", color = MutedSilver, fontSize = 10.sp)
                        Text("Battery Level: ${log.batteryPercent}% (${if (log.isCharging) "Charging" else "Battery"})", color = SafetyAmber, fontSize = 11.sp)
                        Text("Network Type: ${log.networkType}", color = HighContrastWhite, fontSize = 11.sp)
                        Text(
                            text = if (log.isMockLocation) "ALERT: Mock Location Spoofing Detected!" else "Location Authenticity: Verified Genuine GPS",
                            color = if (log.isMockLocation) Color(0xFFFF5252) else EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ----------------------------------------------------
// TAB: MASTER USERS REGISTRY & GOVERNANCE
// ----------------------------------------------------
@Composable
private fun MasterUsersRegistryTab(
    onCallUser: (String) -> Unit
) {
    val sampleUsers = listOf(
        Triple("Christian Kanu", "christiankanu2702@gmail.com", "SUPER ADMIN • State Authority"),
        Triple("Kalu Driver", "08039821033", "RIDER • Plate ABA-882-KU • ATRWAN Zone 2"),
        Triple("Chinedu Okeke", "08149811234", "RIDER • Plate ABA-419-KU • ATRWAN Zone 1"),
        Triple("Captain Eze", "08051122334", "PARK CAPTAIN • Bata Central Terminal"),
        Triple("Ngozi Trader", "07089900112", "PASSENGER • Aba South • ₦14,200 Wallet"),
        Triple("Amaka Clothier", "09012233445", "PASSENGER (MERCHANT) • Ariaria Market • ₦8,500 Wallet")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AsphaltCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Master User & Role Directory", color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Surface(color = EmeraldGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text("SUPER ADMIN ROOT", color = EmeraldGreen, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Direct oversight and communications access across all passengers, registered riders, and park captains.", color = MutedSilver, fontSize = 11.sp)
                }
            }
        }

        items(sampleUsers) { (name, contact, roleDetails) ->
            Surface(
                color = AsphaltCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(roleDetails, color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Contact: $contact", color = MutedSilver, fontSize = 10.sp)
                    }

                    if (contact.startsWith("0")) {
                        IconButton(
                            onClick = { onCallUser(contact) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(EmeraldGreen.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, EmeraldGreen, CircleShape)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
