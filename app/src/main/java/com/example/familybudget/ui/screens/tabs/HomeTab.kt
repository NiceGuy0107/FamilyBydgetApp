package com.example.familybudget.ui.screens.tabs


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.familybudget.ui.screens.HomeScreen
import com.example.familybudget.viewmodel.TransactionViewModel


@Composable
fun HomeTab(username: String, userId: Int, viewModel: TransactionViewModel) {
    val transactions = viewModel.transactions.value

    LaunchedEffect(userId) {
        viewModel.loadUserTransactions(userId)
    }

    HomeScreen(username = username, transactions = transactions)
}

