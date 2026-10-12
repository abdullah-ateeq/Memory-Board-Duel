package com.example.memoryboardduel.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.ui.components.drawOwnerMark
import com.example.memoryboardduel.ui.components.drawSymbol
import com.example.memoryboardduel.ui.components.drawTileBack
import com.example.memoryboardduel.ui.theme.GameTheme
import kotlinx.coroutines.delay

// Purely decorative pair positions; unrelated to any real game board.
private const val DECO_FIRST = 8
private const val DECO_SECOND = 27
private val DecoSymbol = SymbolType.STAR

@Composable
fun SplashScreen(animationsEnabled: Boolean, onFinished: () -> Unit) {
    val finish by rememberUpdatedState(onFinished)
    val progress = remember { Animatable(if (animationsEnabled) 0f else 1f) }

    LaunchedEffect(Unit) {
        if (animationsEnabled) {
            progress.animateTo(1f, tween(1350, easing = LinearEasing))
            delay(150)
        } else {
            delay(500)
        }
        finish()
    }

    val p = progress.value
    Box(
        Modifier
            .fillMaxSize()
            .clearAndSetSemantics { contentDescription = "Memory Board Duel" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DecoBoard(p)
            Spacer(Modifier.height(36.dp))
            val titleAlpha = segment(p, 0.7f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(titleAlpha).graphicsLayer { translationY = (1f - titleAlpha) * 24f }
            ) {
                Text("MEMORY", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                Text("BOARD", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
                Text("DUEL", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DecoBoard(p: Float) {
    val colors = GameTheme.colors
    val tile = 30.dp
    val shape = RoundedCornerShape(6.dp)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(6) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(6) { column ->
                    val index = row * 6 + column
                    val appear = segment(p, index / 36f * 0.22f, index / 36f * 0.22f + 0.08f)
                    val isPair = index == DECO_FIRST || index == DECO_SECOND
                    // Flip up 0.25–0.38, back down 0.5–0.62, captured from 0.66.
                    val rotation = if (!isPair) 0f else when {
                        p < 0.5f -> 180f * segment(p, 0.25f, 0.38f)
                        else -> 180f * (1f - segment(p, 0.5f, 0.62f))
                    }
                    val captured = isPair && p >= 0.66f
                    val capturedAlpha = segment(p, 0.66f, 0.78f)
                    Canvas(
                        Modifier
                            .size(tile)
                            .alpha(appear)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                                scaleX = 0.85f + 0.15f * appear
                                scaleY = 0.85f + 0.15f * appear
                            }
                            .clip(shape)
                    ) {
                        when {
                            captured -> {
                                drawTileBack(colors.tileBack, colors.tileBackPattern, colors.tileBackMark)
                                drawRect(colors.playerContainer.copy(alpha = capturedAlpha))
                                drawRoundRect(
                                    colors.player.copy(alpha = 0.6f * capturedAlpha),
                                    cornerRadius = CornerRadius(6.dp.toPx()),
                                    style = Stroke(1.5.dp.toPx())
                                )
                                drawOwnerMark(Side.PLAYER, colors.player.copy(alpha = capturedAlpha))
                            }
                            rotation > 90f -> {
                                drawRect(colors.tileFace)
                                drawSymbol(DecoSymbol, colors.symbol(DecoSymbol))
                            }
                            else -> drawTileBack(colors.tileBack, colors.tileBackPattern, colors.tileBackMark)
                        }
                    }
                }
            }
        }
    }
}

private fun segment(p: Float, start: Float, end: Float): Float =
    ((p - start) / (end - start)).coerceIn(0f, 1f)
