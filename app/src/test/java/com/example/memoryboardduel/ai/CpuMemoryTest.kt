package com.example.memoryboardduel.ai

import com.example.memoryboardduel.boardWith
import com.example.memoryboardduel.capture
import com.example.memoryboardduel.game.ai.CpuMemory
import com.example.memoryboardduel.game.ai.MemoryBoardCpuStrategy
import com.example.memoryboardduel.game.logic.MemoryBoardGameEngine
import com.example.memoryboardduel.game.model.Coordinate
import com.example.memoryboardduel.game.model.Difficulty
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CpuMemoryTest {

    private val a1 = Coordinate.fromLabel("A1")
    private val d5 = Coordinate.fromLabel("D5")
    private val c4 = Coordinate.fromLabel("C4")

    @Test
    fun `remembers revealed tiles`() {
        val memory = CpuMemory()
        memory.remember(a1, SymbolType.STAR, turn = 0)
        assertTrue(memory.isKnown(a1))
        assertEquals(SymbolType.STAR, memory.symbolAt(a1))
        assertEquals(0, memory.knownTiles.single().lastSeenTurn)
    }

    @Test
    fun `finds a known pair only after both tiles were seen`() {
        val memory = CpuMemory()
        memory.remember(a1, SymbolType.STAR, 0)
        memory.remember(c4, SymbolType.MOON, 0)
        assertNull(memory.findKnownPair())
        memory.remember(d5, SymbolType.STAR, 3)
        val pair = memory.findKnownPair()!!
        assertEquals(setOf(a1, d5), setOf(pair.first.coordinate, pair.second.coordinate))
    }

    @Test
    fun `finds the partner of a symbol at another position`() {
        val memory = CpuMemory()
        memory.remember(a1, SymbolType.STAR, 0)
        assertNull(memory.findMatchFor(SymbolType.STAR, excluding = a1))
        assertEquals(a1, memory.findMatchFor(SymbolType.STAR, excluding = d5)?.coordinate)
    }

    @Test
    fun `forget and remove captured`() {
        val memory = CpuMemory()
        memory.remember(a1, SymbolType.STAR, 0)
        memory.remember(d5, SymbolType.STAR, 0)
        memory.remember(c4, SymbolType.MOON, 0)
        memory.forget(c4)
        assertFalse(memory.isKnown(c4))
        memory.removeCaptured(listOf(a1, d5))
        assertEquals(0, memory.size)
    }

    @Test
    fun `sync drops tiles captured by either side`() {
        val board = boardWith("A1" to SymbolType.STAR, "D5" to SymbolType.STAR)
        val engine = MemoryBoardGameEngine(board)
        val memory = CpuMemory()
        memory.remember(a1, SymbolType.STAR, 0)
        engine.capture(board, SymbolType.STAR, Side.PLAYER)
        memory.syncWith(engine.state)
        assertFalse(memory.isKnown(a1))
    }

    @Test
    fun `random forgetting only affects old memories`() {
        val memory = CpuMemory()
        memory.remember(a1, SymbolType.STAR, turn = 0)
        memory.remember(d5, SymbolType.STAR, turn = 9)
        val forgotten = memory.forgetRandomly(currentTurn = 10, minAge = 4, chance = 1.0, random = Random(1))
        assertEquals(1, forgotten)
        assertFalse(memory.isKnown(a1))
        assertTrue(memory.isKnown(d5))
    }

    @Test
    fun `easy CPU forgets over time while hard CPU never forgets`() {
        val board = boardWith()
        val engine = MemoryBoardGameEngine(board)
        val easyMemory = CpuMemory()
        val hardMemory = CpuMemory()
        listOf(easyMemory, hardMemory).forEach { memory ->
            (0 until 10).forEach { id -> memory.remember(engine.state.tile(id).coordinate, board[id].symbol, turn = 0) }
        }
        // Thirty turns later, nothing captured.
        val state = engine.state.copy(turnCount = 30)
        val easy = MemoryBoardCpuStrategy(Difficulty.EASY, Random(7))
        val hard = MemoryBoardCpuStrategy(Difficulty.HARD, Random(7))
        repeat(5) {
            easy.prepareTurn(state, easyMemory)
            hard.prepareTurn(state, hardMemory)
        }
        assertTrue("Easy should have forgotten something", easyMemory.size < 10)
        assertEquals(10, hardMemory.size)
    }
}
