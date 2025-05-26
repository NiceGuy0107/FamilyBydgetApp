package com.example.familybudget

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
        
        // Safe retrieval of userId that handles both Integer and Long cases
        val userId = try {
            sharedPreferences.getLong("userId", -1L).takeIf { it != -1L }
        } catch (e: ClassCastException) {
            // If stored as Integer, convert to Long
            sharedPreferences.getInt("userId", -1).takeIf { it != -1 }?.toLong()
        }

        Log.d("MainActivity", "Starting app with username: $username, userId: $userId")

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
    initialUserId: Long?,
    sharedPreferences: SharedPreferences
) {
    val context = LocalContext.current
    val langCode = getSavedLanguage(context)
    val localizedContext = remember(langCode) { updateLocale(context, langCode) }

    val themePreferences = remember { ThemePreferences(localizedContext) }
    val coroutineScope = rememberCoroutineScope()

    var usernameState by remember { mutableStateOf(initialUsername) }
    var userIdState by remember { mutableStateOf(initialUserId) }

    var currentTheme by remember { mutableStateOf("light") }

    Log.d("AppContent", "Current states - username: $usernameState, userId: $userIdState")

    // загружаем тему при входе
    LaunchedEffect(usernameState) {
        usernameState?.let { username ->
            currentTheme = themePreferences.getTheme(username)
            Log.d("AppContent", "Loaded theme for user: $username - theme: $currentTheme")
        }
    }

    val isDarkTheme = currentTheme == "dark"
    val isUserLoggedIn = usernameState != null && userIdState != null

    Log.d("AppContent", "isUserLoggedIn: $isUserLoggedIn")

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
                    Log.d("AppContent", "Logging out user")
                    sharedPreferences.edit().clear().apply()
                    usernameState = null
                    userIdState = null
                    currentTheme = "light"
                },
                onLoginSuccess = { loggedInUsername, loggedInUserId ->
                    Log.d("AppContent", "Login success - username: $loggedInUsername, userId: $loggedInUserId")
                    usernameState = loggedInUsername
                    userIdState = loggedInUserId
                },
                themePreferences = themePreferences,
                userId = userIdState
            )
        }
    }
}

@Composable
fun MyApp(
    onToggleTheme: (String) -> Unit,
    onLogout: () -> Unit = {},
    onLoginSuccess: (String, Long) -> Unit = { _, _ -> },
    isLoggedIn: Boolean = false,
    themePreferences: ThemePreferences,
    userId: Long? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    Log.d("MyApp", "Starting with isLoggedIn: $isLoggedIn, userId: $userId")

    val startDestination = if (isLoggedIn && userId != null) {
        val username = sharedPrefs.getString("username", "") ?: ""
        Log.d("MyApp", "User is logged in, navigating to main with userId: $userId, username: $username")
        "main/$userId/$username"
    } else {
        Log.d("MyApp", "User is not logged in, navigating to login")
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            Log.d("MyApp", "Composing login screen")
            val authViewModel: AuthViewModel = viewModel()

            val apiService = remember { GroupApiService.create() }
            val factory = remember { GroupViewModelFactory(apiService) }
            val groupViewModel: GroupViewModel = viewModel(factory = factory)

            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { 
                    Log.d("MyApp", "Navigating to register")
                    navController.navigate("register") 
                },
                onLoginSuccess = { username, userId ->
                    Log.d("MyApp", "Login successful, saving data - username: $username, userId: $userId")
                    sharedPrefs.edit()
                        .putString("username", username)
                        .putLong("userId", userId)
                        .apply()
                    onLoginSuccess(username, userId)
                    Log.d("MyApp", "Navigating to main screen")
                    navController.navigate("main/$userId/$username") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            Log.d("MyApp", "Composing register screen")
            val authViewModel: AuthViewModel = viewModel()
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { 
                    Log.d("MyApp", "Navigating back to login")
                    navController.navigate("login") 
                },
                onRegisterSuccess = { username, userId ->
                    Log.d("MyApp", "Registration successful, saving data - username: $username, userId: $userId")
                    sharedPrefs.edit()
                        .putString("username", username)
                        .putLong("userId", userId)
                        .apply()
                    onLoginSuccess(username, userId)
                    Log.d("MyApp", "Navigating to main screen")
                    navController.navigate("main/$userId/$username") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "main/{userId}/{username}",
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: -1L
            val username = backStackEntry.arguments?.getString("username") ?: ""
            Log.d("MyApp", "Composing main screen with userId: $userId, username: $username")

            if (userId == -1L) {
                Log.d("MyApp", "Invalid userId, redirecting to login")
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
                        Log.d("MyApp", "Logging out user")
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














