package com.example.memoryboardduel.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.TileState
import com.example.memoryboardduel.game.model.VisibleTile
import com.example.memoryboardduel.ui.theme.GameTheme
import com.example.memoryboardduel.util.tileContentDescription

/**
 * One board tile. Everything it draws comes from [VisibleTile], so a hidden tile has no symbol
 * to show, describe or leak.
 */
@Composable
fun MemoryTileCard(
    tile: VisibleTile,
    tileSize: Dp,
    selected: Boolean,
    matched: Boolean,
    mismatched: Boolean,
    matchColor: Color,
    flipDurationMs: Int,
    animationsEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GameTheme.colors
    val faceUp = tile.state != TileState.HIDDEN
    val rotation by animateFloatAsState(
        targetValue = if (faceUp) 180f else 0f,
        animationSpec = if (flipDurationMs > 0) tween(flipDurationMs, easing = FastOutSlowInEasing) else snap(),
        label = "flip"
    )

    val pulse = remember { Animatable(1f) }
    LaunchedEffect(matched, animationsEnabled) {
        if (matched && animationsEnabled) {
            repeat(2) {
                pulse.animateTo(1.1f, tween(140))
                pulse.animateTo(1f, tween(140))
            }
        } else {
            pulse.snapTo(1f)
        }
    }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(mismatched, animationsEnabled) {
        if (mismatched && animationsEnabled) {
            shake.animateTo(0f, keyframes {
                durationMillis = 360
                -7f at 50
                7f at 120
                -5f at 190
                4f at 260
                0f at 360
            })
        } else {
            shake.snapTo(0f)
        }
    }

    val shape = RoundedCornerShape(tileSize * 0.18f)
    val description = tileContentDescription(tile)
    val interactive = !tile.state.isCaptured

    Box(
        modifier = modifier
            .size(tileSize)
            .graphicsLayer {
                translationX = shake.value * density
                scaleX = pulse.value
                scaleY = pulse.value
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .shadow(if (tile.state.isCaptured) 0.dp else 3.dp, shape, clip = false)
            .clip(shape)
            .then(if (interactive) Modifier.clickable(onClick = onClick) else Modifier)
            .clearAndSetSemantics {
                contentDescription = description
                if (interactive) role = Role.Button
                if (selected) stateDescription = "Selected"
            }
    ) {
        if (rotation <= 90f) {
            Canvas(Modifier.fillMaxSize()) {
                drawTileBack(colors.tileBack, colors.tileBackPattern, colors.tileBackMark)
            }
        } else {
            // Counter-rotate so the face is not mirrored.
            Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                TileFace(
                    tile = tile,
                    tileSize = tileSize,
                    selected = selected,
                    matched = matched,
                    mismatched = mismatched,
                    matchColor = matchColor
                )
            }
        }
    }
}

@Composable
private fun TileFace(
    tile: VisibleTile,
    tileSize: Dp,
    selected: Boolean,
    matched: Boolean,
    mismatched: Boolean,
    matchColor: Color
) {
    val colors = GameTheme.colors
    val shape = RoundedCornerShape(tileSize * 0.18f)
    val owner = when (tile.state) {
        TileState.PLAYER_CAPTURED -> Side.PLAYER
        TileState.CPU_CAPTURED -> Side.CPU
        else -> null
    }

    if (owner != null) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(shape)
                .border(1.5.dp, colors.side(owner).copy(alpha = 0.55f), shape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) { drawRect(colors.sideContainer(owner).copy(alpha = 0.55f)) }
            // The claimed symbol stays as a faint watermark; ownership is the focus.
            tile.visibleSymbol?.let {
                SymbolIcon(it, colors.onSideContainer(owner).copy(alpha = 0.12f), Modifier.fillMaxSize().padding(tileSize * 0.08f))
            }
            OwnerMark(owner, colors.side(owner), Modifier.size(tileSize * 0.42f))
        }
        return
    }

    val borderColor = when {
        matched -> matchColor
        mismatched -> colors.cpu.copy(alpha = 0.5f)
        selected -> colors.player
        else -> colors.tileBackMark.copy(alpha = 0.25f)
    }
    Box(
        Modifier
            .fillMaxSize()
            .clip(shape)
            .border(if (selected || matched) 2.5.dp else 1.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) { drawRect(colors.tileFace) }
        tile.visibleSymbol?.let {
            SymbolIcon(it, colors.symbol(it), Modifier.fillMaxSize().padding(tileSize * 0.1f))
        }
    }
}
