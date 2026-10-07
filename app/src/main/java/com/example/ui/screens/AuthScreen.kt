package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.firebase.FirebaseManager
import com.example.data.models.UserRole
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    var selectedRole by remember { mutableStateOf(UserRole.PASSENGER) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AsphaltBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // High-Performance Transit Hero Badge (Zero bitmap decoding overhead)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SafetyAmber.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SafetyAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricRickshaw,
                                contentDescription = "Keke Napep",
                                tint = AsphaltDark,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = EmeraldGreenDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "17 LGAs CERTIFIED",
                                    color = HighContrastWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATRWAN & TOAN COMPLIANT",
                                color = SafetyAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "KekeGo",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SafetyAmber,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Statewide Commercial Tricycle Logistics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HighContrastWhite,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Connecting Aba, Umuahia, and all 17 LGAs of Abia State",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSilver
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Select Transit Operating Mode:",
                    style = MaterialTheme.typography.labelLarge,
                    color = HighContrastWhite,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role selection chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Interactive Google Sign-In via Credential Manager
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val credentialManager = CredentialManager.create(context)
                                val webClientId = context.getString(R.string.default_web_client_id)

                                val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
                                    .build()

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
                                    FirebaseManager.getAuth().signInWithCredential(authCredential).await()
                                    isLoading = false
                                    onSignInSuccess(selectedRole)
                                } else {
                                    isLoading = false
                                    errorMessage = "Unsupported credential format"
                                }
                            } catch (e: GetCredentialCancellationException) {
                                Log.w("AuthScreen", "Sign-in cancelled by user")
                                isLoading = false
                            } catch (e: Exception) {
                                Log.e("AuthScreen", "Sign-in error: ${e.message}", e)
                                isLoading = false
                                errorMessage = "Sign-in note: ${e.localizedMessage ?: "Please retry or use quick access"}"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafetyAmber,
                        contentColor = AsphaltDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AsphaltDark,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connecting...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign in with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fast Direct Explorer Entry for Browser Emulator Testing
                OutlinedButton(
                    onClick = {
                        onDemoSignIn(selectedRole)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EmeraldGreenLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Instant Mode Entry (${selectedRole.name})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Abia Commercial Riders Permit & State Logistics Protocol",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSilver,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
