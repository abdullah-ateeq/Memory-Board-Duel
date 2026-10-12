package com.example.memoryboardduel.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.ui.components.SectionLabel
import com.example.memoryboardduel.ui.components.drawSymbol
import com.example.memoryboardduel.ui.theme.GameTheme

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val version = remember { appVersion(context) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text(
                    "MEMORY BOARD DUEL",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    "Version $version",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "An offline competitive memory game.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            AboutSection(
                "HOW IT WORKS",
                "Remember hidden symbols, find matching positions, and capture pairs before the CPU."
            )
            AboutSection("BOARD", "6 × 6 board containing 18 matching pairs.")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    SectionLabel("THE SYMBOLS")
                    SymbolShowcase(Modifier.padding(top = 12.dp))
                }
            }
            AboutSection("PRIVACY", "No account. No internet connection. No personal data collection.")
        }
    }
}

@Composable
private fun AboutSection(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            SectionLabel(title)
            Text(body, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun SymbolShowcase(modifier: Modifier = Modifier) {
    val colors = GameTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SymbolType.entries.chunked(6).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { symbol ->
                    Canvas(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .semantics { contentDescription = symbol.displayName }
                    ) {
                        drawRect(colors.tileFace)
                        drawSymbol(symbol, colors.symbol(symbol))
                    }
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun appVersion(context: Context): String =
    try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    } catch (_: Exception) {
        "1.0"
    }
