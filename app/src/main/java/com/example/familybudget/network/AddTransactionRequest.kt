package com.example.familybudget.network

import com.example.familybudget.model.TransactionType

data class AddTransactionRequest(
    val groupId: String,
    val amount: Double,
    val username: String,
    val type: TransactionType,
)