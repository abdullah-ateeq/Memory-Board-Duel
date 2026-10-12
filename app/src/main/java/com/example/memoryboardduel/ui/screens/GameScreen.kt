package com.example.memoryboardduel.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.memoryboardduel.game.model.GamePhase
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.ui.GameEvent
import com.example.memoryboardduel.ui.GameUiState
import com.example.memoryboardduel.ui.MemoryBoardViewModel
import com.example.memoryboardduel.ui.components.GameBoard
import com.example.memoryboardduel.ui.components.GameFeedbackEffect
import com.example.memoryboardduel.ui.components.SectionLabel
import com.example.memoryboardduel.ui.components.boardExtent
import com.example.memoryboardduel.ui.components.tileSizeFor
import com.example.memoryboardduel.ui.theme.GameTheme
import com.example.memoryboardduel.util.GameTimings
import com.example.memoryboardduel.util.pairsRemainingDescription
import com.example.memoryboardduel.util.turnDescription
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameScreen(
    viewModel: MemoryBoardViewModel,
    onExit: () -> Unit,
    onGameOver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val timings = GameTimings.forAnimations(settings.animations)
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestartDialog by rememberSaveable { mutableStateOf(false) }
    val gameOverHandler by rememberUpdatedState(onGameOver)

    LaunchedEffect(viewModel) {
        viewModel.messages.collectLatest { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.gameOver.collect { gameOverHandler() }
    }
    GameFeedbackEffect(viewModel.feedback, soundEnabled = settings.sound, hapticsEnabled = settings.haptics)

    val exit = {
        viewModel.abandonGame()
        onExit()
    }
    BackHandler(onBack = exit)

    val requestRestart = {
        if (settings.confirmRestart) showRestartDialog = true else viewModel.startNewGame()
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            GameTopBar(onBack = exit, onRestart = requestRestart)
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                val wide = maxWidth > maxHeight || maxWidth >= 720.dp
                val board: @Composable (Dp) -> Unit = { tileSize ->
                    GameBoard(
                        state = state,
                        tileSize = tileSize,
                        showCoordinates = settings.showCoordinates,
                        flipDurationMs = timings.flipDurationMs,
                        animationsEnabled = settings.animations,
                        onTileClick = viewModel::selectTile
                    )
                }

                if (wide) {
                    val panelWidth = 280.dp
                    val boardSpace = minOf(maxHeight - 16.dp, maxWidth - panelWidth - 24.dp)
                    val tileSize = tileSizeFor(boardSpace, settings.showCoordinates)
                    Row(
                        Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
                    ) {
                        Box(Modifier.verticalScroll(rememberScrollState())) { board(tileSize) }
                        Column(
                            Modifier
                                .width(panelWidth)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ScoreBoard(state)
                            TurnIndicator(state)
                            LastEventCard(state.lastEvent)
                        }
                    }
                } else {
                    val reservedForInfo = 250.dp
                    val boardSpace = minOf(maxWidth, maxHeight - reservedForInfo)
                    val tileSize = tileSizeFor(boardSpace, settings.showCoordinates)
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val contentWidth = maxOf(boardExtent(tileSize, settings.showCoordinates), 280.dp)
                        Column(Modifier.widthIn(max = contentWidth)) { ScoreBoard(state) }
                        Spacer(Modifier.height(16.dp))
                        board(tileSize)
                        Spacer(Modifier.height(16.dp))
                        Column(
                            Modifier.widthIn(max = contentWidth),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TurnIndicator(state)
                            LastEventCard(state.lastEvent)
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Restart this duel?") },
            text = { Text("The current board will be replaced with a completely new one. This game won't count toward your statistics.") },
            confirmButton = {
                TextButton(onClick = {
                    showRestartDialog = false
                    viewModel.startNewGame()
                }) { Text("Restart") }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GameTopBar(onBack: () -> Unit, onRestart: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to home")
        }
        Text(
            "MEMORY BOARD DUEL",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = onRestart) {
            Icon(Icons.Filled.Refresh, contentDescription = "Restart duel")
        }
    }
}

@Composable
private fun ScoreBoard(state: GameUiState) {
    val game = state.game
    val colors = GameTheme.colors
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SideScore(
                label = "PLAYER",
                score = game.playerPairs,
                color = colors.player,
                active = game.phase != GamePhase.GAME_OVER && game.currentSide == Side.PLAYER,
                modifier = Modifier.weight(1f)
            )
            Column(
                Modifier
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = pairsRemainingDescription(game.pairsRemaining) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SectionLabel("PAIRS LEFT")
                AnimatedNumber(game.pairsRemaining, MaterialTheme.colorScheme.onSurface)
            }
            SideScore(
                label = "CPU",
                score = game.cpuPairs,
                color = colors.cpu,
                active = game.phase != GamePhase.GAME_OVER && game.currentSide == Side.CPU,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SideScore(label: String, score: Int, color: Color, active: Boolean, modifier: Modifier = Modifier) {
    val labelColor by animateColorAsState(
        if (active) color else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "sideLabel"
    )
    Column(
        modifier.clearAndSetSemantics { contentDescription = "$label: $score pairs" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(if (active) color else Color.Transparent, CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = labelColor)
        }
        AnimatedNumber(score, color)
    }
}

@Composable
private fun AnimatedNumber(value: Int, color: Color) {
    AnimatedContent(
        targetState = value,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "number"
    ) { number ->
        Text("$number", style = MaterialTheme.typography.headlineMedium, color = color)
    }
}

@Composable
private fun TurnIndicator(state: GameUiState) {
    val game = state.game
    val colors = GameTheme.colors
    val isPlayer = game.currentSide == Side.PLAYER
    val accent = colors.side(game.currentSide)
    val title = when {
        game.phase == GamePhase.GAME_OVER -> "DUEL COMPLETE"
        isPlayer -> "YOUR TURN"
        else -> "CPU TURN"
    }
    val detail = when {
        game.phase == GamePhase.GAME_OVER -> "Counting pairs…"
        !isPlayer && state.cpuThinking -> "Remembering…"
        !isPlayer -> "Revealing…"
        game.phase == GamePhase.RESOLVING -> "Checking your pair…"
        game.selectedTileIds.size == 1 -> "SELECTED — choose a second tile"
        else -> "Choose any two hidden tiles"
    }
    val announcement = "${turnDescription(game)}. ${pairsRemainingDescription(game.pairsRemaining)}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = announcement
                liveRegion = LiveRegionMode.Polite
            },
        colors = CardDefaults.cardColors(containerColor = colors.sideContainer(game.currentSide).copy(alpha = 0.6f))
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(accent, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.onSideContainer(game.currentSide))
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSideContainer(game.currentSide).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun LastEventCard(event: GameEvent) {
    val (title, detail) = describeEvent(event)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        AnimatedContent(
            targetState = title to detail,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "lastEvent"
        ) { (heading, body) ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                SectionLabel(heading)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** Event text only mentions symbols that were face up for both sides to see. */
private fun describeEvent(event: GameEvent): Pair<String, String> = when (event) {
    GameEvent.Started -> "NEW DUEL" to "Reveal two tiles to begin."
    is GameEvent.PairFound ->
        if (event.side == Side.PLAYER) "PAIR FOUND" to "${event.symbol.pluralName} captured."
        else "CPU CAPTURED" to "${event.symbol.displayName} pair."
    is GameEvent.NoMatch ->
        if (event.side == Side.PLAYER) "NO MATCH" to "Try again."
        else "CPU MISSED" to "Your turn."
}
