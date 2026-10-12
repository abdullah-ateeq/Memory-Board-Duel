package com.example.memoryboardduel.ai

import com.example.memoryboardduel.boardWith
import com.example.memoryboardduel.game.ai.CpuMemory
import com.example.memoryboardduel.game.ai.CpuProfile
import com.example.memoryboardduel.game.ai.MemoryBoardCpuStrategy
import com.example.memoryboardduel.game.logic.BoardGenerator
import com.example.memoryboardduel.game.logic.MemoryBoardGameEngine
import com.example.memoryboardduel.game.logic.ResolveOutcome
import com.example.memoryboardduel.game.logic.RevealResult
import com.example.memoryboardduel.game.model.Coordinate
import com.example.memoryboardduel.game.model.Difficulty
import com.example.memoryboardduel.game.model.GameOutcome
import com.example.memoryboardduel.game.model.GamePhase
import com.example.memoryboardduel.game.model.MemoryBoardGameState
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.game.model.TileState
import com.example.memoryboardduel.idOf
import com.example.memoryboardduel.miss
import com.example.memoryboardduel.revealOk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MemoryBoardCpuStrategyTest {

    private val a1 = idOf("A1")
    private val b2 = idOf("B2")
    private val d5 = idOf("D5")

    /** Runs one full CPU turn the same way the ViewModel does. */
    private fun cpuTurn(
        engine: MemoryBoardGameEngine,
        strategy: MemoryBoardCpuStrategy,
        memory: CpuMemory,
        side: Side = Side.CPU
    ): ResolveOutcome {
        strategy.prepareTurn(engine.state, memory)
        val first = strategy.chooseFirstTile(engine.state, memory)
        val firstTile = engine.revealOk(first, side).tile
        memory.remember(firstTile.coordinate, firstTile.visibleSymbol!!, engine.state.turnCount)
        val second = strategy.chooseSecondTile(engine.state, memory, first)
        val secondTile = engine.revealOk(second, side).tile
        memory.remember(secondTile.coordinate, secondTile.visibleSymbol!!, engine.state.turnCount)
        val outcome = engine.resolve()!!
        if (outcome.isMatch) memory.removeCaptured(outcome.tileIds.map { engine.state.tile(it).coordinate })
        return outcome
    }

    @Test
    fun `CPU cannot cheat - choices are identical for any hidden layout`() {
        // Two boards that look identical from the outside (all hidden) but differ inside.
        val boardA = boardWith("A1" to SymbolType.STAR, "B2" to SymbolType.STAR)
        val boardB = BoardGenerator.generate(Random(99))
        Difficulty.entries.forEach { difficulty ->
            repeat(50) { seed ->
                val stateA = MemoryBoardGameEngine(boardA).state
                val stateB = MemoryBoardGameEngine(boardB).state
                assertEquals(stateA, stateB)
                val choiceA = MemoryBoardCpuStrategy(difficulty, Random(seed)).chooseFirstTile(stateA, CpuMemory())
                val choiceB = MemoryBoardCpuStrategy(difficulty, Random(seed)).chooseFirstTile(stateB, CpuMemory())
                assertEquals(choiceA, choiceB)
            }
        }
    }

    @Test
    fun `CPU cannot intentionally pick an unseen pair`() {
        // A1 and B2 are both STAR but the CPU has never seen either.
        val board = boardWith("A1" to SymbolType.STAR, "B2" to SymbolType.STAR)
        var pickedHiddenPair = 0
        val trials = 2000
        repeat(trials) { seed ->
            val engine = MemoryBoardGameEngine(board, startingSide = Side.CPU)
            val strategy = MemoryBoardCpuStrategy(Difficulty.HARD, Random(seed))
            val memory = CpuMemory()
            val first = strategy.chooseFirstTile(engine.state, memory)
            if (first == a1) {
                engine.revealOk(first, Side.CPU)
                if (strategy.chooseSecondTile(engine.state, memory, first) == b2) pickedHiddenPair++
            }
        }
        // Chance alone: 1/36 * 1/35 ≈ 0.08%. Allow a little noise but nothing like knowledge.
        assertTrue("Picked A1+B2 $pickedHiddenPair times", pickedHiddenPair <= 6)
    }

    @Test
    fun `strategy only sees null symbols for hidden tiles`() {
        val engine = MemoryBoardGameEngine(boardWith(), startingSide = Side.CPU)
        val state: MemoryBoardGameState = engine.state
        assertTrue(state.visibleTiles.all { it.visibleSymbol == null })
    }

    @Test
    fun `CPU uses a known pair from memory`() {
        val board = boardWith("A1" to SymbolType.STAR, "D5" to SymbolType.STAR)
        val engine = MemoryBoardGameEngine(board, startingSide = Side.CPU)
        val memory = CpuMemory().apply {
            remember(Coordinate.fromLabel("A1"), SymbolType.STAR, 0)
            remember(Coordinate.fromLabel("D5"), SymbolType.STAR, 0)
        }
        val strategy = MemoryBoardCpuStrategy(Difficulty.HARD, Random(1))
        val first = strategy.chooseFirstTile(engine.state, memory)
        assertEquals(a1, first)
        engine.revealOk(first, Side.CPU)
        assertEquals(d5, strategy.chooseSecondTile(engine.state, memory, first))
        engine.revealOk(d5, Side.CPU)
        val outcome = engine.resolve()!!
        assertTrue(outcome.isMatch)
        assertEquals(1, engine.state.cpuPairs)
        assertEquals(TileState.CPU_CAPTURED, engine.state.tile(a1).state)
    }

    @Test
    fun `CPU memory - seeing both stars over separate turns leads to a capture`() {
        val board = boardWith("A1" to SymbolType.STAR, "B2" to SymbolType.STAR)
        val engine = MemoryBoardGameEngine(board, startingSide = Side.CPU)
        val memory = CpuMemory()
        // Simulate the CPU legitimately flipping A1 on one turn and B2 on another.
        engine.revealOk(a1, Side.CPU)
        memory.remember(Coordinate.fromLabel("A1"), SymbolType.STAR, engine.state.turnCount)
        val other = engine.state.hiddenTiles.first { board[it.id].symbol != SymbolType.STAR }.id
        engine.revealOk(other, Side.CPU)
        memory.remember(engine.state.tile(other).coordinate, board[other].symbol, engine.state.turnCount)
        engine.resolve()
        engine.miss(board, Side.PLAYER)
        engine.revealOk(b2, Side.CPU)
        memory.remember(Coordinate.fromLabel("B2"), SymbolType.STAR, engine.state.turnCount)
        val filler = engine.state.hiddenTiles.first { board[it.id].symbol != SymbolType.STAR && !memory.isKnown(it.coordinate) }.id
        engine.revealOk(filler, Side.CPU)
        engine.resolve()
        engine.miss(board, Side.PLAYER)

        // Next CPU turn: it should go straight for A1 + B2.
        assertEquals(Side.CPU, engine.state.currentSide)
        val outcome = cpuTurn(engine, MemoryBoardCpuStrategy(Difficulty.HARD, Random(3)), memory)
        assertTrue(outcome.isMatch)
        assertEquals(setOf(a1, b2), outcome.tileIds.toSet())
        assertTrue(memory.knownTiles.none { it.symbol == SymbolType.STAR })
    }

    @Test
    fun `CPU recalls the partner of its first tile`() {
        val board = boardWith("A1" to SymbolType.STAR, "D5" to SymbolType.STAR)
        val engine = MemoryBoardGameEngine(board, startingSide = Side.CPU)
        val memory = CpuMemory().apply { remember(Coordinate.fromLabel("D5"), SymbolType.STAR, 0) }
        val strategy = MemoryBoardCpuStrategy(Difficulty.HARD, Random(1))
        engine.revealOk(a1, Side.CPU)
        assertEquals(d5, strategy.chooseSecondTile(engine.state, memory, a1))
    }

    @Test
    fun `CPU never makes an illegal selection in full games`() {
        Difficulty.entries.forEach { difficulty ->
            repeat(60) { seed ->
                val random = Random(seed)
                val engine = MemoryBoardGameEngine(BoardGenerator.generate(random))
                val sides = mapOf(
                    Side.PLAYER to (MemoryBoardCpuStrategy(difficulty, random) to CpuMemory()),
                    Side.CPU to (MemoryBoardCpuStrategy(difficulty, random) to CpuMemory())
                )
                var guard = 0
                while (!engine.isGameOver) {
                    val side = engine.state.currentSide
                    val (strategy, memory) = sides.getValue(side)
                    strategy.prepareTurn(engine.state, memory)
                    val first = strategy.chooseFirstTile(engine.state, memory)
                    val r1 = engine.reveal(first, side)
                    assertTrue("$difficulty illegal first pick: $r1", r1 is RevealResult.Revealed)
                    val t1 = (r1 as RevealResult.Revealed).tile
                    memory.remember(t1.coordinate, t1.visibleSymbol!!, engine.state.turnCount)
                    val second = strategy.chooseSecondTile(engine.state, memory, first)
                    assertTrue(second != first)
                    val r2 = engine.reveal(second, side)
                    assertTrue("$difficulty illegal second pick: $r2", r2 is RevealResult.Revealed)
                    val t2 = (r2 as RevealResult.Revealed).tile
                    memory.remember(t2.coordinate, t2.visibleSymbol!!, engine.state.turnCount)
                    val outcome = engine.resolve()!!
                    if (outcome.isMatch) {
                        val coords = outcome.tileIds.map { engine.state.tile(it).coordinate }
                        sides.values.forEach { (_, m) -> m.removeCaptured(coords) }
                    }
                    assertTrue("Game did not finish", ++guard < 2000)
                }
                assertEquals(GamePhase.GAME_OVER, engine.state.phase)
                assertEquals(18, engine.state.playerPairs + engine.state.cpuPairs)
            }
        }
    }

    @Test
    fun `hard CPU beats easy CPU far more often than not`() {
        var hardWins = 0
        var easyWins = 0
        repeat(200) { seed ->
            val random = Random(seed)
            val engine = MemoryBoardGameEngine(BoardGenerator.generate(random))
            val easy = MemoryBoardCpuStrategy(Difficulty.EASY, random) to CpuMemory()
            val hard = MemoryBoardCpuStrategy(Difficulty.HARD, random) to CpuMemory()
            while (!engine.isGameOver) {
                val side = engine.state.currentSide
                val (strategy, memory) = if (side == Side.PLAYER) easy else hard
                val outcome = cpuTurnFor(engine, strategy, memory, side)
                if (outcome.isMatch) {
                    val coords = outcome.tileIds.map { engine.state.tile(it).coordinate }
                    easy.second.removeCaptured(coords)
                    hard.second.removeCaptured(coords)
                }
            }
            when (engine.result()!!.outcome) {
                GameOutcome.LOSS -> hardWins++
                GameOutcome.WIN -> easyWins++
                GameOutcome.DRAW -> Unit
            }
        }
        assertTrue("hard=$hardWins easy=$easyWins", hardWins > easyWins * 2)
    }

    @Test
    fun `difficulty profiles are ordered from weak to strong memory`() {
        val easy = CpuProfile.forDifficulty(Difficulty.EASY)
        val normal = CpuProfile.forDifficulty(Difficulty.NORMAL)
        val hard = CpuProfile.forDifficulty(Difficulty.HARD)
        assertTrue(easy.useKnownPairChance < normal.useKnownPairChance)
        assertTrue(normal.useKnownPairChance <= hard.useKnownPairChance)
        assertTrue(easy.forgetChance > normal.forgetChance)
        assertEquals(0.0, hard.forgetChance, 0.0)
        assertEquals(1.0, hard.useKnownPairChance, 0.0)
        assertEquals(1.0, hard.recallMatchChance, 0.0)
    }

    private fun cpuTurnFor(engine: MemoryBoardGameEngine, strategy: MemoryBoardCpuStrategy, memory: CpuMemory, side: Side) =
        cpuTurn(engine, strategy, memory, side)
}
