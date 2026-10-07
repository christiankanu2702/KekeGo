package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BidOffer
import com.example.ui.theme.*

@Composable
fun FareBiddingSection(
    proposedFareNaira: Int,
    onProposedFareChanged: (Int) -> Unit,
    activeBids: List<BidOffer>,
    onAcceptBid: (BidOffer) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SafetyAmber.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "P2P Bidding",
                        tint = SafetyAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "P2P Fare Negotiation",
                        style = MaterialTheme.typography.titleMedium,
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = SafetyAmberDark,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Live Market Bidding",
                        color = HighContrastWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Passenger Proposed Fare Stepper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AsphaltCard, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Your Proposed Fare:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSilver
                    )
                    Text(
                        text = "₦$proposedFareNaira",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SafetyAmber,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = {
                            if (proposedFareNaira > 200) onProposedFareChanged(proposedFareNaira - 50)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = AsphaltDark)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Fare", tint = HighContrastWhite)
                    }

                    FilledIconButton(
                        onClick = {
                            onProposedFareChanged(proposedFareNaira + 50)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SafetyAmber)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Fare", tint = AsphaltDark)
                    }
                }
            }

            if (activeBids.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Incoming Driver Counter-Offers (${activeBids.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = HighContrastWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                activeBids.forEach { bid ->
                    BidOfferItem(
                        bid = bid,
                        onAccept = { onAcceptBid(bid) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nearby drivers in corridor will review your proposed fare or counter-offer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSilver
                )
            }
        }
    }
}

@Composable
fun BidOfferItem(
    bid: BidOffer,
    onAccept: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AsphaltCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bid.driverName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = SafetyAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${bid.driverRating}",
                        fontSize = 12.sp,
                        color = SafetyAmber,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${bid.tricyclePlate} • ETA ~${bid.etaMinutes} mins",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSilver
                )

                Text(
                    text = "Offered: ₦${bid.counterFareNaira}",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmeraldGreenLight,
                    fontWeight = FontWeight.Black
                )
            }

            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = AsphaltDark
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Accept",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
