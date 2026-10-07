package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PaystackPaymentSheet(
    onDismiss: () -> Unit,
    onPaymentSuccess: (amount: Int, cardLast4: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAmount by remember { mutableIntStateOf(2500) }
    var cardNumber by remember { mutableStateOf("5399 8321 4409 1842") }
    var expiryDate by remember { mutableStateOf("08/28") }
    var cvv by remember { mutableStateOf("381") }
    var isProcessing by remember { mutableStateOf(false) }
    var completedRef by remember { mutableStateOf<String?>(null) }

    val presetAmounts = listOf(1000, 2500, 5000, 10000)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AsphaltDark,
        titleContentColor = HighContrastWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Paystack Secure Gateway",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Paystack Checkout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (completedRef == null) {
                    Text(
                        text = "Secured Nigerian Card Tokenization (Verve / Mastercard / Visa)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSilver
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Select Top-Up Amount:",
                        style = MaterialTheme.typography.labelLarge,
                        color = HighContrastWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetAmounts.forEach { amount ->
                            FilterChip(
                                selected = selectedAmount == amount,
                                onClick = { selectedAmount = amount },
                                label = { Text("₦$amount", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldGreen,
                                    selectedLabelColor = AsphaltDark,
                                    containerColor = AsphaltCard,
                                    labelColor = HighContrastWhite
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Card Number", color = MutedSilver) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = EmeraldGreen)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighContrastWhite,
                            unfocusedTextColor = HighContrastWhite,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = AsphaltDivider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("MM/YY", color = MutedSilver) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighContrastWhite,
                                unfocusedTextColor = HighContrastWhite,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = AsphaltDivider
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it },
                            label = { Text("CVV", color = MutedSilver) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = HighContrastWhite,
                                unfocusedTextColor = HighContrastWhite,
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = AsphaltDivider
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MutedSilver, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "256-bit encrypted via Paystack PCI-DSS Level 1 Gateway",
                            color = MutedSilver,
                            fontSize = 10.sp
                        )
                    }
                } else {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Payment Tokenized & Verified!",
                            style = MaterialTheme.typography.titleMedium,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reference: $completedRef",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldGreenLight,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₦$selectedAmount credited to KekeGo Digital Wallet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedSilver
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (completedRef == null) {
                Button(
                    onClick = {
                        isProcessing = true
                        // Simulate network call
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            val last4 = if (cardNumber.length >= 4) cardNumber.takeLast(4) else "1842"
                            onPaymentSuccess(selectedAmount, last4)
                            completedRef = "PSTK_TRX_${System.currentTimeMillis().toString().takeLast(8)}"
                            isProcessing = false
                        }, 800)
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AsphaltDark, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Authorizing...")
                    } else {
                        Text("Pay ₦$selectedAmount", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark)
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (completedRef == null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MutedSilver)
                }
            }
        }
    )
}
