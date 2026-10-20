package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.firebase.FirebaseManager
import com.example.data.models.AbiaCorridorData
import com.example.data.models.DriverServiceFilter
import com.example.data.models.UserRole
import com.example.ui.theme.*
import com.example.utils.MlKitDocumentScanner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.SmartToy
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onSignInSuccess: (UserRole) -> Unit,
    onDemoSignIn: (UserRole) -> Unit = onSignInSuccess,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isScanningPermit by remember { mutableStateOf(false) }
    var scanSuccessMessage by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf(UserRole.PASSENGER) }

    // Role-Specific Compliance Registration Fields
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var homeLgaId by remember { mutableStateOf("aba_south") }

    // Driver Specific
    var riderPermitId by remember { mutableStateOf("AB/KKE/2026/8941") }
    var tricyclePlateNo by remember { mutableStateOf("ABA-882-KU") }
    var unionBranchCode by remember { mutableStateOf("ATRWAN Zone 2 (Aba Central)") }
    var chassisNumber by remember { mutableStateOf("BAJAJ-RE-4S-782103") }
    var driverFilter by remember { mutableStateOf(DriverServiceFilter.BOTH) }

    // Park Captain Specific
    var managedParkName by remember { mutableStateOf("Bata Central Terminal Aba") }
    var assignedLgaId by remember { mutableStateOf("aba_south") }

    // Admin Specific
    var adminDepartment by remember { mutableStateOf("Abia State Ministry of Transport Operations") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isScanningPermit = true
                val result = MlKitDocumentScanner.scanDocumentFromUri(context, uri)
                if (result.detectedPlateNumber != null) {
                    tricyclePlateNo = result.detectedPlateNumber
                }
                if (result.detectedPermitNumber != null) {
                    riderPermitId = result.detectedPermitNumber
                }
                scanSuccessMessage = if (result.detectedPlateNumber != null || result.detectedPermitNumber != null) {
                    "Auto-detected: ${result.detectedPlateNumber ?: ""} ${result.detectedPermitNumber ?: ""}"
                } else {
                    "Scanned image text processed. Check extracted fields."
                }
                isScanningPermit = false
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AsphaltBlack
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Brand Header Badge
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SafetyAmber.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(SafetyAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricRickshaw,
                                contentDescription = "Keke Napep",
                                tint = AsphaltDark,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = EmeraldGreenDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "17 LGAs CERTIFIED",
                                    color = HighContrastWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ATRWAN & TOAN COMPLIANT",
                                color = SafetyAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KekeGo Registration",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SafetyAmber,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Abia State Tricycle Transit & Cargo Authority",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSilver
                    )
                }
            }

            // Select Role Tab Row
            item {
                Text(
                    text = "SELECT OPERATING ROLE AT REGISTRATION:",
                    style = MaterialTheme.typography.labelSmall,
                    color = SafetyAmber,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        UserRole.PASSENGER to "Passenger",
                        UserRole.DRIVER to "Rider",
                        UserRole.PARK_CAPTAIN to "Captain",
                        UserRole.ADMIN to "Admin"
                    ).forEach { (role, label) ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
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

            // Role-Specific Profile & Compliance Registration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (selectedRole) {
                                    UserRole.PASSENGER -> Icons.Default.Person
                                    UserRole.DRIVER -> Icons.Default.DirectionsCar
                                    UserRole.PARK_CAPTAIN -> Icons.Default.SupervisorAccount
                                    UserRole.ADMIN -> Icons.Default.Analytics
                                },
                                contentDescription = null,
                                tint = SafetyAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (selectedRole) {
                                    UserRole.PASSENGER -> "Passenger Details"
                                    UserRole.DRIVER -> "State Rider License & Vehicle Compliance"
                                    UserRole.PARK_CAPTAIN -> "Park Terminal & Union Assignment"
                                    UserRole.ADMIN -> "State Transit Governance Credentials"
                                },
                                color = HighContrastWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Common Contact Fields
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name (or Commercial Alias)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighContrastWhite,
                                unfocusedTextColor = HighContrastWhite,
                                focusedBorderColor = SafetyAmber,
                                unfocusedBorderColor = AsphaltDivider
                            )
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number (e.g. 08031234567)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighContrastWhite,
                                unfocusedTextColor = HighContrastWhite,
                                focusedBorderColor = SafetyAmber,
                                unfocusedBorderColor = AsphaltDivider
                            )
                        )

                        when (selectedRole) {
                            UserRole.PASSENGER -> {
                                Text("Primary Residence / Base LGA:", color = MutedSilver, fontSize = 11.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("aba_south" to "Aba South", "aba_north" to "Aba North", "umuahia_north" to "Umuahia").forEach { (id, label) ->
                                        FilterChip(
                                            selected = homeLgaId == id,
                                            onClick = { homeLgaId = id },
                                            label = { Text(label, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldGreen,
                                                selectedLabelColor = AsphaltDark,
                                                containerColor = AsphaltSurface,
                                                labelColor = HighContrastWhite
                                            )
                                        )
                                    }
                                }
                            }

                            UserRole.DRIVER -> {
                                OutlinedTextField(
                                    value = riderPermitId,
                                    onValueChange = { riderPermitId = it },
                                    label = { Text("Abia State Rider Permit ID", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = HighContrastWhite,
                                        unfocusedTextColor = HighContrastWhite,
                                        focusedBorderColor = SafetyAmber,
                                        unfocusedBorderColor = AsphaltDivider
                                    )
                                )

                                OutlinedTextField(
                                    value = tricyclePlateNo,
                                    onValueChange = { tricyclePlateNo = it },
                                    label = { Text("Tricycle Plate No (e.g. ABA-882-KU)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = HighContrastWhite,
                                        unfocusedTextColor = HighContrastWhite,
                                        focusedBorderColor = SafetyAmber,
                                        unfocusedBorderColor = AsphaltDivider
                                    )
                                )

                                OutlinedTextField(
                                    value = unionBranchCode,
                                    onValueChange = { unionBranchCode = it },
                                    label = { Text("Union Branch (ATRWAN / TOAN Unit)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = HighContrastWhite,
                                        unfocusedTextColor = HighContrastWhite,
                                        focusedBorderColor = SafetyAmber,
                                        unfocusedBorderColor = AsphaltDivider
                                    )
                                )

                                OutlinedTextField(
                                    value = chassisNumber,
                                    onValueChange = { chassisNumber = it },
                                    label = { Text("Engine / Chassis VIN Number", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = HighContrastWhite,
                                        unfocusedTextColor = HighContrastWhite,
                                        focusedBorderColor = SafetyAmber,
                                        unfocusedBorderColor = AsphaltDivider
                                    )
                                )
                            }

                            UserRole.PARK_CAPTAIN -> {
                                OutlinedTextField(
                                    value = managedParkName,
                                    onValueChange = { managedParkName = it },
                                    label = { Text("Assigned Transit Park / Terminal", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = HighContrastWhite,
                                        unfocusedTextColor = HighContrastWhite,
                                        focusedBorderColor = SafetyAmber,
                                        unfocusedBorderColor = AsphaltDivider
                                    )
                                )

                                Text("Terminal LGA Jurisdiction:", color = MutedSilver, fontSize = 11.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("aba_south" to "Aba South", "aba_north" to "Aba North", "osisioma" to "Osisioma").forEach { (id, label) ->
                                        FilterChip(
                                            selected = assignedLgaId == id,
                                            onClick = { assignedLgaId = id },
                                            label = { Text(label, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldGreen,
                                                selectedLabelColor = AsphaltDark,
                                                containerColor = AsphaltSurface,
                                                labelColor = HighContrastWhite
                                            )
                                        )
                                    }
                                }
                            }

                            UserRole.ADMIN -> {
                                OutlinedTextField(
                                    value = adminDepartment,
                                    onValueChange = { adminDepartment = it },
                                    label = { Text("State Governance Department", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = HighContrastWhite,
                                        unfocusedTextColor = HighContrastWhite,
                                        focusedBorderColor = SafetyAmber,
                                        unfocusedBorderColor = AsphaltDivider
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val credentialManager = CredentialManager.create(context)
                                val webClientId = context.getString(R.string.default_web_client_id)
                                val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                val result = credentialManager.getCredential(
                                    request = request,
                                    context = context as Activity
                                )
                                val credential = result.credential
                                if (credential is GoogleIdTokenCredential) {
                                    val authCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                                    val authResult = FirebaseManager.getAuth().signInWithCredential(authCredential).await()
                                    val user = authResult.user
                                    if (user != null) {
                                        onSignInSuccess(selectedRole)
                                    } else {
                                        errorMessage = "Failed to obtain Firebase user"
                                    }
                                } else {
                                    errorMessage = "Unexpected credential format"
                                }
                            } catch (e: GetCredentialCancellationException) {
                                Log.i("AuthScreen", "User dismissed Google Sign-In")
                            } catch (e: Exception) {
                                Log.e("AuthScreen", "Authentication failed", e)
                                errorMessage = "Google Sign-In failed: ${e.localizedMessage ?: "Unknown error"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_sign_in_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AsphaltDark)
                    } else {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Register with Google Account",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onDemoSignIn(selectedRole) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("offline_demo_session_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HighContrastWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Instant Demo (${selectedRole.name.replace("_", " ")})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User-Facing Version Badge
                val pVersionName = remember {
                    try {
                        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        val code = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            pInfo.longVersionCode
                        } else {
                            @Suppress("DEPRECATION")
                            pInfo.versionCode.toLong()
                        }
                        "v${pInfo.versionName ?: "1.0.0"} (Build $code)"
                    } catch (e: Exception) {
                        "v1.0.0 (Build 1)"
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KekeGo $pVersionName • Abia State Transit",
                        color = MutedSilver.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
