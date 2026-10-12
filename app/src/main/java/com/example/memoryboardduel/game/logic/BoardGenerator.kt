package com.example.memoryboardduel.game.logic

import com.example.memoryboardduel.game.model.BOARD_SIZE
import com.example.memoryboardduel.game.model.Coordinate
import com.example.memoryboardduel.game.model.MemoryTile
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.game.model.TILE_COUNT
import com.example.memoryboardduel.game.model.TileState
import kotlin.random.Random

object BoardGenerator {

    /** Every symbol twice, Fisher–Yates shuffled, laid out row by row. All tiles start hidden. */
    fun generate(random: Random = Random.Default): List<MemoryTile> {
        val symbols = (SymbolType.entries + SymbolType.entries).shuffled(random)
        return fromSymbols(symbols)
    }

    /** Builds a board from an explicit row-major symbol layout. */
    fun fromSymbols(symbols: List<SymbolType>): List<MemoryTile> {
        require(symbols.size == TILE_COUNT) { "A board needs exactly $TILE_COUNT symbols" }
        SymbolType.entries.forEach { symbol ->
            require(symbols.count { it == symbol } == 2) { "$symbol must appear exactly twice" }
        }
        return symbols.mapIndexed { index, symbol ->
            MemoryTile(
                id = index,
                coordinate = Coordinate(row = index / BOARD_SIZE, column = index % BOARD_SIZE),
                symbol = symbol,
                state = TileState.HIDDEN
            )
        }
    }

    fun idOf(coordinate: Coordinate): Int = coordinate.row * BOARD_SIZE + coordinate.column
}
