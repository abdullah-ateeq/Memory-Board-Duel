package com.example.memoryboardduel.game.logic

import com.example.memoryboardduel.game.model.GamePhase
import com.example.memoryboardduel.game.model.GameResult
import com.example.memoryboardduel.game.model.MemoryBoardGameState
import com.example.memoryboardduel.game.model.MemoryTile
import com.example.memoryboardduel.game.model.PAIR_COUNT
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.game.model.TILE_COUNT
import com.example.memoryboardduel.game.model.TileState
import com.example.memoryboardduel.game.model.VisibleTile
import kotlin.random.Random

enum class RejectReason {
    WRONG_TURN,
    BUSY,
    CAPTURED,
    ALREADY_SELECTED,
    INVALID_TILE,
    GAME_OVER
}

sealed interface RevealResult {
    /** The tile is now face up. [tile] carries its symbol because it was legitimately revealed. */
    data class Revealed(val tile: VisibleTile, val selectionComplete: Boolean) : RevealResult
    data class Rejected(val reason: RejectReason) : RevealResult
}

/** Read-only look at the two face-up tiles before they are resolved. */
data class SelectionEvaluation(
    val side: Side,
    val tileIds: List<Int>,
    val isMatch: Boolean,
    val symbols: List<SymbolType>
)

data class ResolveOutcome(
    val side: Side,
    val tileIds: List<Int>,
    val isMatch: Boolean,
    val symbols: List<SymbolType>,
    val gameOver: Boolean
)

/**
 * Pure game rules. Owns the internal board (with every symbol) and only ever hands out
 * [MemoryBoardGameState] / [VisibleTile] projections in which hidden symbols are null.
 */
class MemoryBoardGameEngine(
    board: List<MemoryTile>,
    startingSide: Side = Side.PLAYER
) {
    private val tiles: MutableList<MemoryTile> = board.toMutableList()
    private val selected = mutableListOf<Int>()

    private var currentSide: Side = startingSide
    private var phase: GamePhase = turnPhaseFor(startingSide)
    private var playerPairs = 0
    private var cpuPairs = 0
    private var turnCount = 0
    private var lastMatch: Boolean? = null
    private var lastCapturedBy: Side? = null

    init {
        require(tiles.size == TILE_COUNT) { "Board must contain $TILE_COUNT tiles" }
        require(tiles.mapIndexed { index, tile -> tile.id == index }.all { it }) { "Tile ids must be 0 until $TILE_COUNT" }
        require(tiles.map { it.coordinate }.toSet().size == TILE_COUNT) { "Coordinates must be unique" }
    }

    val state: MemoryBoardGameState
        get() = MemoryBoardGameState(
            visibleTiles = tiles.map { it.toVisible() },
            phase = phase,
            currentSide = currentSide,
            selectedTileIds = selected.toList(),
            playerPairs = playerPairs,
            cpuPairs = cpuPairs,
            pairsRemaining = pairsRemaining,
            turnCount = turnCount,
            lastMatch = lastMatch,
            lastCapturedBy = lastCapturedBy
        )

    val pairsRemaining: Int get() = PAIR_COUNT - playerPairs - cpuPairs

    val isGameOver: Boolean get() = phase == GamePhase.GAME_OVER

    fun reveal(tileId: Int, side: Side): RevealResult {
        if (phase == GamePhase.GAME_OVER) return RevealResult.Rejected(RejectReason.GAME_OVER)
        if (phase == GamePhase.RESOLVING) return RevealResult.Rejected(RejectReason.BUSY)
        if (side != currentSide) return RevealResult.Rejected(RejectReason.WRONG_TURN)
        val tile = tiles.getOrNull(tileId) ?: return RevealResult.Rejected(RejectReason.INVALID_TILE)
        if (tile.state.isCaptured) return RevealResult.Rejected(RejectReason.CAPTURED)
        if (tileId in selected || tile.state == TileState.REVEALED) {
            return RevealResult.Rejected(RejectReason.ALREADY_SELECTED)
        }
        if (selected.size >= 2) return RevealResult.Rejected(RejectReason.BUSY)

        val revealed = tile.copy(state = TileState.REVEALED)
        tiles[tileId] = revealed
        selected += tileId
        val complete = selected.size == 2
        if (complete) phase = GamePhase.RESOLVING
        return RevealResult.Revealed(revealed.toVisible(), selectionComplete = complete)
    }

    fun evaluateSelection(): SelectionEvaluation? {
        if (phase != GamePhase.RESOLVING || selected.size != 2) return null
        val (a, b) = selected.map { tiles[it] }
        return SelectionEvaluation(
            side = currentSide,
            tileIds = selected.toList(),
            isMatch = a.symbol == b.symbol,
            symbols = listOf(a.symbol, b.symbol)
        )
    }

    /** Captures or hides the two face-up tiles and advances the turn. */
    fun resolve(): ResolveOutcome? {
        val evaluation = evaluateSelection() ?: return null
        val side = evaluation.side
        turnCount++
        lastMatch = evaluation.isMatch

        if (evaluation.isMatch) {
            val capturedState = if (side == Side.PLAYER) TileState.PLAYER_CAPTURED else TileState.CPU_CAPTURED
            evaluation.tileIds.forEach { tiles[it] = tiles[it].copy(state = capturedState) }
            if (side == Side.PLAYER) playerPairs++ else cpuPairs++
            lastCapturedBy = side
        } else {
            evaluation.tileIds.forEach { tiles[it] = tiles[it].copy(state = TileState.HIDDEN) }
            currentSide = side.opponent
        }
        selected.clear()

        val gameOver = pairsRemaining == 0
        phase = if (gameOver) GamePhase.GAME_OVER else turnPhaseFor(currentSide)
        return ResolveOutcome(
            side = side,
            tileIds = evaluation.tileIds,
            isMatch = evaluation.isMatch,
            symbols = evaluation.symbols,
            gameOver = gameOver
        )
    }

    fun result(): GameResult? =
        if (isGameOver) GameResult(playerPairs = playerPairs, cpuPairs = cpuPairs, turns = turnCount) else null

    private fun turnPhaseFor(side: Side) = if (side == Side.PLAYER) GamePhase.PLAYER_TURN else GamePhase.CPU_TURN

    private fun MemoryTile.toVisible() = VisibleTile(
        id = id,
        coordinate = coordinate,
        state = state,
        // Hidden tiles never expose their symbol. Captured pairs were revealed publicly.
        visibleSymbol = if (state == TileState.HIDDEN) null else symbol
    )

    companion object {
        fun newGame(random: Random = Random.Default): MemoryBoardGameEngine =
            MemoryBoardGameEngine(BoardGenerator.generate(random))
    }
}
