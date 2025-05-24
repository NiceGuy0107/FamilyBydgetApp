package com.example.familybudget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.familybudget.viewmodel.TransactionViewModel
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.familybudget.model.TransactionType
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

@Composable
fun AddTransactionScreen(
    username: String,
    groupId: String?,
    transactionType: String?,
    amount: String?,
    dateTime: String?,
    transactionViewModel: TransactionViewModel,
    navController: NavController
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val isIncome = transactionType == "INCOME"
    val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    LaunchedEffect(Unit) {
        if (amount != null && dateTime != null && groupId != null) {
            isLoading = true
            try {
                // Проверяем и форматируем дату и время
                val parsedDateTime = try {
                    LocalDateTime.parse(dateTime, dateTimeFormatter)
                } catch (e: DateTimeParseException) {
                    throw Exception("Неверный формат даты и времени: $dateTime")
                }

                transactionViewModel.addTransaction(
                    groupId = groupId,
                    amount = amount.toDouble(),
                    type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                    username = username,
                    dateTime = parsedDateTime.format(dateTimeFormatter),
                    onSuccess = {
                        // После успешного добавления обновляем список транзакций
                        transactionViewModel.loadGroupTransactions(groupId.toLong())
                        navController.popBackStack()
                    },
                    onError = { error ->
                        errorMessage = error
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        } else {
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        }
        
        errorMessage?.let { error ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = { navController.popBackStack() }) {
                    Text("Назад")
                }
            }
        }
    }
}
