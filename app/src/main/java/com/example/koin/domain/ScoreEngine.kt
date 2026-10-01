package com.example.koin.domain

import com.example.koin.data.model.AppCategory
import com.example.koin.data.model.DailyScore
import com.example.koin.data.model.ScoreTier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScoreEngine {

    /**
     * Calculates total weighted hours from a list of app category usage entries.
     */
    fun calculateWeightedHours(apps: List<AppCategory>): Double {
        val totalWeightedMinutes = apps.sumOf { app ->
            app.rawUsageMinutes * app.categoryType.multiplier
        }
        return totalWeightedMinutes / 60.0
    }

    /**
     * Calculates total raw hours from a list of app usage entries.
     */
    fun calculateRawHours(apps: List<AppCategory>): Double {
        val totalRawMinutes = apps.sumOf { it.rawUsageMinutes }
        return totalRawMinutes / 60.0
    }

    /**
     * Determines the score tier from weighted hours.
     */
    fun determineTier(weightedHours: Double): ScoreTier {
        return ScoreTier.fromWeightedHours(weightedHours)
    }

    /**
     * Computes consecutive streak of EXCELLENT days ending on or near today.
     */
    fun calculateCurrentStreak(dailyScores: List<DailyScore>): Int {
        val sortedScores = dailyScores.sortedByDescending { it.dateString }
        var streak = 0
        for (score in sortedScores) {
            if (score.tier == ScoreTier.EXCELLENT) {
                streak++
            } else if (score.tier == ScoreTier.GOOD) {
                // Good days do not break the streak or add to it, or break streak?
                // Spec says: "When a child hits 7 consecutive Excellent days..."
                // So excellent days build the streak; Poor/Fair breaks it.
                continue
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Builds a DailyScore instance given today's app usages and childId.
     */
    fun createDailyScore(
        childId: String,
        apps: List<AppCategory>,
        dateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    ): DailyScore {
        val weightedHours = calculateWeightedHours(apps)
        val rawHours = calculateRawHours(apps)
        val tier = determineTier(weightedHours)

        return DailyScore(
            id = "${childId}_$dateString",
            childId = childId,
            dateString = dateString,
            totalWeightedHours = Math.round(weightedHours * 100.0) / 100.0,
            rawHours = Math.round(rawHours * 100.0) / 100.0,
            tier = tier,
            bonusOrDeductionPercent = tier.bonusPercent,
            timestamp = System.currentTimeMillis()
        )
    }
}
