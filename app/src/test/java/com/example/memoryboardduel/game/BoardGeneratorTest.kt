package com.example.memoryboardduel.game

import com.example.memoryboardduel.game.logic.BoardGenerator
import com.example.memoryboardduel.game.model.BOARD_SIZE
import com.example.memoryboardduel.game.model.PAIR_COUNT
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.game.model.TILE_COUNT
import com.example.memoryboardduel.game.model.TileState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BoardGeneratorTest {

    @Test
    fun `board has exactly 36 tiles`() {
        assertEquals(36, TILE_COUNT)
        assertEquals(TILE_COUNT, BoardGenerator.generate(Random(1)).size)
    }

    @Test
    fun `board has exactly 18 pairs of 18 unique symbols`() {
        val board = BoardGenerator.generate(Random(2))
        assertEquals(18, PAIR_COUNT)
        assertEquals(18, SymbolType.entries.size)
        assertEquals(PAIR_COUNT, board.map { it.symbol }.toSet().size)
    }

    @Test
    fun `every symbol appears exactly twice`() {
        val counts = BoardGenerator.generate(Random(3)).groupingBy { it.symbol }.eachCount()
        SymbolType.entries.forEach { assertEquals("$it count", 2, counts[it]) }
    }

    @Test
    fun `coordinates are unique and cover the 6x6 grid`() {
        val board = BoardGenerator.generate(Random(4))
        val coordinates = board.map { it.coordinate }
        assertEquals(TILE_COUNT, coordinates.toSet().size)
        assertTrue(coordinates.all { it.row in 0 until BOARD_SIZE && it.column in 0 until BOARD_SIZE })
        board.forEach { assertEquals(it.id, BoardGenerator.idOf(it.coordinate)) }
    }

    @Test
    fun `all tiles start hidden`() {
        assertTrue(BoardGenerator.generate(Random(5)).all { it.state == TileState.HIDDEN })
    }

    @Test
    fun `shuffle produces different layouts`() {
        val layouts = (0 until 50).map { seed -> BoardGenerator.generate(Random(seed)).map { it.symbol } }.toSet()
        assertTrue("Expected varied layouts, got ${layouts.size}", layouts.size >= 49)
        assertNotEquals(
            BoardGenerator.generate(Random(10)).map { it.symbol },
            BoardGenerator.generate(Random(11)).map { it.symbol }
        )
    }

    @Test
    fun `shuffle does not favor positions`() {
        // Over many boards, each symbol should land in the first cell roughly 2/36 of the time.
        val firstCell = (0 until 3600).map { BoardGenerator.generate(Random(it)).first().symbol }
        val counts = firstCell.groupingBy { it }.eachCount()
        SymbolType.entries.forEach { symbol ->
            val count = counts[symbol] ?: 0
            assertTrue("$symbol appeared $count times in A1", count in 120..290)
        }
    }

    @Test
    fun `1000 random boards are all valid`() {
        val random = Random(2026)
        repeat(1000) {
            val board = BoardGenerator.generate(random)
            assertEquals(36, board.size)
            assertEquals(18, board.map { it.symbol }.toSet().size)
            assertTrue(board.groupingBy { it.symbol }.eachCount().values.all { it == 2 })
            assertEquals(36, board.map { it.coordinate }.toSet().size)
            assertTrue(board.all { it.state == TileState.HIDDEN })
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid layouts are rejected`() {
        BoardGenerator.fromSymbols(List(TILE_COUNT) { SymbolType.STAR })
    }
}
