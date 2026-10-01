package com.example.koin.data.model

enum class AppCategoryType(val multiplier: Double, val displayName: String) {
    SOCIAL_MEDIA(2.0, "Social Media"),
    STREAMING(1.5, "Streaming"),
    NEUTRAL(1.0, "Neutral"),
    PRODUCTIVITY(0.5, "Productivity")
}
