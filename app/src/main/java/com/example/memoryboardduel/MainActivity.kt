package com.example.memoryboardduel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memoryboardduel.data.SettingsRepository
import com.example.memoryboardduel.data.SharedPreferencesStore
import com.example.memoryboardduel.data.StatisticsRepository
import com.example.memoryboardduel.navigation.AppNavigation
import com.example.memoryboardduel.ui.MemoryBoardViewModel
import com.example.memoryboardduel.ui.theme.MemoryBoardDuelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = MemoryBoardViewModel.factory(
            settings = SettingsRepository(SharedPreferencesStore(this, "memory_board_settings")),
            statistics = StatisticsRepository(SharedPreferencesStore(this, "memory_board_statistics"))
        )
        setContent {
            val viewModel: MemoryBoardViewModel = viewModel(factory = factory)
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            MemoryBoardDuelTheme(themeMode = settings.themeMode, dynamicColor = settings.dynamicColor) {
                AppNavigation(viewModel)
            }
        }
    }
}
