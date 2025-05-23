package com.example.familybudget.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.familybudget.ui.screens.tabs.HistoryTab
import com.example.familybudget.viewmodel.TransactionViewModel

@Composable
fun HistoryScreen(viewModel: TransactionViewModel, groupId: Long, username: String) {
    val transactions = viewModel.transactions.value

    LaunchedEffect(groupId) {
        viewModel.loadGroupTransactions(groupId)
    }
    HistoryTab(username = username, transactions = transactions)
}
