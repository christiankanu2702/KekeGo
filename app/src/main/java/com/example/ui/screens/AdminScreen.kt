package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AbiaCorridorData
import com.example.data.models.LgaZone
import com.example.data.repository.KekeGoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    repository: KekeGoRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedLga by remember { mutableStateOf<LgaZone>(AbiaCorridorData.ALL_17_LGAS.first()) }
    var erpSyncStatus by remember { mutableStateOf<String?>("Cloud Telemetry Pipeline: Ready (Target: Cloud Firestore /telemetry_audit_logs)") }
    var isSyncingAudit by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AsphaltBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Abia Statewide Transit Admin",
                            style = MaterialTheme.typography.titleLarge,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "17 LGAs Commercial Telemetry & Audit",
                            style = MaterialTheme.typography.bodySmall,
                            color = SafetyAmber
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AsphaltDark)
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
                // Statewide Key Performance Indicators
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Statewide Fleet & Telemetry Metrics",
                            style = MaterialTheme.typography.titleMedium,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(color = AsphaltCard, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Active Tricycles", color = MutedSilver, fontSize = 10.sp)
                                    Text("1,182", color = SafetyAmber, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text("17 LGAs active", color = EmeraldGreenLight, fontSize = 9.sp)
                                }
                            }
                            Surface(color = AsphaltCard, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Daily GMV", color = MutedSilver, fontSize = 10.sp)
                                    Text("₦4.82M", color = HighContrastWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text("+14.2% today", color = EmeraldGreenLight, fontSize = 9.sp)
                                }
                            }
                            Surface(color = AsphaltCard, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("OTP Waybills", color = MutedSilver, fontSize = 10.sp)
                                    Text("99.8%", color = EmeraldGreenLight, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text("Zero theft rate", color = MutedSilver, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ERP Integration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Statewide Telemetry & Audit Pipeline", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(erpSyncStatus ?: "", color = MutedSilver, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                isSyncingAudit = true
                                coroutineScope.launch {
                                    val logId = repository.pushStatewideTelemetryAudit()
                                    erpSyncStatus = "Firestore Synced: Telemetry Packet $logId recorded in collection /telemetry_audit_logs"
                                    isSyncingAudit = false
                                }
                            },
                            enabled = !isSyncingAudit,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isSyncingAudit) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AsphaltDark, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Syncing...", fontSize = 12.sp)
                            } else {
                                Text("Trigger Live Cloud Telemetry Push", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // All 17 LGAs Corridor Inspection
            item {
                Text(
                    text = "Abia State 17 LGAs Transit Grid:",
                    style = MaterialTheme.typography.titleMedium,
                    color = HighContrastWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            items(AbiaCorridorData.ALL_17_LGAS) { lga ->
                val isSelected = selectedLga.id == lga.id
                Surface(
                    onClick = { selectedLga = lga },
                    color = if (isSelected) AsphaltCard else AsphaltSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) SafetyAmber else AsphaltDivider
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = lga.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HighContrastWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = SafetyAmberDark.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = lga.zoneCategory,
                                        color = SafetyAmberLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${lga.activeKekeFleetCount} Kekes",
                                color = EmeraldGreenLight,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Key Markets: ${lga.keyMarketsAndJunctions.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSilver
                        )
                        Text(
                            text = "Base Corridor Fare: ₦${lga.baseFareNaira} • Coordinates: ${lga.centerLat}° N, ${lga.centerLng}° E",
                            style = MaterialTheme.typography.bodySmall,
                            color = SafetyAmber,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
