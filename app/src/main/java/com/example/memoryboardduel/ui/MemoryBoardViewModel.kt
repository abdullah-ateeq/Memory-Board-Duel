package com.example.memoryboardduel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.memoryboardduel.data.AppSettings
import com.example.memoryboardduel.data.SettingsRepository
import com.example.memoryboardduel.data.Statistics
import com.example.memoryboardduel.data.StatisticsRepository
import com.example.memoryboardduel.game.ai.CpuMemory
import com.example.memoryboardduel.game.ai.MemoryBoardCpuStrategy
import com.example.memoryboardduel.game.logic.BoardGenerator
import com.example.memoryboardduel.game.logic.MemoryBoardGameEngine
import com.example.memoryboardduel.game.logic.RejectReason
import com.example.memoryboardduel.game.logic.RevealResult
import com.example.memoryboardduel.game.model.GameOutcome
import com.example.memoryboardduel.game.model.GamePhase
import com.example.memoryboardduel.game.model.GameResult
import com.example.memoryboardduel.game.model.MemoryBoardGameState
import com.example.memoryboardduel.game.model.MemoryTile
import com.example.memoryboardduel.game.model.Side
import com.example.memoryboardduel.game.model.SymbolType
import com.example.memoryboardduel.util.GameTimings
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Most recent notable thing that happened on the board. */
sealed interface GameEvent {
    data object Started : GameEvent
    data class PairFound(val side: Side, val symbol: SymbolType) : GameEvent
    data class NoMatch(val side: Side) : GameEvent
}

enum class FeedbackCue {
    FIRST_REVEAL,
    SECOND_REVEAL,
    MATCH,
    MISMATCH,
    CPU_TURN,
    VICTORY,
    DEFEAT
}

data class GameUiState(
    val game: MemoryBoardGameState,
    val lastEvent: GameEvent = GameEvent.Started,
    /** Tiles currently playing the match pulse / connection effect. */
    val matchedTileIds: List<Int> = emptyList(),
    /** Tiles currently playing the mismatch shake. */
    val mismatchedTileIds: List<Int> = emptyList(),
    val cpuThinking: Boolean = false
) {
    val isPlayerInputEnabled: Boolean
        get() = game.phase == GamePhase.PLAYER_TURN && matchedTileIds.isEmpty() && mismatchedTileIds.isEmpty()
}

data class FinishedGame(
    val result: GameResult,
    val bestPairs: Int,
    val isNewBest: Boolean,
    val winStreak: Int
)

class MemoryBoardViewModel(
    private val settingsRepository: SettingsRepository,
    private val statisticsRepository: StatisticsRepository,
    private val boardFactory: () -> List<MemoryTile> = { BoardGenerator.generate() },
    private val random: Random = Random.Default
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
    val statistics: StateFlow<Statistics> = statisticsRepository.statistics

    private var engine = MemoryBoardGameEngine(boardFactory())
    private var cpuMemory = CpuMemory()
    private var cpuStrategy = MemoryBoardCpuStrategy(settings.value.difficulty, random)
    private var turnJob: Job? = null

    private val _uiState = MutableStateFlow(GameUiState(game = engine.state))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _lastResult = MutableStateFlow<FinishedGame?>(null)
    val lastResult: StateFlow<FinishedGame?> = _lastResult.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _feedback = MutableSharedFlow<FeedbackCue>(extraBufferCapacity = 8)
    val feedback: SharedFlow<FeedbackCue> = _feedback.asSharedFlow()

    private val _gameOver = Channel<FinishedGame>(Channel.BUFFERED)
    /** Emits exactly once per finished game; used to navigate to the result screen. */
    val gameOver: Flow<FinishedGame> = _gameOver.receiveAsFlow()

    val timings: GameTimings get() = GameTimings.forAnimations(settings.value.animations)

    /** Discards any running game and deals a completely new board. */
    fun startNewGame() {
        turnJob?.cancel()
        turnJob = null
        engine = MemoryBoardGameEngine(boardFactory())
        cpuMemory = CpuMemory()
        cpuStrategy = MemoryBoardCpuStrategy(settings.value.difficulty, random)
        _uiState.value = GameUiState(game = engine.state)
    }

    /** Stops background CPU work when the player leaves an unfinished duel. */
    fun abandonGame() {
        if (!engine.isGameOver) startNewGame()
    }

    fun selectTile(tileId: Int) {
        if (turnJob?.isActive == true) return
        if (!_uiState.value.isPlayerInputEnabled) return

        when (val result = engine.reveal(tileId, Side.PLAYER)) {
            is RevealResult.Rejected -> {
                if (result.reason == RejectReason.ALREADY_SELECTED) _messages.tryEmit("Choose another tile.")
            }
            is RevealResult.Revealed -> {
                publish()
                if (result.selectionComplete) {
                    _feedback.tryEmit(FeedbackCue.SECOND_REVEAL)
                    turnJob = viewModelScope.launch {
                        resolveSelection()
                        runCpuTurns()
                    }
                } else {
                    _feedback.tryEmit(FeedbackCue.FIRST_REVEAL)
                }
            }
        }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) = settingsRepository.update(transform)

    fun resetSettings() = settingsRepository.reset()

    fun resetStatistics() = statisticsRepository.reset()

    private suspend fun resolveSelection() {
        val timings = timings
        delay(timings.revealHoldMs)
        val evaluation = engine.evaluateSelection() ?: return
        if (evaluation.isMatch) {
            _uiState.update { it.copy(matchedTileIds = evaluation.tileIds) }
            _feedback.tryEmit(FeedbackCue.MATCH)
            delay(timings.matchEffectMs)
        } else {
            _uiState.update { it.copy(mismatchedTileIds = evaluation.tileIds) }
            _feedback.tryEmit(FeedbackCue.MISMATCH)
            delay(timings.mismatchEffectMs)
        }

        val outcome = engine.resolve() ?: return
        if (outcome.isMatch) {
            // Captured tiles are public knowledge; the CPU simply stops tracking them.
            cpuMemory.removeCaptured(outcome.tileIds.map { engine.state.tile(it).coordinate })
        }
        val event = if (outcome.isMatch) GameEvent.PairFound(outcome.side, outcome.symbols.first()) else GameEvent.NoMatch(outcome.side)
        _uiState.update {
            it.copy(
                game = engine.state,
                lastEvent = event,
                matchedTileIds = emptyList(),
                mismatchedTileIds = emptyList(),
                cpuThinking = false
            )
        }
        if (outcome.gameOver) finishGame()
    }

    private suspend fun runCpuTurns() {
        var announced = false
        while (engine.state.phase == GamePhase.CPU_TURN) {
            if (!announced) {
                _feedback.tryEmit(FeedbackCue.CPU_TURN)
                announced = true
            }
            _uiState.update { it.copy(cpuThinking = true) }
            val timings = timings
            cpuStrategy.prepareTurn(engine.state, cpuMemory)
            delay(random.nextLong(timings.cpuThinkMinMs, timings.cpuThinkMaxMs + 1))

            val first = cpuStrategy.chooseFirstTile(engine.state, cpuMemory)
            cpuReveal(first)
            _feedback.tryEmit(FeedbackCue.FIRST_REVEAL)
            delay(timings.cpuBetweenRevealsMs)

            val second = cpuStrategy.chooseSecondTile(engine.state, cpuMemory, first)
            cpuReveal(second)
            _feedback.tryEmit(FeedbackCue.SECOND_REVEAL)
            _uiState.update { it.copy(cpuThinking = false) }

            resolveSelection()
        }
    }

    private fun cpuReveal(tileId: Int) {
        val result = engine.reveal(tileId, Side.CPU)
        check(result is RevealResult.Revealed) { "CPU made an illegal selection: $result" }
        val tile = result.tile
        // The only path by which the CPU learns a symbol: a tile it flipped itself.
        cpuMemory.remember(tile.coordinate, requireNotNull(tile.visibleSymbol), engine.state.turnCount)
        publish()
    }

    private fun finishGame() {
        val result = engine.result() ?: return
        val previous = statisticsRepository.record(result)
        val updated = statisticsRepository.statistics.value
        val finished = FinishedGame(
            result = result,
            bestPairs = updated.bestPairs,
            isNewBest = result.playerPairs > previous.bestPairs,
            winStreak = updated.currentStreak
        )
        _lastResult.value = finished
        when (result.outcome) {
            GameOutcome.WIN -> _feedback.tryEmit(FeedbackCue.VICTORY)
            GameOutcome.LOSS -> _feedback.tryEmit(FeedbackCue.DEFEAT)
            GameOutcome.DRAW -> Unit
        }
        _gameOver.trySend(finished)
    }

    private fun publish() {
        _uiState.update { it.copy(game = engine.state) }
    }

    companion object {
        fun factory(settings: SettingsRepository, statistics: StatisticsRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { MemoryBoardViewModel(settings, statistics) }
            }
    }
}
