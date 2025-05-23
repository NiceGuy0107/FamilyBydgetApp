package com.example.familybudget.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familybudget.viewmodel.TransactionViewModel

@Composable
fun AddTransactionScreen(
    username: String,
    groupId: String?,
    transactionViewModel: TransactionViewModel
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Добавление транзакции")
        Text(text = "Пользователь: $username")
        Text(text = "Группа: ${groupId ?: "Нет"}")
    }
}
