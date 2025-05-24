package com.example.familybudget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.familybudget.R

import com.example.familybudget.network.RetrofitInstance
import com.example.familybudget.preferences.ThemePreferences
import com.example.familybudget.ui.screens.tabs.HomeTab
import com.example.familybudget.ui.screens.tabs.GroupTab
import com.example.familybudget.ui.screens.tabs.SettingsTab
import com.example.familybudget.ui.screens.HistoryScreen
import com.example.familybudget.viewmodel.AuthViewModel
import com.example.familybudget.viewmodel.GroupState
import com.example.familybudget.viewmodel.GroupViewModel
import com.example.familybudget.viewmodel.GroupViewModelFactory
import com.example.familybudget.viewmodel.TransactionViewModel
import com.example.familybudget.viewmodel.TransactionViewModelFactory


@Composable
fun MainScreen(
    username: String,
    userId: Int,
    onToggleTheme: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel,
    themePreferences: ThemePreferences
) {
    val navController = rememberNavController()
    val screens = listOf("home", "history", "group", "settings")

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.setCurrentUsername(username)
    }

    val groupViewModel: GroupViewModel = viewModel(
        factory = GroupViewModelFactory(RetrofitInstance.groupApiService)
    )

    LaunchedEffect(userId) {
        groupViewModel.loadGroup(context, userId)
    }

    val screenLabels = mapOf(
        "home" to stringResource(R.string.tab_home),
        "history" to stringResource(R.string.tab_history),
        "group" to stringResource(R.string.tab_group),
        "settings" to stringResource(R.string.tab_settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = {},
                        label = { Text(screenLabels[screen] ?: screen) },
                        selected = navController.currentBackStackEntryAsState().value?.destination?.route == screen,
                        onClick = {
                            navController.navigate(screen) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                val transactionViewModel: TransactionViewModel = viewModel(
                    factory = TransactionViewModelFactory(RetrofitInstance.transactionApiService)
                )
                HomeTab(username = username, userId = userId, viewModel = transactionViewModel)
            }

            composable("history") {
                val transactionViewModel: TransactionViewModel = viewModel(
                    factory = TransactionViewModelFactory(RetrofitInstance.transactionApiService)
                )
                val transactions by transactionViewModel.transactions

                val groupState by groupViewModel.groupState.collectAsState(initial = GroupState.None)

                if (groupState is GroupState.Joined) {
                    val group = (groupState as GroupState.Joined).familyGroup

                    LaunchedEffect(Unit) {
                        transactionViewModel.loadGroupTransactions(group.id.toLong())
                    }

                    HistoryScreen(
                        viewModel = transactionViewModel,
                        groupId = group.id.toLong(),
                        username = username,
                        navController = navController
                    )
                } else {
                    Text("Нет данных о группе")
                }
            }


            composable("group") {
                GroupTab(
                    username = username,
                    userId = userId,
                    navController = navController,
                    groupViewModel = groupViewModel
                )
            }

            composable(
                route = "add_transaction/{username}?groupId={groupId}&type={type}&amount={amount}&dateTime={dateTime}",
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType },
                    navArgument("groupId") { type = NavType.LongType },
                    navArgument("type") { type = NavType.StringType },
                    navArgument("amount") { 
                        type = NavType.StringType
                        nullable = true 
                    },
                    navArgument("dateTime") { 
                        type = NavType.StringType
                        nullable = true 
                    }
                )
            ) { backStackEntry ->
                val usernameArg = backStackEntry.arguments?.getString("username") ?: ""
                val groupId = backStackEntry.arguments?.getLong("groupId")?.toString()
                val type = backStackEntry.arguments?.getString("type")
                val amount = backStackEntry.arguments?.getString("amount")
                val dateTime = backStackEntry.arguments?.getString("dateTime")
                val transactionViewModel = viewModel<TransactionViewModel>(
                    factory = TransactionViewModelFactory(RetrofitInstance.transactionApiService)
                )

                AddTransactionScreen(
                    username = usernameArg,
                    groupId = groupId,
                    transactionType = type,
                    amount = amount,
                    dateTime = dateTime,
                    transactionViewModel = transactionViewModel,
                    navController = navController
                )
            }

            composable("settings") {
                SettingsTab(
                    onToggleTheme = onToggleTheme,
                    onLogout = onLogout,
                    onNavigateToRegister = onNavigateToRegister,
                    authViewModel = authViewModel,
                    themePreferences = themePreferences
                )
            }
        }
    }
}








