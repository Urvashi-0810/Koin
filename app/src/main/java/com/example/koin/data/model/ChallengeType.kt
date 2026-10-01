package com.example.koin.data.model

enum class ChallengeType(val displayName: String) {
    BEHAVIOR("Behavior"),
    ACADEMIC("Academic"),
    SCREEN_FREE("Screen-Free")
}

enum class ChallengeStatus {
    PENDING_ACCEPTANCE,
    ACTIVE,
    COMPLETED,
    DECLINED
}
