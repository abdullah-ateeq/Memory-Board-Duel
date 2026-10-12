package com.example.memoryboardduel.util

import com.example.memoryboardduel.game.model.GamePhase
import com.example.memoryboardduel.game.model.MemoryBoardGameState
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.TileState
import com.example.memoryboardduel.game.model.VisibleTile

/**
 * Screen-reader label for a tile. Built only from [VisibleTile], so a hidden tile can never
 * leak its symbol. Captured tiles describe ownership, not the symbol.
 */
fun tileContentDescription(tile: VisibleTile): String {
    val position = tile.coordinate.label
    return when (tile.state) {
        TileState.HIDDEN -> "Hidden tile at $position"
        TileState.REVEALED -> "${tile.visibleSymbol?.displayName ?: "Revealed"} tile at $position"
        TileState.PLAYER_CAPTURED -> "Player captured tile at $position"
        TileState.CPU_CAPTURED -> "CPU captured tile at $position"
    }
}

fun turnDescription(state: MemoryBoardGameState): String = when {
    state.phase == GamePhase.GAME_OVER -> "Game over"
    state.currentSide == Side.PLAYER -> "Your turn"
    else -> "CPU turn"
}

fun pairsRemainingDescription(pairsRemaining: Int): String =
    if (pairsRemaining == 1) "1 pair remaining" else "$pairsRemaining pairs remaining"
