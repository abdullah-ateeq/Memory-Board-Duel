package com.example.memoryboardduel.game.model

const val BOARD_SIZE = 6
const val TILE_COUNT = BOARD_SIZE * BOARD_SIZE
const val PAIR_COUNT = TILE_COUNT / 2

/**
 * Internal tile. Holds the true symbol and must never leave the game engine.
 */
data class MemoryTile(
    val id: Int,
    val coordinate: Coordinate,
    val symbol: SymbolType,
    val state: TileState
)

/**
 * UI- and CPU-safe projection of a tile. [visibleSymbol] is null while the tile is hidden.
 */
data class VisibleTile(
    val id: Int,
    val coordinate: Coordinate,
    val state: TileState,
    val visibleSymbol: SymbolType?
)

/** Public game state: contains only information either side could legitimately see. */
data class MemoryBoardGameState(
    val visibleTiles: List<VisibleTile>,
    val phase: GamePhase,
    val currentSide: Side,
    val selectedTileIds: List<Int>,
    val playerPairs: Int,
    val cpuPairs: Int,
    val pairsRemaining: Int,
    val turnCount: Int,
    val lastMatch: Boolean?,
    val lastCapturedBy: Side?
) {
    fun tile(id: Int): VisibleTile = visibleTiles[id]

    val hiddenTiles: List<VisibleTile> get() = visibleTiles.filter { it.state == TileState.HIDDEN }
}

enum class GameOutcome {
    WIN,
    LOSS,
    DRAW
}

data class GameResult(
    val playerPairs: Int,
    val cpuPairs: Int,
    val turns: Int
) {
    val totalPairs: Int get() = playerPairs + cpuPairs

    val outcome: GameOutcome
        get() = when {
            playerPairs > cpuPairs -> GameOutcome.WIN
            cpuPairs > playerPairs -> GameOutcome.LOSS
            else -> GameOutcome.DRAW
        }

    val isPerfectMemory: Boolean get() = playerPairs == PAIR_COUNT
}
