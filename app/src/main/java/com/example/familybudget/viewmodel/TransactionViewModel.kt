package com.example.familybudget.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.network.TransactionApiService
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class TransactionViewModel(
    private val transactionApiService: TransactionApiService
) : ViewModel() {

    private val _transactions = mutableStateOf<List<TransactionDto>>(emptyList())
    val transactions: State<List<TransactionDto>> = _transactions

    private val _startDate = mutableStateOf(LocalDate.now().withDayOfMonth(1))
    val startDate: State<LocalDate> = _startDate

    private val _endDate = mutableStateOf(LocalDate.now())
    val endDate: State<LocalDate> = _endDate

    private var allTransactions = listOf<TransactionDto>()

    fun setDateRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
        filterTransactions()
    }

    private fun filterTransactions() {
        _transactions.value = allTransactions.filter { transaction ->
            try {
                val transactionDate = LocalDateTime.parse(transaction.date).toLocalDate()
                !transactionDate.isBefore(_startDate.value) && !transactionDate.isAfter(_endDate.value)
            } catch (e: Exception) {
                false
            }
        }
    }

    fun loadGroupTransactions(groupId: Long?) {
        viewModelScope.launch {
            try {
                allTransactions = transactionApiService.getTransactions(groupId)
                filterTransactions()
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Ошибка загрузки транзакций: ${e.message}")
            }
        }
    }

    fun loadUserTransactions(userId: Int) {
        viewModelScope.launch {
            try {
                allTransactions = transactionApiService.getUserTransactions(userId)
                filterTransactions()
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Ошибка загрузки транзакций пользователя: ${e.message}")
            }
        }
    }
}

