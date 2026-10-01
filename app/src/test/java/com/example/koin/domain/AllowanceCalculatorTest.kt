package com.example.koin.domain

import com.example.koin.data.model.Challenge
import com.example.koin.data.model.ChallengeStatus
import com.example.koin.data.model.DailyScore
import com.example.koin.data.model.ScoreTier
import com.example.koin.data.model.Task
import com.example.koin.data.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class AllowanceCalculatorTest {

    @Test
    fun testBestMonth_30ExcellentDays_CappedAt30PercentBonus() {
        val baseAllowance = 1000.0
        val dailyScores = (1..30).map { day ->
            DailyScore(dateString = "2026-09-$day", tier = ScoreTier.EXCELLENT)
        }

        val payout = AllowanceCalculator.calculateMonthlyPayout(
            childId = "c1",
            childName = "Arjun",
            monthYear = "September 2026",
            baseAllowance = baseAllowance,
            dailyScores = dailyScores,
            challenges = emptyList(),
            tasks = emptyList()
        )

        assertEquals(30.0, payout.bonusPercentage, 0.01)
        assertEquals(0.0, payout.deductionPercentage, 0.01)
        assertEquals(1300.0, payout.finalPayout, 0.01)
    }

    @Test
    fun testRoughMonth_CappedAt40PercentDeductionFloor() {
        val baseAllowance = 1000.0
        // 10 poor days = 10 * -10% = -100% uncapped, but should cap at 40% max deduction (60% floor)
        val dailyScores = (1..10).map { day ->
            DailyScore(dateString = "2026-09-$day", tier = ScoreTier.POOR)
        }

        val payout = AllowanceCalculator.calculateMonthlyPayout(
            childId = "c1",
            childName = "Arjun",
            monthYear = "September 2026",
            baseAllowance = baseAllowance,
            dailyScores = dailyScores,
            challenges = emptyList(),
            tasks = emptyList()
        )

        assertEquals(40.0, payout.deductionPercentage, 0.01)
        assertEquals(600.0, payout.finalPayout, 0.01)
    }

    @Test
    fun testWithCompletedChallengesAndTasks() {
        val baseAllowance = 1000.0
        val dailyScores = listOf(
            DailyScore(dateString = "2026-09-01", tier = ScoreTier.EXCELLENT) // +1%
        )
        val challenges = listOf(
            Challenge(
                title = "Science Test",
                rewardAmount = 500.0,
                status = ChallengeStatus.COMPLETED
            )
        )
        val tasks = listOf(
            Task(
                title = "Laundry",
                rewardAmount = 50.0,
                status = TaskStatus.CONFIRMED_BY_PARENT
            )
        )

        val payout = AllowanceCalculator.calculateMonthlyPayout(
            childId = "c1",
            childName = "Arjun",
            monthYear = "September 2026",
            baseAllowance = baseAllowance,
            dailyScores = dailyScores,
            challenges = challenges,
            tasks = tasks
        )

        // Base + 1% bonus = 1010 + 500 challenge + 50 task = 1560
        assertEquals(1560.0, payout.finalPayout, 0.01)
    }
}
