package com.example.koin.data.model

data class DailyScore(
    val id: String = "",
    val childId: String = "",
    val dateString: String = "", // e.g., "2026-09-30"
    val totalWeightedHours: Double = 0.0,
    val rawHours: Double = 0.0,
    val tier: ScoreTier = ScoreTier.GOOD,
    val bonusOrDeductionPercent: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
