package com.example.memoryboardduel.util

/**
 * Pacing for reveals and resolution. The reveal hold is a gameplay rule (players need time to
 * memorise), so it stays the same when animations are off; only decorative effects shorten.
 */
data class GameTimings(
    val flipDurationMs: Int,
    val revealHoldMs: Long,
    val matchEffectMs: Long,
    val mismatchEffectMs: Long,
    val cpuThinkMinMs: Long,
    val cpuThinkMaxMs: Long,
    val cpuBetweenRevealsMs: Long
) {
    companion object {
        fun forAnimations(enabled: Boolean): GameTimings =
            if (enabled) {
                GameTimings(
                    flipDurationMs = 350,
                    revealHoldMs = 800,
                    matchEffectMs = 600,
                    mismatchEffectMs = 450,
                    cpuThinkMinMs = 300,
                    cpuThinkMaxMs = 700,
                    cpuBetweenRevealsMs = 550
                )
            } else {
                GameTimings(
                    flipDurationMs = 0,
                    revealHoldMs = 800,
                    matchEffectMs = 150,
                    mismatchEffectMs = 150,
                    cpuThinkMinMs = 300,
                    cpuThinkMaxMs = 700,
                    cpuBetweenRevealsMs = 400
                )
            }
    }
}
