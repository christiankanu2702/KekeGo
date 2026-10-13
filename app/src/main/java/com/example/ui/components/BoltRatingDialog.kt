package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun BoltRatingDialog(
    tripId: String,
    driverName: String,
    tricyclePlate: String,
    onDismiss: () -> Unit,
    onSubmitRating: (stars: Int, tags: List<String>, comment: String, tip: Int) -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(5) }
    val selectedTags = remember { mutableStateListOf<String>() }
    var commentText by remember { mutableStateOf("") }
    var selectedTip by remember { mutableIntStateOf(0) }

    val availableTags = listOf(
        "Clean Keke",
        "Safe Driving",
        "Polite Driver",
        "Fair Price",
        "Punctual Pickup",
        "Good Music",
        "Helpful with Cargo"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SafetyAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = SafetyAmber,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rate your Ride",
                    color = HighContrastWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "$driverName • $tricyclePlate",
                    color = MutedSilver,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Star selector
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { starIndex ->
                        val isFilled = starIndex <= selectedStars
                        IconButton(
                            onClick = { selectedStars = starIndex },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$starIndex Stars",
                                tint = if (isFilled) SafetyAmber else MutedSilver,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when (selectedStars) {
                        5 -> "Excellent trip!"
                        4 -> "Good experience"
                        3 -> "Average ride"
                        2 -> "Could be better"
                        else -> "Poor experience"
                    },
                    color = SafetyAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                // Compliment chips
                Text(
                    text = "Select Compliment Tags:",
                    color = HighContrastWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                OptInChipWrap(
                    tags = availableTags,
                    selectedTags = selectedTags,
                    onToggleTag = { tag ->
                        if (selectedTags.contains(tag)) {
                            selectedTags.remove(tag)
                        } else {
                            selectedTags.add(tag)
                        }
                    }
                )

                // Tip rider row
                Text(
                    text = "Add a Tip for Driver (Optional):",
                    color = HighContrastWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(0, 100, 200, 500).forEach { tipAmount ->
                        val isSel = selectedTip == tipAmount
                        Surface(
                            color = if (isSel) EmeraldGreen else AsphaltCard,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldGreen else AsphaltDivider),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTip = tipAmount }
                        ) {
                            Text(
                                text = if (tipAmount == 0) "No tip" else "₦$tipAmount",
                                color = if (isSel) AsphaltDark else HighContrastWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Optional comment
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Write feedback for the rider...", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HighContrastWhite,
                        unfocusedTextColor = HighContrastWhite,
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = AsphaltDivider
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitRating(selectedStars, selectedTags.toList(), commentText, selectedTip)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Review & Tip", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for Now", color = MutedSilver)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptInChipWrap(
    tags: List<String>,
    selectedTags: List<String>,
    onToggleTag: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        tags.forEach { tag ->
            val isSelected = selectedTags.contains(tag)
            Surface(
                color = if (isSelected) SafetyAmber else AsphaltCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) SafetyAmber else AsphaltDivider),
                modifier = Modifier.clickable { onToggleTag(tag) }
            ) {
                Text(
                    text = tag,
                    color = if (isSelected) AsphaltDark else HighContrastWhite,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
