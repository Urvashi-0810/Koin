package com.example.koin.domain

import com.example.koin.data.model.AppCategory
import com.example.koin.data.model.AppCategoryType
import com.example.koin.data.model.DailyScore
import com.example.koin.data.model.ScoreTier
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreEngineTest {

    @Test
    fun testWeightedHoursCalculation_SpecExamples() {
        // Child A: 3 hrs Instagram (Social 2.0x) + 1 hr Maps (Neutral 1.0x) = (3 * 2.0) + (1 * 1.0) = 7.0 weighted hrs -> Poor
        val childAApps = listOf(
            AppCategory("com.instagram.android", "Instagram", AppCategoryType.SOCIAL_MEDIA, rawUsageMinutes = 180),
            AppCategory("com.google.android.apps.maps", "Maps", AppCategoryType.NEUTRAL, rawUsageMinutes = 60)
        )
        val childAWeighted = ScoreEngine.calculateWeightedHours(childAApps)
        val childATier = ScoreEngine.determineTier(childAWeighted)

        assertEquals(7.0, childAWeighted, 0.01)
        assertEquals(ScoreTier.POOR, childATier)

        // Child B: 3 hrs Duolingo (Productivity 0.5x) + 1 hr YouTube (Streaming 1.5x) = (3 * 0.5) + (1 * 1.5) = 3.0 weighted hrs -> Good
        val childBApps = listOf(
            AppCategory("com.duolingo", "Duolingo", AppCategoryType.PRODUCTIVITY, rawUsageMinutes = 180),
            AppCategory("com.google.android.youtube", "YouTube", AppCategoryType.STREAMING, rawUsageMinutes = 60)
        )
        val childBWeighted = ScoreEngine.calculateWeightedHours(childBApps)
        val childBTier = ScoreEngine.determineTier(childBWeighted)

        assertEquals(3.0, childBWeighted, 0.01)
        assertEquals(ScoreTier.GOOD, childBTier)
    }

    @Test
    fun testScoreTiers() {
        assertEquals(ScoreTier.EXCELLENT, ScoreEngine.determineTier(1.5))
        assertEquals(ScoreTier.EXCELLENT, ScoreEngine.determineTier(2.0))
        assertEquals(ScoreTier.GOOD, ScoreEngine.determineTier(2.5))
        assertEquals(ScoreTier.GOOD, ScoreEngine.determineTier(3.5))
        assertEquals(ScoreTier.FAIR, ScoreEngine.determineTier(4.0))
        assertEquals(ScoreTier.FAIR, ScoreEngine.determineTier(5.0))
        assertEquals(ScoreTier.POOR, ScoreEngine.determineTier(5.5))
    }

    @Test
    fun testStreakCalculation() {
        val scores = listOf(
            DailyScore(dateString = "2026-09-30", tier = ScoreTier.EXCELLENT),
            DailyScore(dateString = "2026-09-29", tier = ScoreTier.EXCELLENT),
            DailyScore(dateString = "2026-09-28", tier = ScoreTier.EXCELLENT),
            DailyScore(dateString = "2026-09-27", tier = ScoreTier.POOR)
        )
        val streak = ScoreEngine.calculateCurrentStreak(scores)
        assertEquals(3, streak)
    }
}
