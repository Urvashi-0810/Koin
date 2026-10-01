package com.example.koin.data.model

data class Family(
    val id: String = "",
    val familyCode: String = "",
    val parentId: String = "",
    val parentName: String = "",
    val baseAllowance: Double = 1000.0,
    val currencySymbol: String = "₹",
    val childIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
