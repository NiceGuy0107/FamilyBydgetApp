package com.example.familybudget.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberCoroutineScope
import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.preferences.TransactionsPage
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect

@Composable
fun HistoryTab(username: String, transactions: List<TransactionDto>) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val incomes = transactions.filter { it.type == "INCOME" }
    val expenses = transactions.filter { it.type == "EXPENSE" }

    LaunchedEffect(transactions) {
        println("incomes.size = ${incomes.size}, expenses.size = ${expenses.size}")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Доходы") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("Расходы") }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> TransactionsPage(transactions = incomes, isIncome = true)
                1 -> TransactionsPage(transactions = expenses, isIncome = false)
            }
        }
    }
}
