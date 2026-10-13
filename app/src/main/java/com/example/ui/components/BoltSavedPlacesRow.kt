package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class SavedPlaceShortcut(
    val label: String,
    val marketName: String,
    val lgaId: String,
    val icon: ImageVector
)

@Composable
fun BoltSavedPlacesRow(
    onSelectPlace: (marketName: String, lgaId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shortcuts = listOf(
        SavedPlaceShortcut("Home", "Bata Junction Aba", "aba_south", Icons.Default.Home),
        SavedPlaceShortcut("Work", "Factory Road Industrial Layout", "aba_north", Icons.Default.Work),
        SavedPlaceShortcut("Ariaria Market", "Ariaria International Market", "aba_north", Icons.Default.ShoppingBag),
        SavedPlaceShortcut("Ekeoha Plaza", "Ekeoha Shopping Centre", "aba_south", Icons.Default.Storefront),
        SavedPlaceShortcut("Umuahia Terminal", "Isi Gate Central Market", "umuahia_north", Icons.Default.DirectionsBus)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "SAVED PLACES SHORTCUTS",
            style = MaterialTheme.typography.labelSmall,
            color = SafetyAmber,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shortcuts) { place ->
                Surface(
                    color = AsphaltCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                    modifier = Modifier.clickable {
                        onSelectPlace(place.marketName, place.lgaId)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = place.icon,
                            contentDescription = null,
                            tint = Color(0xFF00D664),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = place.label,
                                color = HighContrastWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = place.marketName.take(16),
                                color = MutedSilver,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
