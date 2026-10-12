package com.example.memoryboardduel.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.memoryboardduel.data.ThemeMode
import com.example.memoryboardduel.game.model.Difficulty
import com.example.memoryboardduel.ui.MemoryBoardViewModel
import com.example.memoryboardduel.ui.components.SectionLabel
import com.example.memoryboardduel.ui.components.StatValue
import kotlinx.coroutines.launch
import java.util.Locale

private enum class PendingReset { STATISTICS, SETTINGS }

@Composable
fun SettingsScreen(viewModel: MemoryBoardViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingReset by rememberSaveable { mutableStateOf<PendingReset?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })

                SettingsGroup("APPEARANCE") {
                    ChipSetting(
                        title = "Theme",
                        options = ThemeMode.entries,
                        selected = settings.themeMode,
                        label = { it.label },
                        onSelect = { mode -> viewModel.updateSettings { it.copy(themeMode = mode) } }
                    )
                    SwitchSetting(
                        title = "Dynamic Color",
                        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Use colors from your wallpaper"
                        else "Requires Android 12 or newer",
                        checked = settings.dynamicColor,
                        onCheckedChange = { value -> viewModel.updateSettings { it.copy(dynamicColor = value) } }
                    )
                }

                SettingsGroup("GAME") {
                    ChipSetting(
                        title = "CPU Difficulty",
                        options = Difficulty.entries,
                        selected = settings.difficulty,
                        label = { it.label },
                        onSelect = { difficulty -> viewModel.updateSettings { it.copy(difficulty = difficulty) } }
                    )
                    Text(
                        "Applies from the next duel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
                    )
                    SwitchSetting(
                        title = "Confirm Restart",
                        subtitle = "Ask before replacing the current board",
                        checked = settings.confirmRestart,
                        onCheckedChange = { value -> viewModel.updateSettings { it.copy(confirmRestart = value) } }
                    )
                    SwitchSetting(
                        title = "Show Coordinates",
                        subtitle = "Label columns A–F and rows 1–6",
                        checked = settings.showCoordinates,
                        onCheckedChange = { value -> viewModel.updateSettings { it.copy(showCoordinates = value) } }
                    )
                }

                SettingsGroup("FEEDBACK") {
                    SwitchSetting(
                        title = "Sound",
                        checked = settings.sound,
                        onCheckedChange = { value -> viewModel.updateSettings { it.copy(sound = value) } }
                    )
                    SwitchSetting(
                        title = "Haptic Feedback",
                        checked = settings.haptics,
                        onCheckedChange = { value -> viewModel.updateSettings { it.copy(haptics = value) } }
                    )
                    SwitchSetting(
                        title = "Animations",
                        subtitle = "Card flips, pulses and celebrations",
                        checked = settings.animations,
                        onCheckedChange = { value -> viewModel.updateSettings { it.copy(animations = value) } }
                    )
                }

                SettingsGroup("STATISTICS") {
                    Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            StatValue("PLAYED", "${stats.gamesPlayed}", Modifier.weight(1f))
                            StatValue("WINS", "${stats.wins}", Modifier.weight(1f))
                            StatValue("LOSSES", "${stats.losses}", Modifier.weight(1f))
                            StatValue("DRAWS", "${stats.draws}", Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth()) {
                            StatValue("BEST PAIRS", "${stats.bestPairs}", Modifier.weight(1f))
                            StatValue("BEST STREAK", "${stats.bestStreak}", Modifier.weight(1f))
                            StatValue("TOTAL PAIRS", "${stats.totalPairs}", Modifier.weight(1f))
                            StatValue("AVG PAIRS", String.format(Locale.US, "%.1f", stats.averagePairs), Modifier.weight(1f))
                        }
                    }
                }

                SettingsGroup("DATA") {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { pendingReset = PendingReset.STATISTICS }, modifier = Modifier.fillMaxWidth()) {
                            Text("Reset Statistics")
                        }
                        OutlinedButton(onClick = { pendingReset = PendingReset.SETTINGS }, modifier = Modifier.fillMaxWidth()) {
                            Text("Reset Settings")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }

    pendingReset?.let { reset ->
        val isStats = reset == PendingReset.STATISTICS
        AlertDialog(
            onDismissRequest = { pendingReset = null },
            title = { Text(if (isStats) "Reset statistics?" else "Reset settings?") },
            text = {
                Text(
                    if (isStats) "Games, wins, best pairs and streaks will be cleared."
                    else "All settings will return to their defaults."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingReset = null
                    if (isStats) viewModel.resetStatistics() else viewModel.resetSettings()
                    scope.launch {
                        snackbarHostState.showSnackbar(if (isStats) "Statistics reset" else "Settings reset")
                    }
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { pendingReset = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column {
        SectionLabel(title, Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
private fun SwitchSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun <T> ChipSetting(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(label(option)) }
                )
            }
        }
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
