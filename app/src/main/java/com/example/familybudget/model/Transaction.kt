package com.example.familybudget.model

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val id: String = "",
    val username: String,
    val amount: Double,
    val description: String,
    val type: TransactionType,
    val groupId: String? = null,
    val date: String = System.currentTimeMillis().toString(),
    val timestamp: Long
)
