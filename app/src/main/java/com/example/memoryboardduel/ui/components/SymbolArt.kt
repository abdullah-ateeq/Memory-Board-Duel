package com.example.memoryboardduel.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Original geometric artwork for each symbol, drawn so every shape reads without color. */
@Composable
fun SymbolIcon(symbol: SymbolType, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawSymbol(symbol, color) }
}

fun DrawScope.drawSymbol(symbol: SymbolType, color: Color) {
    val s = size.minDimension
    val c = center
    val r = s * 0.36f
    val stroke = s * 0.1f
    when (symbol) {
        SymbolType.CIRCLE -> drawCircle(color, radius = r * 0.82f, center = c)

        SymbolType.TRIANGLE -> drawPath(
            polygon(c.copy(y = c.y + r * 0.12f), r * 1.05f, 3, -PI / 2),
            color
        )

        SymbolType.SQUARE -> drawRoundRect(
            color,
            topLeft = Offset(c.x - r * 0.75f, c.y - r * 0.75f),
            size = Size(r * 1.5f, r * 1.5f),
            cornerRadius = CornerRadius(s * 0.05f)
        )

        SymbolType.STAR -> drawPath(star(c, r * 1.05f, r * 0.45f, 5), color)

        SymbolType.DIAMOND -> drawPath(
            Path().apply {
                moveTo(c.x, c.y - r)
                lineTo(c.x + r * 0.66f, c.y)
                lineTo(c.x, c.y + r)
                lineTo(c.x - r * 0.66f, c.y)
                close()
            },
            color
        )

        SymbolType.HEXAGON -> drawPath(
            polygon(c, r * 0.9f, 6, 0.0),
            color,
            style = Stroke(width = stroke, join = StrokeJoin.Round)
        )

        SymbolType.MOON -> {
            val disc = Path().apply { addOval(Rect(c, r * 0.9f)) }
            val bite = Path().apply { addOval(Rect(Offset(c.x + r * 0.42f, c.y - r * 0.22f), r * 0.78f)) }
            drawPath(Path.combine(PathOperation.Difference, disc, bite), color)
        }

        SymbolType.BOLT -> drawPath(
            Path().apply {
                moveTo(c.x + r * 0.2f, c.y - r)
                lineTo(c.x - r * 0.55f, c.y + r * 0.12f)
                lineTo(c.x - r * 0.02f, c.y + r * 0.12f)
                lineTo(c.x - r * 0.25f, c.y + r)
                lineTo(c.x + r * 0.58f, c.y - r * 0.18f)
                lineTo(c.x + r * 0.05f, c.y - r * 0.18f)
                close()
            },
            color
        )

        SymbolType.CROSS -> {
            val arm = r * 0.36f
            val corner = CornerRadius(s * 0.035f)
            drawRoundRect(color, Offset(c.x - arm, c.y - r * 0.9f), Size(arm * 2, r * 1.8f), corner)
            drawRoundRect(color, Offset(c.x - r * 0.9f, c.y - arm), Size(r * 1.8f, arm * 2), corner)
        }

        SymbolType.HEART -> drawPath(
            Path().apply {
                moveTo(c.x, c.y + r * 0.85f)
                cubicTo(c.x - r * 1.35f, c.y - r * 0.05f, c.x - r * 0.75f, c.y - r * 1.05f, c.x, c.y - r * 0.42f)
                cubicTo(c.x + r * 0.75f, c.y - r * 1.05f, c.x + r * 1.35f, c.y - r * 0.05f, c.x, c.y + r * 0.85f)
                close()
            },
            color
        )

        SymbolType.FLOWER -> {
            val petals = Path()
            for (i in 0 until 5) {
                val angle = -PI / 2 + i * 2 * PI / 5
                val p = Offset(c.x + (r * 0.5f * cos(angle)).toFloat(), c.y + (r * 0.5f * sin(angle)).toFloat())
                petals.addOval(Rect(p, r * 0.42f))
            }
            val heart = Path().apply { addOval(Rect(c, r * 0.22f)) }
            drawPath(Path.combine(PathOperation.Difference, petals, heart), color)
            drawCircle(color, radius = r * 0.12f, center = c)
        }

        SymbolType.SHIELD -> drawPath(
            Path().apply {
                moveTo(c.x, c.y - r)
                quadraticTo(c.x + r * 0.45f, c.y - r * 0.72f, c.x + r * 0.85f, c.y - r * 0.72f)
                lineTo(c.x + r * 0.85f, c.y - r * 0.05f)
                cubicTo(c.x + r * 0.85f, c.y + r * 0.5f, c.x + r * 0.4f, c.y + r * 0.82f, c.x, c.y + r)
                cubicTo(c.x - r * 0.4f, c.y + r * 0.82f, c.x - r * 0.85f, c.y + r * 0.5f, c.x - r * 0.85f, c.y - r * 0.05f)
                lineTo(c.x - r * 0.85f, c.y - r * 0.72f)
                quadraticTo(c.x - r * 0.45f, c.y - r * 0.72f, c.x, c.y - r)
                close()
            },
            color
        )

        SymbolType.RING -> {
            drawCircle(color, radius = r * 0.72f, center = c, style = Stroke(width = stroke * 1.1f))
            drawCircle(color, radius = r * 0.16f, center = c.copy(y = c.y - r * 0.72f))
        }

        SymbolType.CROWN -> drawPath(
            Path().apply {
                moveTo(c.x - r * 0.9f, c.y + r * 0.6f)
                lineTo(c.x - r * 0.95f, c.y - r * 0.6f)
                lineTo(c.x - r * 0.45f, c.y - r * 0.05f)
                lineTo(c.x, c.y - r * 0.85f)
                lineTo(c.x + r * 0.45f, c.y - r * 0.05f)
                lineTo(c.x + r * 0.95f, c.y - r * 0.6f)
                lineTo(c.x + r * 0.9f, c.y + r * 0.6f)
                close()
            },
            color
        )

        SymbolType.LEAF -> rotate(-45f, c) {
            val leaf = Path().apply {
                moveTo(c.x, c.y - r)
                quadraticTo(c.x + r * 0.9f, c.y, c.x, c.y + r * 0.8f)
                quadraticTo(c.x - r * 0.9f, c.y, c.x, c.y - r)
                close()
            }
            drawPath(leaf, color)
            drawLine(
                color,
                start = Offset(c.x, c.y + r * 0.6f),
                end = Offset(c.x, c.y + r * 1.1f),
                strokeWidth = stroke * 0.7f,
                cap = StrokeCap.Round
            )
        }

        SymbolType.WAVE -> {
            for (offset in listOf(-r * 0.38f, r * 0.38f)) {
                val wave = Path().apply {
                    val y = c.y + offset
                    moveTo(c.x - r, y)
                    cubicTo(c.x - r * 0.65f, y - r * 0.45f, c.x - r * 0.35f, y - r * 0.45f, c.x, y)
                    cubicTo(c.x + r * 0.35f, y + r * 0.45f, c.x + r * 0.65f, y + r * 0.45f, c.x + r, y)
                }
                drawPath(wave, color, style = Stroke(width = stroke * 0.9f, cap = StrokeCap.Round))
            }
        }

        SymbolType.SUN -> {
            drawCircle(color, radius = r * 0.44f, center = c)
            for (i in 0 until 8) {
                val angle = i * PI / 4
                val dx = cos(angle).toFloat()
                val dy = sin(angle).toFloat()
                drawLine(
                    color,
                    start = Offset(c.x + dx * r * 0.66f, c.y + dy * r * 0.66f),
                    end = Offset(c.x + dx * r * 0.98f, c.y + dy * r * 0.98f),
                    strokeWidth = stroke * 0.7f,
                    cap = StrokeCap.Round
                )
            }
        }

        SymbolType.SPIRAL -> {
            val spiral = Path()
            val turns = 2.4
            val steps = 64
            for (i in 0..steps) {
                val t = i / steps.toDouble()
                val angle = t * turns * 2 * PI
                val radius = r * 0.95f * t.toFloat()
                val x = c.x + (radius * cos(angle)).toFloat()
                val y = c.y + (radius * sin(angle)).toFloat()
                if (i == 0) spiral.moveTo(x, y) else spiral.lineTo(x, y)
            }
            drawPath(spiral, color, style = Stroke(width = stroke * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** Small ownership glyph: a person for the player, a chip for the CPU. */
@Composable
fun OwnerMark(side: Side, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) { drawOwnerMark(side, color) }
}

fun DrawScope.drawOwnerMark(side: Side, color: Color) {
    val s = size.minDimension
    val c = center
    when (side) {
        Side.PLAYER -> {
            drawCircle(color, radius = s * 0.17f, center = c.copy(y = c.y - s * 0.16f))
            drawPath(
                Path().apply {
                    moveTo(c.x - s * 0.32f, c.y + s * 0.36f)
                    cubicTo(c.x - s * 0.32f, c.y + s * 0.02f, c.x + s * 0.32f, c.y + s * 0.02f, c.x + s * 0.32f, c.y + s * 0.36f)
                    close()
                },
                color
            )
        }
        Side.CPU -> {
            val body = s * 0.5f
            val pin = s * 0.07f
            drawRoundRect(
                color,
                topLeft = Offset(c.x - body / 2, c.y - body / 2),
                size = Size(body, body),
                cornerRadius = CornerRadius(s * 0.07f)
            )
            for (i in -1..1) {
                val o = i * s * 0.15f
                drawLine(color, Offset(c.x + o, c.y - body / 2), Offset(c.x + o, c.y - body / 2 - s * 0.12f), pin, StrokeCap.Round)
                drawLine(color, Offset(c.x + o, c.y + body / 2), Offset(c.x + o, c.y + body / 2 + s * 0.12f), pin, StrokeCap.Round)
                drawLine(color, Offset(c.x - body / 2, c.y + o), Offset(c.x - body / 2 - s * 0.12f, c.y + o), pin, StrokeCap.Round)
                drawLine(color, Offset(c.x + body / 2, c.y + o), Offset(c.x + body / 2 + s * 0.12f, c.y + o), pin, StrokeCap.Round)
            }
        }
    }
}

/** Original tile back: a faint diagonal lattice with an inset frame and a centered diamond. */
fun DrawScope.drawTileBack(base: Color, pattern: Color, mark: Color) {
    val s = size.minDimension
    drawRect(base)
    val step = s / 5f
    var x = -s
    while (x < s * 2) {
        drawLine(pattern, Offset(x, 0f), Offset(x + s, s), strokeWidth = s * 0.02f)
        drawLine(pattern, Offset(x + s, 0f), Offset(x, s), strokeWidth = s * 0.02f)
        x += step
    }
    val inset = s * 0.12f
    drawRoundRect(
        mark.copy(alpha = 0.22f),
        topLeft = Offset(inset, inset),
        size = Size(s - inset * 2, s - inset * 2),
        cornerRadius = CornerRadius(s * 0.1f),
        style = Stroke(width = s * 0.025f)
    )
    val c = center
    val d = s * 0.15f
    val diamond = Path().apply {
        moveTo(c.x, c.y - d)
        lineTo(c.x + d, c.y)
        lineTo(c.x, c.y + d)
        lineTo(c.x - d, c.y)
        close()
    }
    drawPath(diamond, base)
    drawPath(diamond, mark.copy(alpha = 0.75f), style = Stroke(width = s * 0.03f, join = StrokeJoin.Round))
    drawCircle(mark.copy(alpha = 0.75f), radius = s * 0.03f, center = c)
}

private fun polygon(center: Offset, radius: Float, sides: Int, startAngle: Double): Path = Path().apply {
    for (i in 0 until sides) {
        val angle = startAngle + i * 2 * PI / sides
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun star(center: Offset, outer: Float, inner: Float, points: Int): Path = Path().apply {
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outer else inner
        val angle = -PI / 2 + i * PI / points
        val x = center.x + (radius * cos(angle)).toFloat()
        val y = center.y + (radius * sin(angle)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
