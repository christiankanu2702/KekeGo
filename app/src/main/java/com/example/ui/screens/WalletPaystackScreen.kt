package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.models.LedgerEntry
import com.example.data.models.TransactionType
import com.example.data.repository.KekeGoRepository
import com.example.ui.components.KekeGoVaultCard
import com.example.ui.components.PaystackPaymentSheet
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletPaystackScreen(
    repository: KekeGoRepository,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val userAccount by repository.currentUserAccount.collectAsState()
    val ledgerEntries by repository.getLedgerFlow().collectAsState(initial = emptyList())

    var showPaystackSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AsphaltBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Paystack Digital Wallet",
                        style = MaterialTheme.typography.titleLarge,
                        color = HighContrastWhite,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HighContrastWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AsphaltDark)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Digital Balance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AsphaltSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AVAILABLE CASH BALANCE",
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedSilver,
                                letterSpacing = 1.sp
                            )
                            Surface(color = EmeraldGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "Paystack Secured",
                                    color = EmeraldGreenLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "₦${userAccount?.walletBalanceNaira ?: 3500}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = HighContrastWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 36.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showPaystackSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = AsphaltDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Top Up via Paystack (Debit Card / USSD)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // KekeGo Vault Card
            item {
                KekeGoVaultCard(
                    vaultBalanceNaira = userAccount?.vaultSavingsNaira ?: 450,
                    onSweepClicked = {
                        coroutineScope.launch {
                            repository.sweepVaultToWallet()
                        }
                    }
                )
            }

            // Ledger Transaction History
            item {
                Text(
                    text = "Transaction History & Cloud Ledgers:",
                    style = MaterialTheme.typography.titleMedium,
                    color = HighContrastWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            if (ledgerEntries.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AsphaltCard,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MutedSilver, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No transactions recorded yet", color = HighContrastWhite, fontWeight = FontWeight.Bold)
                            Text("Top-ups, trips, and vault sweeps sync instantly to Firestore", color = MutedSilver, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(ledgerEntries) { entry ->
                    Surface(
                        color = AsphaltCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (entry.type == TransactionType.WALLET_TOPUP || entry.type == TransactionType.VAULT_SWEEP)
                                                EmeraldGreen.copy(alpha = 0.2f)
                                            else
                                                SafetyAmberDark.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (entry.type) {
                                            TransactionType.WALLET_TOPUP -> Icons.Default.Add
                                            TransactionType.VAULT_SWEEP -> Icons.Default.Savings
                                            TransactionType.TRIP_PAYMENT -> Icons.Default.ElectricRickshaw
                                            else -> Icons.Default.Payments
                                        },
                                        contentDescription = null,
                                        tint = if (entry.type == TransactionType.WALLET_TOPUP || entry.type == TransactionType.VAULT_SWEEP) EmeraldGreen else SafetyAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = entry.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = HighContrastWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ref: ${entry.referenceToken.take(16)}...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSilver,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Text(
                                text = "${if (entry.type == TransactionType.WALLET_TOPUP || entry.type == TransactionType.VAULT_SWEEP) "+" else "-"}₦${entry.amountNaira}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (entry.type == TransactionType.WALLET_TOPUP || entry.type == TransactionType.VAULT_SWEEP) EmeraldGreenLight else HighContrastWhite,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showPaystackSheet) {
        PaystackPaymentSheet(
            onDismiss = { showPaystackSheet = false },
            onPaymentSuccess = { amount, last4 ->
                coroutineScope.launch {
                    repository.fundWalletPaystack(amount, last4)
                }
            }
        )
    }
}
