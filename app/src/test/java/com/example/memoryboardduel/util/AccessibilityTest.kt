package com.example.memoryboardduel.util

import com.example.memoryboardduel.boardWith
import com.example.memoryboardduel.capture
import com.example.memoryboardduel.game.logic.MemoryBoardGameEngine
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.idOf
import com.example.memoryboardduel.miss
import com.example.memoryboardduel.revealOk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AccessibilityTest {

    private val board = boardWith("A1" to SymbolType.STAR, "D5" to SymbolType.STAR, "B2" to SymbolType.MOON)

    @Test
    fun `tile descriptions follow state`() {
        val engine = MemoryBoardGameEngine(board)
        assertEquals("Hidden tile at A1", tileContentDescription(engine.state.tile(idOf("A1"))))
        engine.revealOk(idOf("A1"), Side.PLAYER)
        assertEquals("Star tile at A1", tileContentDescription(engine.state.tile(idOf("A1"))))
        engine.revealOk(idOf("D5"), Side.PLAYER)
        engine.resolve()
        assertEquals("Player captured tile at A1", tileContentDescription(engine.state.tile(idOf("A1"))))
        engine.miss(board, Side.PLAYER)
        engine.capture(board, SymbolType.MOON, Side.CPU)
        assertEquals("CPU captured tile at B2", tileContentDescription(engine.state.tile(idOf("B2"))))
    }

    @Test
    fun `hidden tile descriptions never contain a symbol name`() {
        val engine = MemoryBoardGameEngine(board)
        engine.state.visibleTiles.forEach { tile ->
            val description = tileContentDescription(tile)
            SymbolType.entries.forEach { symbol ->
                assertFalse("$description leaks $symbol", description.contains(symbol.displayName, ignoreCase = true))
            }
        }
    }

    @Test
    fun `status descriptions`() {
        val engine = MemoryBoardGameEngine(board)
        assertEquals("Your turn", turnDescription(engine.state))
        engine.miss(board, Side.PLAYER)
        assertEquals("CPU turn", turnDescription(engine.state))
        assertEquals("8 pairs remaining", pairsRemainingDescription(8))
        assertEquals("1 pair remaining", pairsRemainingDescription(1))
    }
}
