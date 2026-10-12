package com.example.memoryboardduel.data

import com.example.memoryboardduel.game.model.Difficulty
import com.example.memoryboardduel.game.model.GameResult
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositoriesTest {

    @Test
    fun `statistics track wins, losses, draws and averages`() {
        val stats = Statistics()
            .record(GameResult(10, 8, 20))
            .record(GameResult(6, 12, 25))
            .record(GameResult(9, 9, 22))
        assertEquals(3, stats.gamesPlayed)
        assertEquals(1, stats.wins)
        assertEquals(1, stats.losses)
        assertEquals(1, stats.draws)
        assertEquals(25, stats.totalPairs)
        assertEquals(25 / 3.0, stats.averagePairs, 0.0001)
    }

    @Test
    fun `best score keeps the highest player pairs`() {
        val stats = Statistics().record(GameResult(12, 6, 20)).record(GameResult(10, 8, 20))
        assertEquals(12, stats.bestPairs)
    }

    @Test
    fun `win streak grows on wins, survives draws, resets on losses`() {
        var stats = Statistics()
        stats = stats.record(GameResult(10, 8, 1)).record(GameResult(11, 7, 1))
        assertEquals(2, stats.currentStreak)
        stats = stats.record(GameResult(9, 9, 1))
        assertEquals(2, stats.currentStreak)
        stats = stats.record(GameResult(10, 8, 1))
        assertEquals(3, stats.currentStreak)
        stats = stats.record(GameResult(4, 14, 1))
        assertEquals(0, stats.currentStreak)
        assertEquals(3, stats.bestStreak)
    }

    @Test
    fun `statistics persist across repository instances and reset`() {
        val store = InMemoryStore()
        StatisticsRepository(store).record(GameResult(10, 8, 20))
        val reloaded = StatisticsRepository(store)
        assertEquals(1, reloaded.statistics.value.wins)
        assertEquals(10, reloaded.statistics.value.bestPairs)
        reloaded.reset()
        assertEquals(Statistics(), StatisticsRepository(store).statistics.value)
    }

    @Test
    fun `settings default values`() {
        val settings = SettingsRepository(InMemoryStore()).settings.value
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(true, settings.dynamicColor)
        assertEquals(Difficulty.NORMAL, settings.difficulty)
        assertEquals(true, settings.sound)
        assertEquals(true, settings.haptics)
        assertEquals(true, settings.animations)
        assertEquals(true, settings.confirmRestart)
        assertEquals(true, settings.showCoordinates)
    }

    @Test
    fun `settings persist across repository instances and reset`() {
        val store = InMemoryStore()
        val changed = AppSettings(
            themeMode = ThemeMode.DARK,
            dynamicColor = false,
            difficulty = Difficulty.HARD,
            sound = false,
            haptics = false,
            animations = false,
            confirmRestart = false,
            showCoordinates = false
        )
        SettingsRepository(store).update { changed }
        assertEquals(changed, SettingsRepository(store).settings.value)
        SettingsRepository(store).reset()
        assertEquals(AppSettings(), SettingsRepository(store).settings.value)
    }
}
