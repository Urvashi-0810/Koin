package com.example.koin.data.model

data class PayoutSummary(
    val id: String = "",
    val childId: String = "",
    val childName: String = "",
    val monthYear: String = "", // e.g., "September 2026"
    val baseAllowance: Double = 1000.0,
    val bonusPercentage: Double = 0.0,
    val deductionPercentage: Double = 0.0,
    val netBonusDeductionAmount: Double = 0.0,
    val challengeEarnings: Double = 0.0,
    val taskEarnings: Double = 0.0,
    val finalPayout: Double = 0.0,
    val isConfirmedByParent: Boolean = false,
    val confirmedTimestamp: Long = 0L
)
