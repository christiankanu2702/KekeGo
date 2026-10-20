package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
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
import com.example.ui.theme.*
import com.example.utils.GeminiTransitAssistant
import com.example.utils.WaybillAnalysis
import kotlinx.coroutines.launch

@Composable
fun GeminiWaybillAssistantDialog(
    cargoDescription: String,
    originMarket: String,
    destinationMarket: String,
    onApplySuggestedFare: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var analysis by remember { mutableStateOf<WaybillAnalysis?>(null) }

    LaunchedEffect(cargoDescription) {
        coroutineScope.launch {
            isLoading = true
            analysis = GeminiTransitAssistant.analyzeWaybillCargo(
                context = context,
                cargoDescription = cargoDescription,
                originMarket = originMarket,
                destinationMarket = destinationMarket
            )
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8A2BE2).copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFBA68C8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text("Gemini AI Cargo Analysis", color = HighContrastWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("Intelligent Volumetric & Tariff Valuation", color = Color(0xFFBA68C8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = Color(0xFFBA68C8))
                    Text("Gemini is analyzing cargo dimensions & transit corridor tariffs...", color = MutedSilver, fontSize = 12.sp)
                }
            } else if (analysis != null) {
                val res = analysis!!
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Identified Category:", color = MutedSilver, fontSize = 11.sp)
                                Text(res.cargoCategory, color = HighContrastWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Suggested Corridor Fare:", color = MutedSilver, fontSize = 11.sp)
                                Text("₦${res.suggestedFareNaira}", color = Color(0xFF00E676), fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Recommended Seat Space:", color = MutedSilver, fontSize = 11.sp)
                                Text("${res.recommendedSeats} Seat${if (res.recommendedSeats > 1) "s" else ""}", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimated Weight:", color = MutedSilver, fontSize = 11.sp)
                                Text("~${res.estimatedWeightKg} KG", color = HighContrastWhite, fontSize = 11.sp)
                            }
                        }
                    }

                    Text(
                        text = "💡 Advice for Rider: ${res.handlingAdvice}",
                        color = MutedSilver,
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            if (!isLoading && analysis != null) {
                Button(
                    onClick = {
                        onApplySuggestedFare(analysis!!.suggestedFareNaira, analysis!!.recommendedSeats)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = AsphaltDark)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Suggested ₦${analysis!!.suggestedFareNaira}", fontWeight = FontWeight.Black)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MutedSilver)
            }
        }
    )
}
