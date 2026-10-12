package com.example.memoryboardduel.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.memoryboardduel.game.model.GameOutcome
import com.example.memoryboardduel.ui.FinishedGame
import com.example.memoryboardduel.ui.MemoryBoardViewModel
import com.example.memoryboardduel.ui.components.StatValue
import com.example.memoryboardduel.ui.theme.GameTheme
import kotlin.random.Random

@Composable
fun ResultScreen(
    viewModel: MemoryBoardViewModel,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val finished by viewModel.lastResult.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    BackHandler(onBack = onHome)

    val current = finished
    if (current == null) {
        LaunchedEffect(Unit) { onHome() }
        return
    }
    ResultContent(current, settings.animations, onPlayAgain, onHome)
}

@Composable
private fun ResultContent(
    finished: FinishedGame,
    animationsEnabled: Boolean,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val result = finished.result
    val outcome = result.outcome
    val colors = GameTheme.colors

    val title = when (outcome) {
        GameOutcome.WIN -> "YOU WIN"
        GameOutcome.LOSS -> "YOU LOSE"
        GameOutcome.DRAW -> "DRAW"
    }
    val message = when {
        result.isPerfectMemory -> "Perfect memory."
        outcome == GameOutcome.WIN -> "Your memory won the duel."
        outcome == GameOutcome.LOSS -> "The CPU remembered more."
        else -> "Both sides remembered equally well."
    }
    val titleColor = when (outcome) {
        GameOutcome.WIN -> colors.player
        GameOutcome.LOSS -> colors.cpu
        GameOutcome.DRAW -> MaterialTheme.colorScheme.onBackground
    }

    val countUp = animationsEnabled && outcome == GameOutcome.WIN
    val fade = remember { Animatable(if (animationsEnabled) 0f else 1f) }
    val count = remember { Animatable(if (countUp) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (animationsEnabled) fade.animateTo(1f, tween(500))
        if (countUp) count.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    val shownPlayer = (result.playerPairs * count.value).toInt()
    val shownCpu = (result.cpuPairs * count.value).toInt()

    Box(Modifier.fillMaxSize()) {
        if (animationsEnabled && outcome == GameOutcome.WIN) {
            TileParticles(listOf(colors.player, colors.cpu, MaterialTheme.colorScheme.tertiary))
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .alpha(fade.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                Modifier.widthIn(max = 460.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                if (result.isPerfectMemory) {
                    Surface(
                        color = colors.playerContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "PERFECT MEMORY",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onPlayerContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.displayMedium,
                    color = titleColor,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "$shownPlayer — $shownCpu",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "${result.playerPairs} pairs to ${result.cpuPairs}"
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 16.dp, horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            StatValue("YOUR PAIRS", "${result.playerPairs}", Modifier.weight(1f), valueColor = colors.player)
                            StatValue("CPU PAIRS", "${result.cpuPairs}", Modifier.weight(1f), valueColor = colors.cpu)
                            StatValue("TOTAL PAIRS", "${result.totalPairs}", Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth()) {
                            StatValue("TURNS", "${result.turns}", Modifier.weight(1f))
                            StatValue(
                                if (finished.isNewBest) "NEW BEST" else "BEST SCORE",
                                "${finished.bestPairs}",
                                Modifier.weight(1f)
                            )
                            StatValue("WIN STREAK", "${finished.winStreak}", Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
                Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("PLAY AGAIN")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("HOME")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private data class Particle(
    val x: Float,
    val delay: Float,
    val size: Float,
    val drift: Float,
    val spin: Float,
    val colorIndex: Int
)

/** A few small tiles drifting upward once: a quiet, board-themed celebration. */
@Composable
private fun TileParticles(palette: List<Color>) {
    val particles = remember {
        val random = Random(System.nanoTime())
        List(22) {
            Particle(
                x = random.nextFloat(),
                delay = random.nextFloat() * 0.4f,
                size = 6f + random.nextFloat() * 8f,
                drift = (random.nextFloat() - 0.5f) * 60f,
                spin = (random.nextFloat() - 0.5f) * 240f,
                colorIndex = random.nextInt(palette.size)
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(2600, easing = LinearEasing)) }

    Canvas(Modifier.fillMaxSize().clearAndSetSemantics { }) {
        particles.forEach { particle ->
            val t = ((progress.value - particle.delay) / (1f - particle.delay)).coerceIn(0f, 1f)
            if (t <= 0f || t >= 1f) return@forEach
            val side = particle.size.dp.toPx()
            val x = particle.x * size.width + particle.drift * t
            val y = size.height * (1.02f - t * 0.9f)
            val alpha = (if (t < 0.2f) t / 0.2f else 1f - (t - 0.2f) / 0.8f) * 0.7f
            rotate(particle.spin * t, Offset(x, y)) {
                drawRoundRect(
                    palette[particle.colorIndex].copy(alpha = alpha),
                    topLeft = Offset(x - side / 2, y - side / 2),
                    size = Size(side, side),
                    cornerRadius = CornerRadius(side * 0.25f)
                )
            }
        }
    }
}
