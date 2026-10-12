package com.example.memoryboardduel.game

import com.example.memoryboardduel.boardWith
import com.example.memoryboardduel.capture
import com.example.memoryboardduel.game.logic.MemoryBoardGameEngine
import com.example.memoryboardduel.game.logic.RejectReason
import com.example.memoryboardduel.game.logic.RevealResult
import com.example.memoryboardduel.game.model.GameOutcome
import com.example.memoryboardduel.game.model.GamePhase
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.game.model.TileState
import com.example.memoryboardduel.idOf
import com.example.memoryboardduel.miss
import com.example.memoryboardduel.revealOk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryBoardGameEngineTest {

    private val board = boardWith("A1" to SymbolType.STAR, "D5" to SymbolType.STAR, "B2" to SymbolType.MOON, "C5" to SymbolType.SUN)
    private val engine = MemoryBoardGameEngine(board)
    private val a1 = idOf("A1")
    private val d5 = idOf("D5")
    private val b2 = idOf("B2")
    private val c5 = idOf("C5")

    @Test
    fun `new game starts hidden on the player's turn`() {
        val state = engine.state
        assertEquals(GamePhase.PLAYER_TURN, state.phase)
        assertEquals(Side.PLAYER, state.currentSide)
        assertEquals(18, state.pairsRemaining)
        assertTrue(state.visibleTiles.all { it.state == TileState.HIDDEN && it.visibleSymbol == null })
    }

    @Test
    fun `first selection reveals the tile`() {
        val result = engine.revealOk(a1, Side.PLAYER)
        assertFalse(result.selectionComplete)
        assertEquals(SymbolType.STAR, result.tile.visibleSymbol)
        assertEquals(TileState.REVEALED, engine.state.tile(a1).state)
        assertEquals(listOf(a1), engine.state.selectedTileIds)
        assertEquals(GamePhase.PLAYER_TURN, engine.state.phase)
    }

    @Test
    fun `second selection completes the pair and locks input`() {
        engine.revealOk(a1, Side.PLAYER)
        val result = engine.revealOk(b2, Side.PLAYER)
        assertTrue(result.selectionComplete)
        assertEquals(GamePhase.RESOLVING, engine.state.phase)
        assertEquals(RevealResult.Rejected(RejectReason.BUSY), engine.reveal(c5, Side.PLAYER))
    }

    @Test
    fun `selecting the same tile twice is rejected and keeps it revealed`() {
        engine.revealOk(a1, Side.PLAYER)
        assertEquals(RevealResult.Rejected(RejectReason.ALREADY_SELECTED), engine.reveal(a1, Side.PLAYER))
        assertEquals(listOf(a1), engine.state.selectedTileIds)
        assertEquals(TileState.REVEALED, engine.state.tile(a1).state)
    }

    @Test
    fun `match - player captures and keeps the turn`() {
        engine.revealOk(a1, Side.PLAYER)
        engine.revealOk(d5, Side.PLAYER)
        assertTrue(engine.evaluateSelection()!!.isMatch)
        val outcome = engine.resolve()!!
        assertTrue(outcome.isMatch)
        val state = engine.state
        assertEquals(1, state.playerPairs)
        assertEquals(0, state.cpuPairs)
        assertEquals(17, state.pairsRemaining)
        assertEquals(Side.PLAYER, state.currentSide)
        assertEquals(GamePhase.PLAYER_TURN, state.phase)
        assertEquals(TileState.PLAYER_CAPTURED, state.tile(a1).state)
        assertEquals(TileState.PLAYER_CAPTURED, state.tile(d5).state)
        assertTrue(state.selectedTileIds.isEmpty())
        assertEquals(true, state.lastMatch)
        assertEquals(Side.PLAYER, state.lastCapturedBy)
    }

    @Test
    fun `captured tiles cannot be selected and do not consume the turn`() {
        engine.capture(board, SymbolType.STAR, Side.PLAYER)
        assertEquals(RevealResult.Rejected(RejectReason.CAPTURED), engine.reveal(a1, Side.PLAYER))
        assertEquals(RevealResult.Rejected(RejectReason.CAPTURED), engine.reveal(d5, Side.PLAYER))
        val state = engine.state
        assertEquals(GamePhase.PLAYER_TURN, state.phase)
        assertEquals(Side.PLAYER, state.currentSide)
        assertTrue(state.selectedTileIds.isEmpty())
        assertEquals(1, state.playerPairs)
    }

    @Test
    fun `mismatch hides both tiles and passes the turn to the CPU`() {
        engine.revealOk(a1, Side.PLAYER)
        engine.revealOk(b2, Side.PLAYER)
        assertFalse(engine.evaluateSelection()!!.isMatch)
        val outcome = engine.resolve()!!
        assertFalse(outcome.isMatch)
        val state = engine.state
        assertEquals(TileState.HIDDEN, state.tile(a1).state)
        assertEquals(TileState.HIDDEN, state.tile(b2).state)
        assertNull(state.tile(a1).visibleSymbol)
        assertEquals(Side.CPU, state.currentSide)
        assertEquals(GamePhase.CPU_TURN, state.phase)
        assertEquals(0, state.playerPairs)
        assertEquals(18, state.pairsRemaining)
        assertEquals(false, state.lastMatch)
    }

    @Test
    fun `player cannot act during the CPU turn`() {
        engine.miss(board, Side.PLAYER)
        assertEquals(RevealResult.Rejected(RejectReason.WRONG_TURN), engine.reveal(a1, Side.PLAYER))
    }

    @Test
    fun `CPU match counts for the CPU and the CPU keeps its turn`() {
        engine.miss(board, Side.PLAYER)
        engine.capture(board, SymbolType.STAR, Side.CPU)
        val state = engine.state
        assertEquals(1, state.cpuPairs)
        assertEquals(0, state.playerPairs)
        assertEquals(Side.CPU, state.currentSide)
        assertEquals(GamePhase.CPU_TURN, state.phase)
        assertEquals(TileState.CPU_CAPTURED, state.tile(a1).state)
        assertEquals(Side.CPU, state.lastCapturedBy)
    }

    @Test
    fun `turns alternate after each mismatch`() {
        engine.miss(board, Side.PLAYER)
        assertEquals(Side.CPU, engine.state.currentSide)
        engine.miss(board, Side.CPU)
        assertEquals(Side.PLAYER, engine.state.currentSide)
        assertEquals(2, engine.state.turnCount)
    }

    @Test
    fun `player memory - a previously seen tile shows its symbol again only when selected`() {
        engine.revealOk(a1, Side.PLAYER)
        engine.revealOk(b2, Side.PLAYER)
        engine.resolve()
        // Hidden again: no symbol, no hint.
        assertNull(engine.state.tile(a1).visibleSymbol)
        engine.miss(board, Side.CPU)
        assertNull(engine.state.tile(a1).visibleSymbol)
        // Player recalls A1 later.
        val recalled = engine.revealOk(a1, Side.PLAYER)
        assertEquals(SymbolType.STAR, recalled.tile.visibleSymbol)
        engine.revealOk(d5, Side.PLAYER)
        assertTrue(engine.resolve()!!.isMatch)
    }

    @Test
    fun `final pair ends the game`() {
        SymbolType.entries.dropLast(1).forEach { engine.capture(board, it, Side.PLAYER) }
        assertEquals(1, engine.state.pairsRemaining)
        assertNull(engine.result())
        engine.capture(board, SymbolType.entries.last(), Side.PLAYER)
        assertEquals(0, engine.state.pairsRemaining)
        assertEquals(GamePhase.GAME_OVER, engine.state.phase)
        assertEquals(RevealResult.Rejected(RejectReason.GAME_OVER), engine.reveal(0, Side.PLAYER))
    }

    @Test
    fun `perfect memory when the player captures all 18 pairs`() {
        SymbolType.entries.forEach { engine.capture(board, it, Side.PLAYER) }
        val result = engine.result()!!
        assertEquals(18, result.playerPairs)
        assertEquals(0, result.cpuPairs)
        assertEquals(GameOutcome.WIN, result.outcome)
        assertTrue(result.isPerfectMemory)
        assertEquals(18, result.turns)
    }

    @Test
    fun `player win`() {
        val symbols = SymbolType.entries
        symbols.take(10).forEach { engine.capture(board, it, Side.PLAYER) }
        engine.miss(board, Side.PLAYER)
        symbols.drop(10).forEach { engine.capture(board, it, Side.CPU) }
        val result = engine.result()!!
        assertEquals(10, result.playerPairs)
        assertEquals(8, result.cpuPairs)
        assertEquals(18, result.totalPairs)
        assertEquals(GameOutcome.WIN, result.outcome)
        assertFalse(result.isPerfectMemory)
    }

    @Test
    fun `CPU win`() {
        val symbols = SymbolType.entries
        symbols.take(5).forEach { engine.capture(board, it, Side.PLAYER) }
        engine.miss(board, Side.PLAYER)
        symbols.drop(5).forEach { engine.capture(board, it, Side.CPU) }
        val result = engine.result()!!
        assertEquals(GameOutcome.LOSS, result.outcome)
        assertEquals(13, result.cpuPairs)
    }

    @Test
    fun `draw at nine pairs each`() {
        val symbols = SymbolType.entries
        symbols.take(9).forEach { engine.capture(board, it, Side.PLAYER) }
        engine.miss(board, Side.PLAYER)
        symbols.drop(9).forEach { engine.capture(board, it, Side.CPU) }
        val result = engine.result()!!
        assertEquals(9, result.playerPairs)
        assertEquals(9, result.cpuPairs)
        assertEquals(GameOutcome.DRAW, result.outcome)
    }

    @Test
    fun `resolve without a full selection does nothing`() {
        assertNull(engine.resolve())
        engine.revealOk(a1, Side.PLAYER)
        assertNull(engine.resolve())
        assertEquals(0, engine.state.turnCount)
    }

    @Test
    fun `public state never exposes hidden symbols`() {
        engine.revealOk(a1, Side.PLAYER)
        engine.state.visibleTiles.forEach { tile ->
            if (tile.state == TileState.HIDDEN) assertNull(tile.visibleSymbol)
        }
        assertEquals(SymbolType.STAR, engine.state.tile(a1).visibleSymbol)
    }
}
