package com.example.familybudget.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.network.TransactionApiService
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val transactionApiService: TransactionApiService
) : ViewModel() {

    private val _transactions = mutableStateOf<List<TransactionDto>>(emptyList())
    val transactions: State<List<TransactionDto>> = _transactions

    fun loadGroupTransactions(groupId: Long?) {
        viewModelScope.launch {
            try {
                val result = transactionApiService.getTransactions(groupId)
                _transactions.value = result
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Ошибка загрузки транзакций: ${e.message}")
            }
        }
    }
    fun loadUserTransactions(userId: Int) {
        viewModelScope.launch {
            try {
                val result = transactionApiService.getUserTransactions(userId)
                _transactions.value = result
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Ошибка загрузки транзакций пользователя: ${e.message}")
            }
        }
    }
}

