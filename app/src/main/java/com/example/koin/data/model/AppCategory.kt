package com.example.koin.data.model

data class AppCategory(
    val packageName: String = "",
    val appName: String = "",
    val categoryType: AppCategoryType = AppCategoryType.NEUTRAL,
    val iconUri: String? = null,
    val rawUsageMinutes: Long = 0,
    val isCustomizedByParent: Boolean = false
) {
    val weightedMinutes: Double
        get() = rawUsageMinutes * categoryType.multiplier
}
