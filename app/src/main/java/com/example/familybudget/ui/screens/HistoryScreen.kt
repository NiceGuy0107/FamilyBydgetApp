package com.example.familybudget.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.familybudget.ui.screens.tabs.HistoryTab
import com.example.familybudget.viewmodel.TransactionViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    viewModel: TransactionViewModel,
    groupId: Long,
    username: String,
    navController: NavController
) {
    val transactions = viewModel.transactions.value
    val coroutineScope = rememberCoroutineScope()

    // Загружаем данные при первом появлении экрана
    LaunchedEffect(Unit) {
        viewModel.loadGroupTransactions(groupId)
    }

    // Периодически обновляем данные каждые 5 секунд
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // 5 секунд
            viewModel.loadGroupTransactions(groupId)
        }
    }
    
    HistoryTab(
        username = username,
        transactions = transactions,
        viewModel = viewModel,
        navController = navController,
        groupId = groupId
    )
}
