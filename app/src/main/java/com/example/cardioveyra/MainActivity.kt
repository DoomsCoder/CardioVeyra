package com.example.cardioveyra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.cardioveyra.ui.components.CardioBottomBar
import com.example.cardioveyra.ui.components.CardioTopBar
import com.example.cardioveyra.ui.navigation.Destination
import com.example.cardioveyra.ui.screens.cardio.CardioScreen
import com.example.cardioveyra.ui.screens.scan.ScanScreen
import com.example.cardioveyra.ui.onboarding.OnboardingScreen
import com.example.cardioveyra.ui.screens.settings.SettingsScreen
import com.example.cardioveyra.ui.screens.statistics.StatisticsScreen
import com.example.cardioveyra.ui.screens.details.ScanDetailsScreen
import com.example.cardioveyra.ui.theme.CardioVeyraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CardioVeyraTheme {

                val navKeyBackStack = rememberNavBackStack(Destination.Cardio)

                @Suppress("UNCHECKED_CAST")
                val backStack = navKeyBackStack as NavBackStack<Destination>

                val currentDestination = backStack.last()
                val showBars = currentDestination.showSystemBars

                // We use Surface to ensure the app background updates correctly when the theme toggles
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        topBar = {
                            if (showBars && currentDestination != Destination.Stats && currentDestination != Destination.Settings ) {
                                CardioTopBar()
                            }
                        },
                        bottomBar = {
                            if(showBars) {
                                CardioBottomBar(
                                    currentDestination = currentDestination,
                                    onNavigate = { newDestination ->

                                        if (currentDestination != newDestination) {
                                            backStack.clear()
                                            backStack.add(newDestination)
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->

                        NavDisplay(
                            backStack = backStack,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = if (showBars) innerPadding.calculateTopPadding() else 0.dp)
                                .padding(bottom = if (showBars) innerPadding.calculateBottomPadding() else 0.dp),
                            entryProvider = { key ->
                                NavEntry(key) {
                                    when (key) {
                                        Destination.Cardio -> CardioScreen(
                                            onNavigateToScan = {
                                                backStack.add(Destination.Onboarding)
                                            }
                                        )
                                        Destination.Stats -> StatisticsScreen()
                                        Destination.Settings -> SettingsScreen()
                                        Destination.Onboarding -> OnboardingScreen(
                                            onFinish = {
                                                // Pop Onboarding and go to LiveScan
                                                backStack.remove(Destination.Onboarding)
                                                backStack.add(Destination.LiveScan)
                                            }
                                        )
                                        Destination.LiveScan -> ScanScreen(
                                            onCloseClicked = {
                                                backStack.remove(Destination.LiveScan)
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}