package com.example.memoryboardduel.ui

import com.example.memoryboardduel.boardWith
import com.example.memoryboardduel.data.InMemoryStore
import com.example.memoryboardduel.data.SettingsRepository
import com.example.memoryboardduel.data.StatisticsRepository
import com.example.memoryboardduel.game.model.GamePhase
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.game.model.TileState
import com.example.memoryboardduel.idOf
import com.example.memoryboardduel.idsOf
import com.example.memoryboardduel.util.GameTimings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryBoardViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val board = boardWith("A1" to SymbolType.STAR, "D5" to SymbolType.STAR, "B2" to SymbolType.MOON)
    private val a1 = idOf("A1")
    private val d5 = idOf("D5")
    private val b2 = idOf("B2")
    private var boardsDealt = 0
    private lateinit var settings: SettingsRepository
    private lateinit var statistics: StatisticsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        settings = SettingsRepository(InMemoryStore())
        statistics = StatisticsRepository(InMemoryStore())
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = MemoryBoardViewModel(
        settingsRepository = settings,
        statisticsRepository = statistics,
        boardFactory = { boardsDealt++; board },
        random = Random(42)
    )

    private fun TestScope.capture(vm: MemoryBoardViewModel, symbol: SymbolType) {
        val (x, y) = board.idsOf(symbol)
        vm.selectTile(x)
        vm.selectTile(y)
        advanceUntilIdle()
    }

    @Test
    fun `player match is captured after the reveal pause and the player keeps the turn`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.selectTile(a1)
        vm.selectTile(d5)
        assertEquals(GamePhase.RESOLVING, vm.uiState.value.game.phase)
        advanceUntilIdle()
        val game = vm.uiState.value.game
        assertEquals(1, game.playerPairs)
        assertEquals(TileState.PLAYER_CAPTURED, game.tile(a1).state)
        assertEquals(GamePhase.PLAYER_TURN, game.phase)
        assertEquals(GameEvent.PairFound(Side.PLAYER, SymbolType.STAR), vm.uiState.value.lastEvent)
    }

    @Test
    fun `mismatch hands the turn to the CPU which then plays and returns it`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.selectTile(a1)
        vm.selectTile(b2)
        advanceTimeBy(GameTimings.forAnimations(true).revealHoldMs + 700)
        runCurrent()
        val afterMiss = vm.uiState.value.game
        assertEquals(TileState.HIDDEN, afterMiss.tile(a1).state)
        assertNull(afterMiss.tile(a1).visibleSymbol)
        assertEquals(Side.CPU, afterMiss.currentSide)
        advanceUntilIdle()
        val state = vm.uiState.value.game
        assertTrue(state.phase == GamePhase.PLAYER_TURN || state.phase == GamePhase.GAME_OVER)
        assertTrue(state.turnCount >= 2)
    }

    @Test
    fun `rapid taps cannot select more than two tiles or act during resolution`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.selectTile(a1)
        vm.selectTile(a1) // same tile: rejected
        vm.selectTile(b2)
        vm.selectTile(d5) // third tile: ignored
        val game = vm.uiState.value.game
        assertEquals(listOf(a1, b2), game.selectedTileIds)
        assertEquals(TileState.HIDDEN, game.tile(d5).state)
        // Tapping during the CPU's turn is ignored as well.
        advanceTimeBy(1600)
        runCurrent()
        val hiddenBefore = vm.uiState.value.game.hiddenTiles.size
        vm.selectTile(d5)
        assertEquals(hiddenBefore, vm.uiState.value.game.hiddenTiles.size)
    }

    @Test
    fun `same tile tap shows a message`() = runTest(dispatcher) {
        val vm = viewModel()
        val messages = mutableListOf<String>()
        val job = backgroundScope.launch { vm.messages.collect { messages += it } }
        runCurrent()
        vm.selectTile(a1)
        vm.selectTile(a1)
        runCurrent()
        assertEquals(listOf("Choose another tile."), messages)
        job.cancel()
    }

    @Test
    fun `captured tile tap is ignored without consuming the turn`() = runTest(dispatcher) {
        val vm = viewModel()
        capture(vm, SymbolType.STAR)
        vm.selectTile(a1)
        val game = vm.uiState.value.game
        assertTrue(game.selectedTileIds.isEmpty())
        assertEquals(GamePhase.PLAYER_TURN, game.phase)
    }

    @Test
    fun `animations disabled resolves with shortened effects but the same reveal pause`() = runTest(dispatcher) {
        settings.update { it.copy(animations = false) }
        val vm = viewModel()
        val timings = vm.timings
        assertEquals(0, timings.flipDurationMs)
        assertEquals(GameTimings.forAnimations(true).revealHoldMs, timings.revealHoldMs)
        vm.selectTile(a1)
        vm.selectTile(d5)
        advanceTimeBy(timings.revealHoldMs + timings.matchEffectMs + 1)
        runCurrent()
        assertEquals(1, vm.uiState.value.game.playerPairs)
    }

    @Test
    fun `finishing a perfect game records statistics and emits game over once`() = runTest(dispatcher) {
        val vm = viewModel()
        SymbolType.entries.forEach { capture(vm, it) }
        val finished = vm.gameOver.first()
        assertTrue(finished.result.isPerfectMemory)
        assertEquals(18, finished.bestPairs)
        assertTrue(finished.isNewBest)
        assertEquals(1, finished.winStreak)
        assertNotNull(vm.lastResult.value)
        val stats = statistics.statistics.value
        assertEquals(1, stats.gamesPlayed)
        assertEquals(1, stats.wins)
        assertEquals(18, stats.bestPairs)
        assertEquals(1, stats.bestStreak)
        assertEquals(GamePhase.GAME_OVER, vm.uiState.value.game.phase)
        // Further taps do nothing and do not record twice.
        vm.selectTile(0)
        advanceUntilIdle()
        assertEquals(1, statistics.statistics.value.gamesPlayed)
    }

    @Test
    fun `restart deals a new board and resets the duel`() = runTest(dispatcher) {
        val vm = viewModel()
        capture(vm, SymbolType.STAR)
        vm.selectTile(b2)
        vm.startNewGame()
        val game = vm.uiState.value.game
        assertEquals(2, boardsDealt)
        assertEquals(0, game.playerPairs)
        assertEquals(0, game.turnCount)
        assertEquals(18, game.pairsRemaining)
        assertEquals(GamePhase.PLAYER_TURN, game.phase)
        assertTrue(game.visibleTiles.all { it.state == TileState.HIDDEN && it.visibleSymbol == null })
        advanceUntilIdle()
        assertEquals(0, statistics.statistics.value.gamesPlayed)
    }

    @Test
    fun `restart during CPU turn cancels the CPU`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.selectTile(a1)
        vm.selectTile(b2)
        advanceTimeBy(1500)
        runCurrent()
        vm.startNewGame()
        advanceUntilIdle()
        val game = vm.uiState.value.game
        assertEquals(0, game.turnCount)
        assertEquals(GamePhase.PLAYER_TURN, game.phase)
    }
}
