package com.example.koin.domain

object NudgeManager {

    const val MAX_NUDGES_PER_WINDOW = 4
    const val TWELVE_HOURS_MS = 12 * 60 * 60 * 1000L

    /**
     * Checks if a child can send a nudge based on last nudge timestamp and current count.
     * @return Pair(canSend: Boolean, errorMessage: String?)
     */
    fun canSendNudge(
        currentCount: Int,
        lastNudgeTimestamp: Long,
        nowTimestamp: Long = System.currentTimeMillis()
    ): Pair<Boolean, String?> {
        val timePassed = nowTimestamp - lastNudgeTimestamp

        // Reset count if 12 hours have passed
        val effectiveCount = if (timePassed >= TWELVE_HOURS_MS) 0 else currentCount

        if (effectiveCount >= MAX_NUDGES_PER_WINDOW) {
            val remainingTimeMs = TWELVE_HOURS_MS - timePassed
            val remainingHours = (remainingTimeMs / (1000 * 60 * 60)) + 1
            return Pair(
                false,
                "Maximum 4 nudges reached for this 12-hour window. Please wait ~$remainingHours hrs."
            )
        }

        return Pair(true, null)
    }

    /**
     * Computes updated nudge metadata after sending a nudge.
     * @return Pair(newCount, newTimestamp)
     */
    fun getUpdatedNudgeMetadata(
        currentCount: Int,
        lastNudgeTimestamp: Long,
        nowTimestamp: Long = System.currentTimeMillis()
    ): Pair<Int, Long> {
        val timePassed = nowTimestamp - lastNudgeTimestamp
        val effectiveCount = if (timePassed >= TWELVE_HOURS_MS) 0 else currentCount
        return Pair(effectiveCount + 1, nowTimestamp)
    }
}
