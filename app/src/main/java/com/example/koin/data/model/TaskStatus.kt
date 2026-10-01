package com.example.koin.data.model

enum class TaskStatus {
    PENDING,
    COMPLETED_BY_CHILD,
    CONFIRMED_BY_PARENT
}

data class Task(
    val id: String = "",
    val familyId: String = "",
    val childId: String = "",
    val title: String = "",
    val rewardAmount: Double = 0.0,
    val isRecurring: Boolean = false,
    val status: TaskStatus = TaskStatus.PENDING,
    val nudgeCount: Int = 0,
    val lastNudgeTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
