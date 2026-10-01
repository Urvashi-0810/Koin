package com.example.koin.data.model

data class ParentGesture(
    val id: String = "",
    val childId: String = "",
    val parentId: String = "",
    val message: String = "Well done!",
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false
)
