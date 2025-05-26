package com.example.familybudget.ui.screens.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.familybudget.ui.screens.HomeScreen
import com.example.familybudget.viewmodel.TransactionViewModel
import com.example.familybudget.viewmodel.GroupViewModel
import androidx.navigation.NavController

@Composable
fun HomeTab(
    username: String, 
    userId: Long, 
    viewModel: TransactionViewModel,
    groupViewModel: GroupViewModel,
    navController: NavController
) {
    val transactions = viewModel.transactions.value
    val groups by groupViewModel.groups.collectAsState(initial = emptyList())

    LaunchedEffect(userId) {
        viewModel.loadUserTransactions(userId)
        groupViewModel.loadUserGroups(userId)
    }

    HomeScreen(
        username = username,
        transactions = transactions,
        groups = groups,
        onGroupClick = { group ->
            navController.navigate("group/${group.id}")
        }
    )
}

