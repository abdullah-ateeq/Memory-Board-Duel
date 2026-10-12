package com.example.memoryboardduel.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.memoryboardduel.game.model.BOARD_SIZE
import com.example.memoryboardduel.game.model.GamePhase
import com.example.memoryboardduel.ui.GameUiState
import com.example.memoryboardduel.ui.theme.GameTheme

val BoardTileGap = 6.dp
val CoordinateLabelSize = 16.dp

/** Width/height the board occupies for a given tile size. */
fun boardExtent(tileSize: Dp, showCoordinates: Boolean): Dp =
    tileSize * BOARD_SIZE + BoardTileGap * (BOARD_SIZE - 1) + if (showCoordinates) CoordinateLabelSize + 4.dp else 0.dp

/** Largest tile size that fits [available], kept within comfortable touch-target bounds. */
fun tileSizeFor(available: Dp, showCoordinates: Boolean): Dp {
    val labels = if (showCoordinates) CoordinateLabelSize + 4.dp else 0.dp
    val raw = (available - labels - BoardTileGap * (BOARD_SIZE - 1)) / BOARD_SIZE
    return raw.coerceIn(36.dp, 76.dp)
}

@Composable
fun GameBoard(
    state: GameUiState,
    tileSize: Dp,
    showCoordinates: Boolean,
    flipDurationMs: Int,
    animationsEnabled: Boolean,
    onTileClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val game = state.game
    val colors = GameTheme.colors
    val activeColor = colors.side(game.currentSide)
    val singleSelection = game.phase == GamePhase.PLAYER_TURN && game.selectedTileIds.size == 1

    Column(modifier, horizontalAlignment = Alignment.End) {
        if (showCoordinates) {
            Row(horizontalArrangement = Arrangement.spacedBy(BoardTileGap)) {
                repeat(BOARD_SIZE) { column ->
                    CoordinateLabel(text = "${'A' + column}", modifier = Modifier.width(tileSize))
                }
            }
        }
        Row(verticalAlignment = Alignment.Top) {
            if (showCoordinates) {
                Column(verticalArrangement = Arrangement.spacedBy(BoardTileGap)) {
                    repeat(BOARD_SIZE) { row ->
                        Box(Modifier.size(CoordinateLabelSize, tileSize), contentAlignment = Alignment.Center) {
                            CoordinateLabel(text = "${row + 1}")
                        }
                    }
                }
                Box(Modifier.width(4.dp))
            }
            Box {
                Column(verticalArrangement = Arrangement.spacedBy(BoardTileGap)) {
                    repeat(BOARD_SIZE) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(BoardTileGap)) {
                            repeat(BOARD_SIZE) { column ->
                                val tile = game.visibleTiles[row * BOARD_SIZE + column]
                                MemoryTileCard(
                                    tile = tile,
                                    tileSize = tileSize,
                                    selected = singleSelection && tile.id in game.selectedTileIds,
                                    matched = tile.id in state.matchedTileIds,
                                    mismatched = tile.id in state.mismatchedTileIds,
                                    matchColor = activeColor,
                                    flipDurationMs = flipDurationMs,
                                    animationsEnabled = animationsEnabled,
                                    onClick = { onTileClick(tile.id) }
                                )
                            }
                        }
                    }
                }
                if (state.matchedTileIds.size == 2) {
                    MatchConnection(
                        fromIndex = state.matchedTileIds[0],
                        toIndex = state.matchedTileIds[1],
                        tileSize = tileSize,
                        color = activeColor,
                        animate = animationsEnabled,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun CoordinateLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        // Coordinates are already part of every tile's description.
        modifier = modifier.clearAndSetSemantics { }
    )
}

/** A short line drawn between the two tiles of a newly found pair. */
@Composable
private fun MatchConnection(
    fromIndex: Int,
    toIndex: Int,
    tileSize: Dp,
    color: Color,
    animate: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = remember(fromIndex, toIndex) { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(fromIndex, toIndex) {
        if (animate) progress.animateTo(1f, tween(320))
    }
    Canvas(modifier.clearAndSetSemantics { }) {
        val cell = tileSize.toPx()
        val gap = BoardTileGap.toPx()
        fun centerOf(index: Int) = Offset(
            x = (index % BOARD_SIZE) * (cell + gap) + cell / 2,
            y = (index / BOARD_SIZE) * (cell + gap) + cell / 2
        )
        val start = centerOf(fromIndex)
        val end = centerOf(toIndex)
        val current = start + (end - start) * progress.value
        drawLine(color.copy(alpha = 0.55f), start, current, strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(color, radius = 5.dp.toPx(), center = start)
        if (progress.value >= 1f) drawCircle(color, radius = 5.dp.toPx(), center = end)
    }
}
