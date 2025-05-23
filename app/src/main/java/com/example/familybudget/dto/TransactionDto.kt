package com.example.familybudget.dto

data class TransactionDto(
    val id: Long,
    val amount: Double,
    val description: String,
    val date: String,
    val type: String,
    val username: String

)
