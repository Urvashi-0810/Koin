package com.example.koin.data.model

data class Challenge(
    val id: String = "",
    val familyId: String = "",
    val childId: String = "",
    val title: String = "",
    val description: String = "",
    val challengeType: ChallengeType = ChallengeType.BEHAVIOR,
    val rewardAmount: Double = 0.0,
    val rewardPercentageBonus: Double = 0.0,
    val totalDaysOrTarget: Int = 1,
    val completedDays: Int = 0,
    val status: ChallengeStatus = ChallengeStatus.PENDING_ACCEPTANCE,
    val childProofNote: String = "",
    val nudgeCount: Int = 0,
    val lastNudgeTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
