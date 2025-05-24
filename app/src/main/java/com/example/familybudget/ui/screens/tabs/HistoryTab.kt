package com.example.familybudget.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.preferences.TransactionsPage
import com.example.familybudget.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import com.example.familybudget.ui.components.DatePickerDialog

@Composable
fun HistoryTab(
    username: String,
    transactions: List<TransactionDto>,
    viewModel: TransactionViewModel
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val startDate = viewModel.startDate.value
    val endDate = viewModel.endDate.value
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val incomes = transactions.filter { it.type == "INCOME" }
    val expenses = transactions.filter { it.type == "EXPENSE" }

    LaunchedEffect(transactions) {
        println("incomes.size = ${incomes.size}, expenses.size = ${expenses.size}")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Date range selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("С: ${startDate.format(dateFormatter)}")
            }
            
            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("По: ${endDate.format(dateFormatter)}")
            }
        }

        if (showStartDatePicker) {
            DatePickerDialog(
                onDateSelected = { date ->
                    viewModel.setDateRange(date, endDate)
                },
                onDismiss = { showStartDatePicker = false },
                initialDate = startDate
            )
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                onDateSelected = { date ->
                    viewModel.setDateRange(startDate, date)
                },
                onDismiss = { showEndDatePicker = false },
                initialDate = endDate
            )
        }

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
                0 -> TransactionsPage(
                    transactions = incomes,
                    isIncome = true,
                    startDate = startDate,
                    endDate = endDate
                )
                1 -> TransactionsPage(
                    transactions = expenses,
                    isIncome = false,
                    startDate = startDate,
                    endDate = endDate
                )
            }
        }
    }
}
