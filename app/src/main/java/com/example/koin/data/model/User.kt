package com.example.koin.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CHILD,
    val familyId: String = "",
    val currentStreak: Int = 0,
    val totalBonusPercentage: Double = 0.0,
    val badges: List<String> = emptyList(),
    val optInLeaderboard: Boolean = true,
    val deviceName: String = ""
)
