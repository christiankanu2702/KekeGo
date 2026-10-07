package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {

    private lateinit var repository: KekeGoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = KekeGoRepository(this, lifecycleScope)

        setContent {
            KekeGoTheme {
                MainAppHost(repository = repository)
            }
        }
    }
}

enum class NavigationDestination(val title: String, val icon: ImageVector) {
    PASSENGER("Passenger", Icons.Default.ElectricRickshaw),
    DRIVER("Rider", Icons.Default.DirectionsCar),
    PARK_CAPTAIN("Captain", Icons.Default.SupervisorAccount),
    ADMIN("Admin", Icons.Default.Analytics),
    WALLET("Wallet", Icons.Default.AccountBalanceWallet)
}

@Composable
fun MainAppHost(repository: KekeGoRepository) {
    var firebaseUser by remember { mutableStateOf<FirebaseUser?>(null) }
    var isGuestSessionActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        firebaseUser = FirebaseManager.getCurrentUser()
    }

    DisposableEffect(Unit) {
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            firebaseUser = auth.currentUser
            if (auth.currentUser != null) {
                repository.initUserSession()
            }
        }
        FirebaseManager.getAuth().addAuthStateListener(authListener)
        onDispose {
            FirebaseManager.getAuth().removeAuthStateListener(authListener)
        }
    }

    if (firebaseUser == null && !isGuestSessionActive) {
        // Composable Auth Gate: Strictly visible Google Sign-In with instantaneous demo entry
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

    // Synchronize initial destination with user role
    LaunchedEffect(userAccount?.role) {
        userAccount?.role?.let { role ->
            when (role) {
                UserRole.PASSENGER -> currentDestination = NavigationDestination.PASSENGER
                UserRole.DRIVER -> currentDestination = NavigationDestination.DRIVER
                UserRole.PARK_CAPTAIN -> currentDestination = NavigationDestination.PARK_CAPTAIN
                UserRole.ADMIN -> currentDestination = NavigationDestination.ADMIN
            }
        }
    }

    // Hardware back press handler
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
            // Mode Indicator Bar
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
                                text = "CLOUD & ROOM CACHED",
                                color = HighContrastWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Abia State 17 LGAs Active",
                            color = MutedSilver,
                            fontSize = 11.sp
                        )
                    }

                    Box {
                        TextButton(
                            onClick = { showRoleSwitchMenu = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = SafetyAmber)
                        ) {
                            Text(
                                text = "Role: ${userAccount?.role?.name ?: "PASSENGER"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Role",
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
                            Divider(color = AsphaltDivider)
                            DropdownMenuItem(
                                text = { Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    FirebaseManager.getAuth().signOut()
                                    showRoleSwitchMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Screen Switcher
            when (currentDestination) {
                NavigationDestination.PASSENGER -> {
                    PassengerScreen(
                        repository = repository,
                        onNavigateToWallet = { currentDestination = NavigationDestination.WALLET }
                    )
                }
                NavigationDestination.DRIVER -> {
                    DriverScreen(
                        repository = repository,
                        onNavigateToWallet = { currentDestination = NavigationDestination.WALLET }
                    )
                }
                NavigationDestination.PARK_CAPTAIN -> {
                    ParkCaptainScreen(
                        repository = repository
                    )
                }
                NavigationDestination.ADMIN -> {
                    AdminScreen(
                        repository = repository
                    )
                }
                NavigationDestination.WALLET -> {
                    WalletPaystackScreen(
                        repository = repository,
                        onBackClicked = { currentDestination = NavigationDestination.PASSENGER }
                    )
                }
            }
        }
    }
}
