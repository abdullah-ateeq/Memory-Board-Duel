package com.example.memoryboardduel.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.memoryboardduel.game.model.Difficulty
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.ui.MemoryBoardViewModel
import com.example.memoryboardduel.ui.components.SectionLabel
import com.example.memoryboardduel.ui.components.StatsRowCard
import com.example.memoryboardduel.ui.components.drawOwnerMark
import com.example.memoryboardduel.ui.components.drawSymbol
import com.example.memoryboardduel.ui.components.drawTileBack
import com.example.memoryboardduel.ui.theme.GameTheme

@Composable
fun HomeScreen(
    viewModel: MemoryBoardViewModel,
    onPlay: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showHowToPlay by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            BrandMark()
            Spacer(Modifier.height(24.dp))
            Text(
                "MEMORY BOARD",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                "DUEL",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Remember the board. Capture the pairs.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("PLAY", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(24.dp))
            StatsRowCard(
                listOf(
                    "GAMES" to stats.gamesPlayed.toString(),
                    "WINS" to stats.wins.toString(),
                    "BEST PAIRS" to stats.bestPairs.toString(),
                    "BEST STREAK" to stats.bestStreak.toString()
                )
            )
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SectionLabel("DIFFICULTY")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Difficulty.entries.forEach { difficulty ->
                            FilterChip(
                                selected = settings.difficulty == difficulty,
                                onClick = { viewModel.updateSettings { it.copy(difficulty = difficulty) } },
                                label = { Text(difficulty.label.uppercase()) }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            OutlinedButton(onClick = { showHowToPlay = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("How to Play")
            }
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onOpenSettings) { Text("Settings") }
                TextButton(onClick = onOpenAbout) { Text("About") }
            }
        }
    }

    if (showHowToPlay) {
        HowToPlaySheet(onDismiss = { showHowToPlay = false })
    }
}

/** A tiny 3-tile motif: hidden, revealed and claimed. */
@Composable
private fun BrandMark() {
    val colors = GameTheme.colors
    val shape = RoundedCornerShape(9.dp)
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clearAndSetSemantics { }
    ) {
        Canvas(Modifier.size(42.dp).clip(shape)) {
            drawTileBack(colors.tileBack, colors.tileBackPattern, colors.tileBackMark)
        }
        Canvas(Modifier.size(42.dp).clip(shape)) {
            drawRect(colors.tileFace)
            drawSymbol(SymbolType.STAR, colors.symbol(SymbolType.STAR))
        }
        Canvas(Modifier.size(42.dp).clip(shape)) {
            drawRect(colors.playerContainer)
            drawOwnerMark(Side.PLAYER, colors.player)
        }
    }
}
