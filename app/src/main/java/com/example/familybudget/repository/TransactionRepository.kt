package com.example.familybudget.repository

import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.network.TransactionApiService

class TransactionRepository(private val apiService: TransactionApiService) {
    suspend fun fetchTransactions(groupId: Long): List<TransactionDto> {
        return apiService.getTransactions(groupId)
    }

}
