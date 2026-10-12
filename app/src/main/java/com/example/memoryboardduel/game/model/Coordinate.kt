package com.example.memoryboardduel.game.model

/**
 * A board position. Rows are labelled 1–6 (top to bottom) and columns A–F (left to right),
 * so `Coordinate(row = 0, column = 0)` is "A1".
 */
data class Coordinate(
    val row: Int,
    val column: Int
) {
    val label: String get() = "${'A' + column}${row + 1}"

    companion object {
        fun fromLabel(label: String): Coordinate {
            require(label.length == 2) { "Invalid coordinate label: $label" }
            return Coordinate(row = label[1].digitToInt() - 1, column = label[0].uppercaseChar() - 'A')
        }
    }
}
