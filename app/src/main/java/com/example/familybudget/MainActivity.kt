package com.example.familybudget

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.familybudget.ui.screens.*
import com.example.familybudget.ui.theme.FamilyBudgetTheme
import com.example.familybudget.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.familybudget.network.GroupApiService
import com.example.familybudget.preferences.ThemePreferences
import com.example.familybudget.viewmodel.GroupViewModel
import com.example.familybudget.viewmodel.GroupViewModelFactory
import kotlinx.coroutines.launch
import com.jakewharton.threetenabp.AndroidThreeTen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val userId = sharedPreferences.getInt("userId", -1).takeIf { it != -1 } // -1 значит не сохранен

        setContent {
            AppContent(
                initialUsername = username,
                initialUserId = userId,
                sharedPreferences = sharedPreferences
            )
        }
    }
}

@Composable
fun AppContent(
    initialUsername: String?,
    initialUserId: Int?,  // добавляем userId
    sharedPreferences: SharedPreferences
) {
    val context = LocalContext.current
    val langCode = getSavedLanguage(context)
    val localizedContext = remember(langCode) { updateLocale(context, langCode) }

    val themePreferences = remember { ThemePreferences(localizedContext) }
    val coroutineScope = rememberCoroutineScope()

    var usernameState by remember { mutableStateOf(initialUsername) }
    var userIdState by remember { mutableStateOf(initialUserId) }  // состояние userId

    var currentTheme by remember { mutableStateOf("light") }

    // загружаем тему при входе
    LaunchedEffect(usernameState) {
        usernameState?.let { username ->
            currentTheme = themePreferences.getTheme(username)
        }
    }

    val isDarkTheme = currentTheme == "dark"
    val isUserLoggedIn = usernameState != null && userIdState != null

    FamilyBudgetTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
        MyApp(
                isLoggedIn = isUserLoggedIn,
                onToggleTheme = { themeKey ->
                    usernameState?.let {
                        currentTheme = themeKey
                        coroutineScope.launch {
                            themePreferences.saveTheme(it, themeKey)
                        }
                    }
                },
                onLogout = {
                    sharedPreferences.edit().clear().apply()
                    usernameState = null
                    userIdState = null
                    currentTheme = "light"
                },
                onLoginSuccess = { loggedInUsername, loggedInUserId ->
                    usernameState = loggedInUsername
                    userIdState = loggedInUserId
                },
                themePreferences = themePreferences,
                userId = userIdState  // передаем userId в MyApp, если нужно
            )
        }
    }
}

@Composable
fun MyApp(
    onToggleTheme: (String) -> Unit,
    onLogout: () -> Unit = {},
    onLoginSuccess: (String, Int) -> Unit = { _, _ -> },
    isLoggedIn: Boolean = false,
    themePreferences: ThemePreferences,
    userId: Int? = null  // добавляем userId
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn && userId != null) "main/$userId/${sharedPrefs.getString("username", "")}" else "login"
    ) {
        composable("login") {
            val authViewModel: AuthViewModel = viewModel()

            val apiService = remember { GroupApiService.create() }
            val factory = remember { GroupViewModelFactory(apiService) }
            val groupViewModel: GroupViewModel = viewModel(factory = factory)

            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { username, userId ->
                    sharedPrefs.edit()
                        .putString("username", username)
                        .putInt("userId", userId)
                        .apply()
                    onLoginSuccess(username, userId)
                    navController.navigate("main/$userId/$username") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("register") {
            val authViewModel: AuthViewModel = viewModel()
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate("login") },
                onRegisterSuccess = { username, userId ->
                    sharedPrefs.edit()
                        .putString("username", username)
                        .putInt("userId", userId)
                        .apply()
                    onLoginSuccess(username, userId)
                    navController.navigate("main/$userId/$username") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        composable(
            "main/{userId}/{username}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            val username = backStackEntry.arguments?.getString("username") ?: ""

            if (userId == -1) {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("main/$userId/$username") { inclusive = true }
                    }
                }
            } else {
                val authViewModel: AuthViewModel = viewModel()
                MainScreen(
                    username = username,
                    userId = userId,
                    onToggleTheme = onToggleTheme,
                    onLogout = {
                        sharedPrefs.edit().clear().apply()
                        onLogout()
                        navController.navigate("login") {
                            popUpTo("main/$userId/$username") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    authViewModel = authViewModel,
                    themePreferences = themePreferences
                )
            }
        }
    }
}














