package com.example.memoryboardduel

import com.example.memoryboardduel.game.logic.BoardGenerator
import com.example.memoryboardduel.game.logic.MemoryBoardGameEngine
import com.example.memoryboardduel.game.logic.RevealResult
import com.example.memoryboardduel.game.model.Coordinate
import com.example.memoryboardduel.game.model.MemoryTile
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.game.model.TILE_COUNT
import org.junit.Assert.assertTrue

/** Builds a valid board with the given placements; remaining cells are filled with the leftover symbols. */
fun boardWith(vararg placements: Pair<String, SymbolType>): List<MemoryTile> {
    val symbols = arrayOfNulls<SymbolType>(TILE_COUNT)
    placements.forEach { (label, symbol) -> symbols[idOf(label)] = symbol }
    val pool = SymbolType.entries.flatMap { symbol ->
        List(2 - symbols.count { it == symbol }) { symbol }
    }.toMutableList()
    for (i in symbols.indices) {
        if (symbols[i] == null) symbols[i] = pool.removeAt(0)
    }
    return BoardGenerator.fromSymbols(symbols.map { it!! })
}

fun idOf(label: String): Int = BoardGenerator.idOf(Coordinate.fromLabel(label))

fun List<MemoryTile>.idsOf(symbol: SymbolType): List<Int> = filter { it.symbol == symbol }.map { it.id }

fun MemoryBoardGameEngine.revealOk(tileId: Int, side: Side): RevealResult.Revealed {
    val result = reveal(tileId, side)
    assertTrue("Expected tile $tileId to be revealed but got $result", result is RevealResult.Revealed)
    return result as RevealResult.Revealed
}

/** Has [side] flip both tiles of [symbol] and resolves the capture. */
fun MemoryBoardGameEngine.capture(board: List<MemoryTile>, symbol: SymbolType, side: Side) {
    val (a, b) = board.idsOf(symbol)
    revealOk(a, side)
    revealOk(b, side)
    resolve()
}

/** Has [side] flip two non-matching hidden tiles and resolves the miss. */
fun MemoryBoardGameEngine.miss(board: List<MemoryTile>, side: Side) {
    val hidden = state.hiddenTiles.map { it.id }
    val first = hidden.first()
    val second = hidden.first { board[it].symbol != board[first].symbol }
    revealOk(first, side)
    revealOk(second, side)
    resolve()
}
