package com.example.memoryboardduel.data

import com.example.memoryboardduel.game.model.GameOutcome
import com.example.memoryboardduel.game.model.GameResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Statistics(
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val bestPairs: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalPairs: Int = 0
) {
    val averagePairs: Double get() = if (gamesPlayed == 0) 0.0 else totalPairs.toDouble() / gamesPlayed

    /** Pure update for one finished game. */
    fun record(result: GameResult): Statistics {
        val streak = when (result.outcome) {
            GameOutcome.WIN -> currentStreak + 1
            GameOutcome.LOSS -> 0
            GameOutcome.DRAW -> currentStreak
        }
        return copy(
            gamesPlayed = gamesPlayed + 1,
            wins = wins + if (result.outcome == GameOutcome.WIN) 1 else 0,
            losses = losses + if (result.outcome == GameOutcome.LOSS) 1 else 0,
            draws = draws + if (result.outcome == GameOutcome.DRAW) 1 else 0,
            bestPairs = maxOf(bestPairs, result.playerPairs),
            currentStreak = streak,
            bestStreak = maxOf(bestStreak, streak),
            totalPairs = totalPairs + result.playerPairs
        )
    }
}

class StatisticsRepository(private val store: KeyValueStore) {

    private val _statistics = MutableStateFlow(load())
    val statistics: StateFlow<Statistics> = _statistics.asStateFlow()

    /** Records a finished game and returns the statistics as they were before it. */
    fun record(result: GameResult): Statistics {
        val previous = _statistics.value
        val updated = previous.record(result)
        save(updated)
        _statistics.value = updated
        return previous
    }

    fun reset() {
        store.remove(ALL_KEYS)
        _statistics.value = Statistics()
    }

    private fun load() = Statistics(
        gamesPlayed = store.getInt(KEY_GAMES, 0),
        wins = store.getInt(KEY_WINS, 0),
        losses = store.getInt(KEY_LOSSES, 0),
        draws = store.getInt(KEY_DRAWS, 0),
        bestPairs = store.getInt(KEY_BEST_PAIRS, 0),
        currentStreak = store.getInt(KEY_STREAK, 0),
        bestStreak = store.getInt(KEY_BEST_STREAK, 0),
        totalPairs = store.getInt(KEY_TOTAL_PAIRS, 0)
    )

    private fun save(stats: Statistics) {
        store.putInt(KEY_GAMES, stats.gamesPlayed)
        store.putInt(KEY_WINS, stats.wins)
        store.putInt(KEY_LOSSES, stats.losses)
        store.putInt(KEY_DRAWS, stats.draws)
        store.putInt(KEY_BEST_PAIRS, stats.bestPairs)
        store.putInt(KEY_STREAK, stats.currentStreak)
        store.putInt(KEY_BEST_STREAK, stats.bestStreak)
        store.putInt(KEY_TOTAL_PAIRS, stats.totalPairs)
    }

    private companion object {
        const val KEY_GAMES = "games_played"
        const val KEY_WINS = "wins"
        const val KEY_LOSSES = "losses"
        const val KEY_DRAWS = "draws"
        const val KEY_BEST_PAIRS = "best_pairs"
        const val KEY_STREAK = "current_streak"
        const val KEY_BEST_STREAK = "best_streak"
        const val KEY_TOTAL_PAIRS = "total_pairs"
        val ALL_KEYS = listOf(
            KEY_GAMES, KEY_WINS, KEY_LOSSES, KEY_DRAWS,
            KEY_BEST_PAIRS, KEY_STREAK, KEY_BEST_STREAK, KEY_TOTAL_PAIRS
        )
    }
}
