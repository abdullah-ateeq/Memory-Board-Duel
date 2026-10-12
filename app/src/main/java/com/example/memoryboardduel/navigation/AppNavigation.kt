package com.example.memoryboardduel.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.memoryboardduel.ui.MemoryBoardViewModel
import com.example.memoryboardduel.ui.screens.AboutScreen
import com.example.memoryboardduel.ui.screens.GameScreen
import com.example.memoryboardduel.ui.screens.HomeScreen
import com.example.memoryboardduel.ui.screens.ResultScreen
import com.example.memoryboardduel.ui.screens.SettingsScreen
import com.example.memoryboardduel.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

private data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

private val TopLevelDestinations = listOf(
    TopLevelDestination(Routes.HOME, "Home", Icons.Filled.Home),
    TopLevelDestination(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
    TopLevelDestination(Routes.ABOUT, "About", Icons.Filled.Info)
)

/** Ignores repeated taps that arrive while a navigation transition is already underway. */
private fun NavBackStackEntry.isResumed() = lifecycle.currentState == Lifecycle.State.RESUMED

private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigation(viewModel: MemoryBoardViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestinations.any { it.route == currentRoute }
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                if (currentRoute != destination.route) navController.navigateToTab(destination.route)
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(animationsEnabled = settings.animations) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            }
            composable(Routes.HOME) { entry ->
                HomeScreen(
                    viewModel = viewModel,
                    onPlay = {
                        if (entry.isResumed()) {
                            viewModel.startNewGame()
                            navController.navigate(Routes.GAME) { launchSingleTop = true }
                        }
                    },
                    onOpenSettings = { if (entry.isResumed()) navController.navigateToTab(Routes.SETTINGS) },
                    onOpenAbout = { if (entry.isResumed()) navController.navigateToTab(Routes.ABOUT) }
                )
            }
            composable(Routes.GAME) { entry ->
                GameScreen(
                    viewModel = viewModel,
                    onExit = { if (entry.isResumed()) navController.popBackStack(Routes.HOME, inclusive = false) },
                    onGameOver = {
                        navController.navigate(Routes.RESULT) {
                            popUpTo(Routes.HOME)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.RESULT) { entry ->
                ResultScreen(
                    viewModel = viewModel,
                    onPlayAgain = {
                        if (entry.isResumed()) {
                            viewModel.startNewGame()
                            navController.navigate(Routes.GAME) { popUpTo(Routes.HOME) }
                        }
                    },
                    onHome = {
                        if (navController.currentDestination?.route == Routes.RESULT) {
                            if (!navController.popBackStack(Routes.HOME, inclusive = false)) {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }
            composable(Routes.SETTINGS) { SettingsScreen(viewModel) }
            composable(Routes.ABOUT) { AboutScreen() }
        }
    }
}
