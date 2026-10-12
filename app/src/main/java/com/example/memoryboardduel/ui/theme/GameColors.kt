package com.example.memoryboardduel.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType

/** Board-specific colors. Player and CPU accents come from the Material color scheme. */
@Immutable
data class GameColors(
    val tileBack: Color,
    val tileBackPattern: Color,
    val tileBackMark: Color,
    val tileFace: Color,
    val player: Color,
    val playerContainer: Color,
    val onPlayerContainer: Color,
    val cpu: Color,
    val cpuContainer: Color,
    val onCpuContainer: Color,
    private val symbolColors: List<Color>
) {
    fun symbol(symbol: SymbolType): Color = symbolColors[symbol.ordinal % symbolColors.size]

    fun side(side: Side): Color = if (side == Side.PLAYER) player else cpu

    fun sideContainer(side: Side): Color = if (side == Side.PLAYER) playerContainer else cpuContainer

    fun onSideContainer(side: Side): Color = if (side == Side.PLAYER) onPlayerContainer else onCpuContainer
}

// Muted accents, one per symbol. Shapes stay distinct, so color is never the only cue.
private val LightSymbolColors = listOf(
    Color(0xFF3F6E8C), Color(0xFFA0583C), Color(0xFF4F7A55), Color(0xFFB08A2E), Color(0xFF6A5A8E),
    Color(0xFF3D7C7A), Color(0xFF5C6475), Color(0xFFB07A2A), Color(0xFF8A4A5C), Color(0xFFA84E50),
    Color(0xFF8A5F9A), Color(0xFF46607E), Color(0xFF7A6A3A), Color(0xFF9C7A2C), Color(0xFF4E7F4A),
    Color(0xFF3B6F9A), Color(0xFFB8732E), Color(0xFF6E5A7E)
)

private val DarkSymbolColors = listOf(
    Color(0xFF8DB7D1), Color(0xFFE0A086), Color(0xFF9CC4A0), Color(0xFFE2C377), Color(0xFFB4A6D8),
    Color(0xFF8CC7C3), Color(0xFFAFB6C6), Color(0xFFE8B66E), Color(0xFFD597A8), Color(0xFFE59A9B),
    Color(0xFFCDA8DA), Color(0xFF9BB2CF), Color(0xFFCFC08F), Color(0xFFE8C66E), Color(0xFFA2CC9C),
    Color(0xFF93BCE0), Color(0xFFF0B87A), Color(0xFFBFAED0)
)

fun gameColors(scheme: ColorScheme, dark: Boolean): GameColors = GameColors(
    tileBack = if (dark) DarkTileBack else LightTileBack,
    tileBackPattern = if (dark) DarkTileBackPattern else LightTileBackPattern,
    tileBackMark = if (dark) DarkTileBackMark else LightTileBackMark,
    tileFace = if (dark) DarkTileFace else LightTileFace,
    player = scheme.primary,
    playerContainer = scheme.primaryContainer,
    onPlayerContainer = scheme.onPrimaryContainer,
    cpu = scheme.secondary,
    cpuContainer = scheme.secondaryContainer,
    onCpuContainer = scheme.onSecondaryContainer,
    symbolColors = if (dark) DarkSymbolColors else LightSymbolColors
)

val LocalGameColors = staticCompositionLocalOf<GameColors> { error("GameColors not provided") }
