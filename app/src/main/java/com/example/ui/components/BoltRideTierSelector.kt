package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class BoltRideTier(
    val title: String,
    val subtitle: String,
    val multiplier: Double,
    val capacitySeats: Int,
    val etaMins: Int
) {
    STANDARD("Keke Standard", "Shared passenger transit", 1.0, 3, 2),
    CHARTER("Express Charter", "Private non-stop direct ride", 1.8, 3, 1),
    CARGO("Cargo & Waybill", "Sacks, basins & merchant freight", 1.5, 1, 4)
}

@Composable
fun BoltRideTierSelector(
    baseFareNaira: Int,
    selectedTier: BoltRideTier,
    onTierSelected: (BoltRideTier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "SELECT RIDE CATEGORY",
            style = MaterialTheme.typography.labelSmall,
            color = SafetyAmber,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BoltRideTier.values().forEach { tier ->
                val isSelected = tier == selectedTier
                val calculatedFare = (baseFareNaira * tier.multiplier).toInt()

                Surface(
                    color = if (isSelected) Color(0xFF1E2838) else AsphaltCard,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) Color(0xFF00D664) else AsphaltDivider
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTierSelected(tier) }
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (tier) {
                                    BoltRideTier.STANDARD -> Icons.Default.DirectionsCar
                                    BoltRideTier.CHARTER -> Icons.Default.ElectricRickshaw
                                    BoltRideTier.CARGO -> Icons.Default.LocalShipping
                                },
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF00D664) else SafetyAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MutedSilver, modifier = Modifier.size(10.dp))
                                Text("${tier.capacitySeats}", color = MutedSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tier.title,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${tier.etaMins} mins",
                            color = Color(0xFF00D664),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₦$calculatedFare",
                            color = if (isSelected) Color(0xFF00D664) else SafetyAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
