package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CargoCalculator
import com.example.ui.theme.*

@Composable
fun VolumetricCargoCard(
    sacks: Int,
    basins: Int,
    onSacksChanged: (Int) -> Unit,
    onBasinsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableSeats = CargoCalculator.calculateAvailableSeats(sacks, basins)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Cargo Logistics",
                        tint = SafetyAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Volumetric Market Cargo",
                        style = MaterialTheme.typography.titleMedium,
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Rear Seat Counter Pill
                Surface(
                    color = if (availableSeats > 0) EmeraldGreenDark else ErrorRed,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chair,
                            contentDescription = null,
                            tint = HighContrastWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$availableSeats / 3 Seats Left",
                            color = HighContrastWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Market Sacks Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Market Sacks (Ariaria/Ubani Bags):",
                        color = MutedSilver,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$sacks Sacks (+₦${sacks * 150})",
                        color = SafetyAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Slider(
                    value = sacks.toFloat(),
                    onValueChange = { onSacksChanged(it.toInt()) },
                    valueRange = 0f..3f,
                    steps = 2,
                    colors = SliderDefaults.colors(
                        thumbColor = SafetyAmber,
                        activeTrackColor = SafetyAmber,
                        inactiveTrackColor = AsphaltCard
                    )
                )
            }

            // Market Basins Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Produce Basins / Crates:",
                        color = MutedSilver,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$basins Basins (+₦${basins * 100})",
                        color = SafetyAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Slider(
                    value = basins.toFloat(),
                    onValueChange = { onBasinsChanged(it.toInt()) },
                    valueRange = 0f..4f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = SafetyAmber,
                        activeTrackColor = SafetyAmber,
                        inactiveTrackColor = AsphaltCard
                    )
                )
            }
        }
    }
}

@Composable
fun KekeGoVaultCard(
    vaultBalanceNaira: Int,
    onSweepClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AsphaltCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = "Vault Savings",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "KekeGo Vault",
                        style = MaterialTheme.typography.titleMedium,
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fractional Cash Change Accumulated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSilver
                    )
                    Text(
                        text = "₦$vaultBalanceNaira",
                        style = MaterialTheme.typography.titleLarge,
                        color = EmeraldGreenLight,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Button(
                onClick = onSweepClicked,
                enabled = vaultBalanceNaira > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = AsphaltDark
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Sweep",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
