package com.example.memoryboardduel.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalView
import com.example.memoryboardduel.ui.FeedbackCue
import kotlinx.coroutines.flow.Flow

/**
 * Plays short synthesized tones and haptics for game cues. Tones come from [ToneGenerator],
 * so no audio assets are needed; if the device refuses a generator the game simply stays silent.
 */
@Composable
fun GameFeedbackEffect(cues: Flow<FeedbackCue>, soundEnabled: Boolean, hapticsEnabled: Boolean) {
    val view = LocalView.current
    val sound by rememberUpdatedState(soundEnabled)
    val haptics by rememberUpdatedState(hapticsEnabled)
    val tones = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 35)
        } catch (_: RuntimeException) {
            null
        }
    }
    DisposableEffect(tones) {
        onDispose { tones?.release() }
    }
    LaunchedEffect(cues) {
        cues.collect { cue ->
            if (haptics) performHaptic(view, cue)
            if (sound) tones?.let { playTone(it, cue) }
        }
    }
}

private fun performHaptic(view: View, cue: FeedbackCue) {
    val constant = when (cue) {
        FeedbackCue.FIRST_REVEAL, FeedbackCue.SECOND_REVEAL, FeedbackCue.MISMATCH -> HapticFeedbackConstants.CLOCK_TICK
        FeedbackCue.MATCH -> HapticFeedbackConstants.VIRTUAL_KEY
        FeedbackCue.VICTORY ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
            else HapticFeedbackConstants.LONG_PRESS
        FeedbackCue.DEFEAT ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
            else HapticFeedbackConstants.LONG_PRESS
        FeedbackCue.CPU_TURN -> return
    }
    view.performHapticFeedback(constant)
}

private fun playTone(tones: ToneGenerator, cue: FeedbackCue) {
    val (tone, duration) = when (cue) {
        FeedbackCue.FIRST_REVEAL, FeedbackCue.SECOND_REVEAL -> ToneGenerator.TONE_PROP_BEEP to 35
        FeedbackCue.MATCH -> ToneGenerator.TONE_PROP_ACK to 140
        FeedbackCue.MISMATCH -> ToneGenerator.TONE_PROP_NACK to 110
        FeedbackCue.CPU_TURN -> ToneGenerator.TONE_PROP_BEEP2 to 60
        FeedbackCue.VICTORY -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 300
        FeedbackCue.DEFEAT -> ToneGenerator.TONE_SUP_ERROR to 250
    }
    try {
        tones.startTone(tone, duration)
    } catch (_: RuntimeException) {
        // Audio focus or hardware issues should never interrupt the game.
    }
}
