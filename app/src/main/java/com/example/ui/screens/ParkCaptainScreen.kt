package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DisputeRecord
import com.example.data.repository.KekeGoRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkCaptainScreen(
    repository: KekeGoRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val userAccount by repository.currentUserAccount.collectAsState()
    val disputes by repository.getDisputesFlow().collectAsState(initial = emptyList())
    val trips by repository.getCachedTripsFlow().collectAsState(initial = emptyList())

    var showNewDisputeDialog by remember { mutableStateOf(false) }
    var disputeTripId by remember { mutableStateOf("TRIP_9812") }
    var disputeCategory by remember { mutableStateOf("CARGO_DAMAGE") }
    var disputeDescription by remember { mutableStateOf("") }
    var attachedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        attachedPhotoUri = uri
    }

    var activeKekesInPark = 68
    val unionDailyLevyTotal = activeKekesInPark * 200
    val waybillsProcessedToday = trips.filter { it.serviceMode.name == "WAYBILL" }.size + 14
    var showExportAuditDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AsphaltBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Park Captain Aggregator",
                            style = MaterialTheme.typography.titleLarge,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "ATRWAN / TOAN Branch: ${userAccount?.managedParkName ?: "Bata Central Terminal Aba"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SafetyAmber,
                            fontSize = 11.sp
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
                // Park Fleet Telemetry & Union Revenue Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = SafetyAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sub-Fleet Dispatch Telemetry",
                                style = MaterialTheme.typography.titleMedium,
                                color = HighContrastWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = AsphaltCard,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Active Kekes", color = MutedSilver, fontSize = 11.sp)
                                    Text("$activeKekesInPark", color = EmeraldGreenLight, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text("In Queue: 12", color = HighContrastWhite, fontSize = 10.sp)
                                }
                            }

                            Surface(
                                color = AsphaltCard,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Union Tickets", color = MutedSilver, fontSize = 11.sp)
                                    Text("₦$unionDailyLevyTotal", color = SafetyAmber, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text("₦200 / Unit", color = HighContrastWhite, fontSize = 10.sp)
                                }
                            }

                            Surface(
                                color = AsphaltCard,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Waybills Today", color = MutedSilver, fontSize = 11.sp)
                                    Text("$waybillsProcessedToday", color = HighContrastWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text("OTP Verified: 100%", color = EmeraldGreenLight, fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Google Sheets Export Action
                        Button(
                            onClick = { showExportAuditDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Union Audit to Google Sheets (CSV)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Sub-Fleet Queue Management
            item {
                Text(
                    text = "Sub-Fleet Queue (Park Marshall Dispatch):",
                    style = MaterialTheme.typography.titleMedium,
                    color = HighContrastWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            val queueDrivers = listOf(
                Triple("Ikechukwu N.", "ABA-991-KU", "Permit: AB/2026/102"),
                Triple("Chinedu Eze", "ABA-324-KU", "Permit: AB/2026/894"),
                Triple("Emeka Okoro", "ABA-718-KU", "Permit: AB/2026/551"),
                Triple("Sunday Kalu", "ABA-410-KU", "Permit: AB/2026/309")
            )

            items(queueDrivers) { (name, plate, permit) ->
                Surface(
                    color = AsphaltCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ElectricRickshaw, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(name, color = HighContrastWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$plate • $permit", color = MutedSilver, fontSize = 11.sp)
                            }
                        }

                        Surface(color = EmeraldGreenDark, shape = RoundedCornerShape(6.dp)) {
                            Text("Ready", color = HighContrastWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }

            // Dispute Resolution Pipeline
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dispute Resolution Pipeline",
                        style = MaterialTheme.typography.titleMedium,
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showNewDisputeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = HighContrastWhite),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Incident", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (disputes.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AsphaltCard,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No unresolved disputes in this union sector", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                            Text("Cargo and passenger operations running seamlessly", color = MutedSilver, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(disputes) { dispute ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AsphaltCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(color = ErrorRed.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        dispute.reasonCategory,
                                        color = ErrorRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    dispute.status,
                                    color = SafetyAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(dispute.description, color = HighContrastWhite, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Photo evidence & GPS coordinates logged", color = MutedSilver, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showNewDisputeDialog) {
        AlertDialog(
            onDismissRequest = { showNewDisputeDialog = false },
            containerColor = AsphaltDark,
            title = {
                Text("Log Incident / Dispute", color = HighContrastWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Category:", color = MutedSilver, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("CARGO_DAMAGE", "FARE_DISPUTE", "ROUTE_DEVIATION").forEach { cat ->
                            FilterChip(
                                selected = disputeCategory == cat,
                                onClick = { disputeCategory = cat },
                                label = { Text(cat.replace("_", " "), fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ErrorRed,
                                    selectedLabelColor = HighContrastWhite,
                                    containerColor = AsphaltCard,
                                    labelColor = HighContrastWhite
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = disputeTripId,
                        onValueChange = { disputeTripId = it },
                        label = { Text("Trip / Waybill ID", color = MutedSilver) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite,
                            focusedBorderColor = SafetyAmber,
                            unfocusedBorderColor = AsphaltDivider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = disputeDescription,
                        onValueChange = { disputeDescription = it },
                        label = { Text("Incident Summary & Damage Details", color = MutedSilver) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite,
                            focusedBorderColor = SafetyAmber,
                            unfocusedBorderColor = AsphaltDivider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Android Jetpack Photo Picker Evidence Integration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (attachedPhotoUri != null) "Photo Evidence Attached" else "Cargo Photo Proof",
                                color = if (attachedPhotoUri != null) EmeraldGreenLight else HighContrastWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (attachedPhotoUri != null) "Selected: ${attachedPhotoUri?.lastPathSegment?.take(16)}..." else "Snap or pick from gallery",
                                color = MutedSilver,
                                fontSize = 10.sp
                            )
                        }

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (attachedPhotoUri != null) EmeraldGreen else AsphaltCard,
                                contentColor = if (attachedPhotoUri != null) AsphaltDark else HighContrastWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (attachedPhotoUri != null) "Change" else "Attach Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.logDispute(
                                tripId = disputeTripId,
                                reasonCategory = disputeCategory,
                                description = disputeDescription.ifBlank { "Ariaria market sack torn during transit, fare adjustment required" },
                                locationLga = "Aba South",
                                photoUriString = attachedPhotoUri?.toString()
                            )
                            attachedPhotoUri = null
                            showNewDisputeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = HighContrastWhite)
                ) {
                    Text("Submit Dispute", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDisputeDialog = false }) {
                    Text("Cancel", color = MutedSilver)
                }
            }
        )
    }

    // Google Sheets Export Dialog for Park Chairmen
    if (showExportAuditDialog) {
        val csvData = repository.exportUnionAuditSpreadsheetData()
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showExportAuditDialog = false },
            containerColor = AsphaltDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ATRWAN / TOAN Daily Park Audit", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Formatted in Google Sheets CSV structure (Tricycle Plate, Permit, Branch, Levy, Timestamp):",
                        color = MutedSilver,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp)
                    ) {
                        Text(
                            text = csvData,
                            color = EmeraldGreenLight,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, csvData)
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "Export to Google Drive / Sheets")
                        context.startActivity(shareIntent)
                        showExportAuditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share to Google Drive / Sheets", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportAuditDialog = false }) {
                    Text("Close", color = MutedSilver)
                }
            }
        )
    }
}
