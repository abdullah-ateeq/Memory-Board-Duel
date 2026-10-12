package com.example.memoryboardduel.game.ai

import com.example.memoryboardduel.game.model.Difficulty
import com.example.memoryboardduel.game.model.MemoryBoardGameState
import com.example.memoryboardduel.game.model.TileState
import com.example.memoryboardduel.game.model.VisibleTile
import kotlin.random.Random

/** Tuning knobs for one difficulty level. */
data class CpuProfile(
    /** Chance to go for a pair it fully remembers at the start of a turn. */
    val useKnownPairChance: Double,
    /** Chance to recall the partner of the first flipped tile. */
    val recallMatchChance: Double,
    /** Chance to flip any hidden tile instead of a fresh, unexplored one. */
    val carelessChance: Double,
    /** Memories at least this many turns old may fade. */
    val forgetAfterTurns: Int,
    /** Per-turn chance for each old memory to fade. */
    val forgetChance: Double
) {
    companion object {
        fun forDifficulty(difficulty: Difficulty): CpuProfile = when (difficulty) {
            Difficulty.EASY -> CpuProfile(
                useKnownPairChance = 0.35,
                recallMatchChance = 0.4,
                carelessChance = 0.5,
                forgetAfterTurns = 4,
                forgetChance = 0.35
            )
            Difficulty.NORMAL -> CpuProfile(
                useKnownPairChance = 0.85,
                recallMatchChance = 0.9,
                carelessChance = 0.1,
                forgetAfterTurns = 10,
                forgetChance = 0.08
            )
            Difficulty.HARD -> CpuProfile(
                useKnownPairChance = 1.0,
                recallMatchChance = 1.0,
                carelessChance = 0.0,
                forgetAfterTurns = Int.MAX_VALUE,
                forgetChance = 0.0
            )
        }
    }
}

/**
 * Chooses CPU tiles using only the public [MemoryBoardGameState] (where hidden symbols are null)
 * and the CPU's own [CpuMemory]. It has no access to the internal board.
 */
class MemoryBoardCpuStrategy(
    val difficulty: Difficulty,
    private val random: Random = Random.Default
) {
    private val profile = CpuProfile.forDifficulty(difficulty)

    /** Called at the start of each CPU turn: drops captured tiles and lets weak memories fade. */
    fun prepareTurn(state: MemoryBoardGameState, memory: CpuMemory) {
        memory.syncWith(state)
        memory.forgetRandomly(
            currentTurn = state.turnCount,
            minAge = profile.forgetAfterTurns,
            chance = profile.forgetChance,
            random = random
        )
    }

    fun chooseFirstTile(state: MemoryBoardGameState, memory: CpuMemory): Int {
        val hidden = state.hiddenTiles
        check(hidden.isNotEmpty()) { "No hidden tiles left to choose" }

        val knownPair = memory.findKnownPair()
        if (knownPair != null && random.nextDouble() < profile.useKnownPairChance) {
            val first = hidden.firstOrNull { it.coordinate == knownPair.first.coordinate }
            val second = hidden.firstOrNull { it.coordinate == knownPair.second.coordinate }
            if (first != null && second != null) return first.id
        }
        return explore(hidden, memory)
    }

    fun chooseSecondTile(state: MemoryBoardGameState, memory: CpuMemory, firstTileId: Int): Int {
        val first = state.tile(firstTileId)
        check(first.state == TileState.REVEALED) { "First tile must be face up before choosing the second" }
        val candidates = state.hiddenTiles.filter { it.id != firstTileId }
        check(candidates.isNotEmpty()) { "No tile left to pair with" }

        val symbol = first.visibleSymbol
        if (symbol != null && random.nextDouble() < profile.recallMatchChance) {
            val remembered = memory.findMatchFor(symbol, excluding = first.coordinate)
            val partner = remembered?.let { known -> candidates.firstOrNull { it.coordinate == known.coordinate } }
            if (partner != null) return partner.id
        }
        return explore(candidates, memory)
    }

    /** Prefer tiles it has never seen; careless play may pick any hidden tile. */
    private fun explore(candidates: List<VisibleTile>, memory: CpuMemory): Int {
        val unexplored = candidates.filterNot { memory.isKnown(it.coordinate) }
        val pool = if (unexplored.isEmpty() || random.nextDouble() < profile.carelessChance) candidates else unexplored
        return pool.random(random).id
    }
}
