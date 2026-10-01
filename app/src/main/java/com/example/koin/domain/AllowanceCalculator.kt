package com.example.koin.domain

import com.example.koin.data.model.Challenge
import com.example.koin.data.model.ChallengeStatus
import com.example.koin.data.model.DailyScore
import com.example.koin.data.model.PayoutSummary
import com.example.koin.data.model.ScoreTier
import com.example.koin.data.model.Task
import com.example.koin.data.model.TaskStatus
import kotlin.math.min

object AllowanceCalculator {

    const val MAX_DAILY_BONUS_PERCENT = 30.0
    const val MAX_DEDUCTION_PERCENT = 40.0 // Max deduction cap = 40% (floor 60%)

    /**
     * Calculates the payout summary for a given child for the month.
     */
    fun calculateMonthlyPayout(
        childId: String,
        childName: String,
        monthYear: String,
        baseAllowance: Double,
        dailyScores: List<DailyScore>,
        challenges: List<Challenge>,
        tasks: List<Task>
    ): PayoutSummary {
        // 1. Calculate Daily Score Bonuses and Deductions
        var totalBonusPercent = 0.0
        var totalDeductionPercent = 0.0

        for (score in dailyScores) {
            when (score.tier) {
                ScoreTier.EXCELLENT -> {
                    totalBonusPercent += 1.0
                }
                ScoreTier.FAIR -> {
                    totalDeductionPercent += 5.0
                }
                ScoreTier.POOR -> {
                    totalDeductionPercent += 10.0
                }
                ScoreTier.GOOD -> {
                    // No bonus or deduction
                }
            }
        }

        // Cap daily bonus percent at 30%
        val cappedBonusPercent = min(totalBonusPercent, MAX_DAILY_BONUS_PERCENT)

        // Cap deduction percent at 40%
        val cappedDeductionPercent = min(totalDeductionPercent, MAX_DEDUCTION_PERCENT)

        // Base allowance adjustment
        val bonusDeductionMultiplier = (cappedBonusPercent - cappedDeductionPercent) / 100.0
        val baseAdjustmentAmount = baseAllowance * bonusDeductionMultiplier

        // 2. Challenge Earnings (Additive cash rewards + percentage bonus)
        var totalChallengeCash = 0.0
        var totalChallengeBonusPercent = 0.0

        for (challenge in challenges) {
            if (challenge.status == ChallengeStatus.COMPLETED) {
                totalChallengeCash += challenge.rewardAmount
                totalChallengeBonusPercent += challenge.rewardPercentageBonus
            }
        }

        val challengeBonusAmount = baseAllowance * (totalChallengeBonusPercent / 100.0)
        val totalChallengeEarnings = totalChallengeCash + challengeBonusAmount

        // 3. Task Earnings (Additive cash)
        var totalTaskEarnings = 0.0
        for (task in tasks) {
            if (task.status == TaskStatus.CONFIRMED_BY_PARENT) {
                totalTaskEarnings += task.rewardAmount
            }
        }

        // 4. Final Payout Calculation
        val adjustedBase = baseAllowance + baseAdjustmentAmount
        val finalPayoutAmount = Math.max(0.0, adjustedBase + totalChallengeEarnings + totalTaskEarnings)

        return PayoutSummary(
            id = "${childId}_$monthYear",
            childId = childId,
            childName = childName,
            monthYear = monthYear,
            baseAllowance = baseAllowance,
            bonusPercentage = cappedBonusPercent,
            deductionPercentage = cappedDeductionPercent,
            netBonusDeductionAmount = baseAdjustmentAmount,
            challengeEarnings = totalChallengeEarnings,
            taskEarnings = totalTaskEarnings,
            finalPayout = Math.round(finalPayoutAmount * 100.0) / 100.0,
            isConfirmedByParent = false,
            confirmedTimestamp = 0L
        )
    }
}
