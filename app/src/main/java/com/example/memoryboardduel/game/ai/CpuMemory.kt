package com.example.memoryboardduel.game.ai

import com.example.memoryboardduel.game.model.Coordinate
import com.example.memoryboardduel.game.model.MemoryBoardGameState
import com.example.memoryboardduel.game.model.SymbolType
import kotlin.random.Random

data class KnownTile(
    val coordinate: Coordinate,
    val symbol: SymbolType,
    val lastSeenTurn: Int
)

/**
 * What the CPU has personally seen. Entries are only added through [remember], which the
 * game calls with tiles the CPU itself just flipped face up.
 */
class CpuMemory {
    private val known = linkedMapOf<Coordinate, KnownTile>()

    val knownTiles: List<KnownTile> get() = known.values.toList()

    val size: Int get() = known.size

    fun isKnown(coordinate: Coordinate): Boolean = coordinate in known

    fun symbolAt(coordinate: Coordinate): SymbolType? = known[coordinate]?.symbol

    fun remember(coordinate: Coordinate, symbol: SymbolType, turn: Int) {
        known[coordinate] = KnownTile(coordinate, symbol, turn)
    }

    fun forget(coordinate: Coordinate) {
        known.remove(coordinate)
    }

    /**
     * Drops each memory that is at least [minAge] turns old with probability [chance].
     * Returns the number of forgotten tiles.
     */
    fun forgetRandomly(currentTurn: Int, minAge: Int, chance: Double, random: Random): Int {
        if (chance <= 0.0) return 0
        val forgotten = known.values
            .filter { currentTurn - it.lastSeenTurn >= minAge && random.nextDouble() < chance }
            .map { it.coordinate }
        forgotten.forEach { known.remove(it) }
        return forgotten.size
    }

    /** Two remembered coordinates with the same symbol, if any. */
    fun findKnownPair(): Pair<KnownTile, KnownTile>? =
        known.values
            .groupBy { it.symbol }
            .values
            .firstOrNull { it.size >= 2 }
            ?.let { it[0] to it[1] }

    /** A remembered tile with [symbol] at a position other than [excluding]. */
    fun findMatchFor(symbol: SymbolType, excluding: Coordinate): KnownTile? =
        known.values.firstOrNull { it.symbol == symbol && it.coordinate != excluding }

    fun removeCaptured(coordinates: Collection<Coordinate>) {
        coordinates.forEach { known.remove(it) }
    }

    /** Removes anything that is no longer on the board as a hidden or face-up tile. */
    fun syncWith(state: MemoryBoardGameState) {
        val captured = state.visibleTiles.filter { it.state.isCaptured }.map { it.coordinate }
        removeCaptured(captured)
    }

    fun clear() = known.clear()
}
