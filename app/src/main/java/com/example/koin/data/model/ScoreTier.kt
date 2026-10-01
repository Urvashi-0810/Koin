package com.example.koin.data.model

enum class ScoreTier(
    val displayName: String,
    val bonusPercent: Double,
    val minWeightedHours: Double,
    val maxWeightedHours: Double
) {
    EXCELLENT("Excellent", 1.0, 0.0, 2.0),
    GOOD("Good", 0.0, 2.0, 3.5),
    FAIR("Fair", -5.0, 3.5, 5.0),
    POOR("Poor", -10.0, 5.0, Double.MAX_VALUE);

    companion object {
        fun fromWeightedHours(weightedHours: Double): ScoreTier {
            return when {
                weightedHours <= 2.0 -> EXCELLENT
                weightedHours <= 3.5 -> GOOD
                weightedHours <= 5.0 -> FAIR
                else -> POOR
            }
        }
    }
}
