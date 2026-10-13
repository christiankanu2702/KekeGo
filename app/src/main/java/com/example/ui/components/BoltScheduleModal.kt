package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.models.ServiceMode
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BoltScheduleModal(
    originMarket: String,
    originLgaId: String,
    destinationMarket: String,
    destinationLgaId: String,
    baseFareNaira: Int,
    onDismiss: () -> Unit,
    onConfirmSchedule: (pickupEpoch: Long, isRecurring: Boolean, rule: String, mode: ServiceMode) -> Unit
) {
    var selectedHoursAhead by remember { mutableIntStateOf(2) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringRule by remember { mutableStateOf("DAILY") } // DAILY, WEEKDAYS, WEEKLY
    var selectedMode by remember { mutableStateOf(ServiceMode.PASSENGER) }

    val calendar = Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, selectedHoursAhead)
    }
    val timeFormat = SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault())
    val formattedTargetTime = timeFormat.format(calendar.time)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = SafetyAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Schedule a Ride",
                    color = HighContrastWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Trip route preview
                Surface(
                    color = AsphaltCard,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TripOrigin, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(originMarket, color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = SafetyAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(destinationMarket, color = HighContrastWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Pickup Time selection
                Text("Select Pickup Window:", color = MutedSilver, fontSize = 12.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(1 to "in 1 hr", 2 to "in 2 hrs", 4 to "in 4 hrs", 12 to "Tomorrow").forEach { (hours, label) ->
                        val isSel = selectedHoursAhead == hours
                        Surface(
                            color = if (isSel) SafetyAmber else AsphaltCard,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) SafetyAmber else AsphaltDivider),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedHoursAhead = hours }
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) AsphaltDark else HighContrastWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Surface(
                    color = AsphaltSurface,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Scheduled Pickup: $formattedTargetTime",
                        color = SafetyAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // Recurring switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isRecurring = !isRecurring },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Repeat This Ride", color = HighContrastWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Auto-dispatch daily for market runs", color = MutedSilver, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AsphaltDark,
                            checkedTrackColor = SafetyAmber,
                            uncheckedThumbColor = MutedSilver,
                            uncheckedTrackColor = AsphaltCard
                        )
                    )
                }

                if (isRecurring) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("DAILY" to "Daily", "WEEKDAYS" to "Mon-Fri", "WEEKLY" to "Weekly").forEach { (rule, label) ->
                            val isSel = recurringRule == rule
                            Surface(
                                color = if (isSel) EmeraldGreen else AsphaltCard,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { recurringRule = rule }
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) AsphaltDark else HighContrastWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmSchedule(calendar.timeInMillis, isRecurring, recurringRule, selectedMode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Scheduled Ride (₦$baseFareNaira)", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = MutedSilver)
            }
        }
    )
}
