package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SosFloatingTrigger(
    onTriggerSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_scale"
    )

    IconButton(
        onClick = { showDialog = true },
        modifier = modifier
            .scale(pulseScale)
            .size(52.dp)
            .clip(CircleShape)
            .background(ErrorRed)
    ) {
        Icon(
            imageVector = Icons.Default.Emergency,
            contentDescription = "Emergency SOS Trigger",
            tint = HighContrastWhite,
            modifier = Modifier.size(28.dp)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = AsphaltDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ABIA TRANSIT EMERGENCY SOS",
                        style = MaterialTheme.typography.titleMedium,
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "This will transmit your live GPS coordinates, vehicle registry, and compressed operational logs to the nearest Union Park Marshals & Abia State Emergency Dispatch.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSilver
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "LIVE TELEMETRY PAYLOAD:", color = SafetyAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Coordinates: 5.1118° N, 7.3689° E (Aba South)", color = HighContrastWhite, fontSize = 12.sp)
                            Text(text = "Log: COMPRESSED_TELEMETRY_PACKET_OK", color = MutedSilver, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onTriggerSos()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = HighContrastWhite),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("TRANSMIT SOS NOW", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = MutedSilver)
                }
            }
        )
    }
}
