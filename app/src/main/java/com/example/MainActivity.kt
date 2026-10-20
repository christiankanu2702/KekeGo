package com.example
import androidx.compose.foundation.shape.RoundedCornerShape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.firebase.FirebaseManager
import com.example.data.models.UserRole
import com.example.data.repository.KekeGoRepository
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.components.AppUpdatePromptDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    private lateinit var repository: KekeGoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = KekeGoRepository(this, lifecycleScope)
        repository.checkForAppUpdates()

        setContent {
            val availableUpdate by repository.availableAppUpdate.collectAsState()
            if (availableUpdate != null) {
                AppUpdatePromptDialog(
                    updateInfo = availableUpdate!!,
                    onDismiss = { repository.dismissUpdatePrompt() }
                )
            }
            KekeGoTheme {
                MainAppHost(repository = repository)
            }
        }
    }
}

enum class NavigationDestination(val title: String, val icon: ImageVector) {
    PASSENGER("Book", Icons.Default.ElectricRickshaw),
    RIDES("Rides", Icons.Default.ReceiptLong),
    DRIVER("Rider", Icons.Default.DirectionsCar),
    WALLET("Wallet", Icons.Default.AccountBalanceWallet),
    ADMIN("Admin", Icons.Default.Analytics)
}

@Composable
fun MainAppHost(repository: KekeGoRepository) {
    var firebaseUser by remember { mutableStateOf<FirebaseUser?>(null) }
    var isGuestSessionActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firebaseUser = FirebaseManager.getCurrentUser()
    }

    DisposableEffect(Unit) {
        val authListener = FirebaseAuth.AuthStateListener { a ->
            firebaseUser = a.currentUser
            if (a.currentUser != null) {
                repository.initUserSession()
            }
        }
        try {
            val auth = FirebaseManager.getAuth()
            auth.addAuthStateListener(authListener)
        } catch (_: Exception) {}
        onDispose {
            try {
                FirebaseManager.getAuth().removeAuthStateListener(authListener)
            } catch (_: Exception) {}
        }
    }

    if (firebaseUser == null && !isGuestSessionActive) {
        AuthScreen(
            onSignInSuccess = { role ->
                repository.initUserSession(role)
            },
            onDemoSignIn = { role ->
                repository.initDemoSession(role)
                isGuestSessionActive = true
            }
        )
    } else {
        AppContent(repository = repository)
    }
}

@Composable
fun AppContent(repository: KekeGoRepository) {
    val userAccount by repository.currentUserAccount.collectAsState()
    var currentDestination by remember { mutableStateOf(NavigationDestination.PASSENGER) }
    var showRoleSwitchMenu by remember { mutableStateOf(false) }

    LaunchedEffect(userAccount?.role) {
        userAccount?.role?.let { role ->
            when (role) {
                UserRole.PASSENGER -> currentDestination = NavigationDestination.PASSENGER
                UserRole.DRIVER -> currentDestination = NavigationDestination.DRIVER
                UserRole.PARK_CAPTAIN -> currentDestination = NavigationDestination.ADMIN
                UserRole.ADMIN -> currentDestination = NavigationDestination.ADMIN
            }
        }
    }

    if (currentDestination != NavigationDestination.PASSENGER) {
        BackHandler {
            currentDestination = NavigationDestination.PASSENGER
        }
    }

    Scaffold(
        containerColor = AsphaltBlack,
        bottomBar = {
            NavigationBar(
                containerColor = AsphaltDark,
                contentColor = HighContrastWhite
            ) {
                NavigationDestination.entries.forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.title,
                                tint = if (isSelected) SafetyAmber else MutedSilver
                            )
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SafetyAmber else MutedSilver
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = AsphaltCard
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                color = AsphaltCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = EmeraldGreenDark,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "KEKEGO TRANSIT • ROOM OFFLINE CACHE",
                                color = HighContrastWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        val ctx = LocalContext.current
                        val appVersionText = remember {
                            try {
                                val pInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
                                "v${pInfo.versionName ?: "1.0.0"}"
                            } catch (_: Exception) {
                                "v1.0.0"
                            }
                        }
                        Text(
                            text = "Abia State • $appVersionText",
                            color = MutedSilver,
                            fontSize = 10.sp
                        )
                    }

                    val isSuperAdmin = userAccount?.email?.equals("christiankanu2702@gmail.com", ignoreCase = true) == true || userAccount?.role == UserRole.ADMIN
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSuperAdmin) {
                            Surface(
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text(
                                    text = "SUPER ADMIN MASTER",
                                    color = Color(0xFFFFD700),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        if (isSuperAdmin) {
                            Box {
                                TextButton(
                                    onClick = { showRoleSwitchMenu = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "View As: ${userAccount?.role?.name ?: "PASSENGER"}",
                                        color = SafetyAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = SafetyAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showRoleSwitchMenu,
                                    onDismissRequest = { showRoleSwitchMenu = false },
                                    modifier = Modifier.background(AsphaltDark)
                                ) {
                                    UserRole.entries.forEach { role ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    role.name.replace("_", " "),
                                                    color = HighContrastWhite,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            onClick = {
                                                repository.switchUserRole(role)
                                                showRoleSwitchMenu = false
                                            }
                                        )
                                    }
                                    HorizontalDivider(color = AsphaltDivider)
                                    DropdownMenuItem(
                                        text = { Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            FirebaseManager.getAuth().signOut()
                                            showRoleSwitchMenu = false
                                        }
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = AsphaltCard,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider)
                                ) {
                                    Text(
                                        text = userAccount?.role?.name ?: "PASSENGER",
                                        color = EmeraldGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                TextButton(
                                    onClick = { FirebaseManager.getAuth().signOut() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Sign Out", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            when (currentDestination) {
                NavigationDestination.PASSENGER -> {
                    PassengerScreen(
                        repository = repository,
                        onNavigateToWallet = { currentDestination = NavigationDestination.WALLET }
                    )
                }
                NavigationDestination.RIDES -> {
                    RideHistoryScreen(
                        repository = repository,
                        onRebookTrip = { origin, dest, fare ->
                            currentDestination = NavigationDestination.PASSENGER
                        }
                    )
                }
                NavigationDestination.DRIVER -> {
                    DriverScreen(
                        repository = repository,
                        onNavigateToWallet = { currentDestination = NavigationDestination.WALLET }
                    )
                }
                NavigationDestination.WALLET -> {
                    WalletPaystackScreen(
                        repository = repository,
                        onBackClicked = { currentDestination = NavigationDestination.PASSENGER }
                    )
                }
                NavigationDestination.ADMIN -> {
                    AdminScreen(
                        repository = repository
                    )
                }
            }
        }
    }
}
