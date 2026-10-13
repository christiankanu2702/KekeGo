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
import com.example.data.models.SavedPlace
import com.example.ui.theme.*

@Composable
fun SavedPlacesQuickRow(
    savedPlaces: List<SavedPlace>,
    onSelectPlace: (SavedPlace) -> Unit,
    onAddPlaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAVED PLACES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MutedSilver,
                letterSpacing = 1.sp
            )
            Text(
                text = "+ Add",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SafetyAmber,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onAddPlaceClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(savedPlaces) { place ->
                val icon = when (place.iconType) {
                    "HOME" -> Icons.Default.Home
                    "WORK" -> Icons.Default.Work
                    "MARKET" -> Icons.Default.Storefront
                    else -> Icons.Default.Place
                }

                Surface(
                    color = AsphaltCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                    modifier = Modifier.clickable { onSelectPlace(place) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SafetyAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = SafetyAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = place.title,
                                color = HighContrastWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = place.addressOrMarket.take(18) + if (place.addressOrMarket.length > 18) "..." else "",
                                color = MutedSilver,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSavedPlaceDialog(
    onDismiss: () -> Unit,
    onSavePlace: (title: String, addressOrMarket: String, lgaId: String, iconType: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("HOME") }
    var selectedLga by remember { mutableStateOf("aba_south") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        title = {
            Text("Save Favourite Place", color = HighContrastWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Label (e.g. Home, Shop, Work)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HighContrastWhite,
                        unfocusedTextColor = HighContrastWhite,
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = AsphaltDivider
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Junction or Market (e.g. Bata Junction)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HighContrastWhite,
                        unfocusedTextColor = HighContrastWhite,
                        focusedBorderColor = SafetyAmber,
                        unfocusedBorderColor = AsphaltDivider
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Select Category Icon", color = MutedSilver, fontSize = 12.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("HOME" to Icons.Default.Home, "WORK" to Icons.Default.Work, "MARKET" to Icons.Default.Storefront, "CUSTOM" to Icons.Default.Star).forEach { (type, icon) ->
                        val isSel = selectedIcon == type
                        IconButton(
                            onClick = { selectedIcon = type },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) SafetyAmber else AsphaltCard)
                        ) {
                            Icon(icon, contentDescription = type, tint = if (isSel) AsphaltDark else HighContrastWhite)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && address.isNotBlank()) {
                        onSavePlace(title, address, selectedLga, selectedIcon)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyAmber, contentColor = AsphaltDark)
            ) {
                Text("Save Place", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedSilver)
            }
        }
    )
}
