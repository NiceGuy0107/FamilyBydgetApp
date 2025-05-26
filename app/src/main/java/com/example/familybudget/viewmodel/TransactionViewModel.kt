package com.example.familybudget.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.model.Transaction
import com.example.familybudget.model.TransactionType
import com.example.familybudget.network.AddTransactionRequest
import com.example.familybudget.network.TransactionApiService
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

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

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

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
                Log.e("TransactionViewModel", "Ошибка парсинга даты: ${e.message}")
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

    fun loadUserTransactions(userId: Long) {
        viewModelScope.launch {
            try {
                val transactions = transactionApiService.getUserTransactions(userId)
                _transactions.value = transactions
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Ошибка загрузки транзакций пользователя: ${e.message}")
            }
        }
    }

    fun addTransaction(
        groupId: String,
        amount: Double,
        type: TransactionType,
        username: String,
        dateTime: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d("TransactionViewModel", "Получена дата: $dateTime")
                
                // Парсим полученную дату и время
                val parsedDateTime = try {
                    LocalDateTime.parse(dateTime, dateTimeFormatter)
                } catch (e: DateTimeParseException) {
                    Log.e("TransactionViewModel", "Ошибка парсинга даты: ${e.message}, дата: $dateTime")
                    onError("Неверный формат даты и времени: $dateTime")
                    return@launch
                }

                Log.d("TransactionViewModel", "Распарсенная дата: $parsedDateTime")

                val request = AddTransactionRequest(
                    groupId = groupId.toLong(),
                    amount = amount,
                    username = username,
                    type = type.name,
                    dateTime = parsedDateTime.format(dateTimeFormatter)
                )

                Log.d("TransactionViewModel", "Отправляем запрос с датой: ${request.dateTime}")

                transactionApiService.addTransaction(request)
                
                // Обновляем список транзакций
                loadGroupTransactions(groupId.toLong())
                
                onSuccess()
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Ошибка добавления транзакции: ${e.message}")
                onError(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}

