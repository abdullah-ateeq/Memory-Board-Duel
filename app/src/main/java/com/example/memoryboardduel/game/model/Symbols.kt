package com.example.memoryboardduel.game.model

enum class SymbolType(val displayName: String, val pluralName: String) {
    CIRCLE("Circle", "Circles"),
    TRIANGLE("Triangle", "Triangles"),
    SQUARE("Square", "Squares"),
    STAR("Star", "Stars"),
    DIAMOND("Diamond", "Diamonds"),
    HEXAGON("Hexagon", "Hexagons"),
    MOON("Moon", "Moons"),
    BOLT("Bolt", "Bolts"),
    CROSS("Cross", "Crosses"),
    HEART("Heart", "Hearts"),
    FLOWER("Flower", "Flowers"),
    SHIELD("Shield", "Shields"),
    RING("Ring", "Rings"),
    CROWN("Crown", "Crowns"),
    LEAF("Leaf", "Leaves"),
    WAVE("Wave", "Waves"),
    SUN("Sun", "Suns"),
    SPIRAL("Spiral", "Spirals")
}

enum class Side {
    PLAYER,
    CPU;

    val opponent: Side get() = if (this == PLAYER) CPU else PLAYER
}

enum class TileState {
    HIDDEN,
    REVEALED,
    PLAYER_CAPTURED,
    CPU_CAPTURED;

    val isCaptured: Boolean get() = this == PLAYER_CAPTURED || this == CPU_CAPTURED
}

enum class GamePhase {
    PLAYER_TURN,
    CPU_TURN,
    RESOLVING,
    GAME_OVER
}

enum class Difficulty(val label: String) {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard")
}
