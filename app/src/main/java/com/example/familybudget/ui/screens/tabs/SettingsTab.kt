package com.example.familybudget.ui.screens.tabs

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.familybudget.R
import com.example.familybudget.restartApp
import com.example.familybudget.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import com.example.familybudget.saveLanguagePreference
import com.example.familybudget.getSavedLanguage
import com.example.familybudget.preferences.SettingItem
import com.example.familybudget.preferences.ThemePreferences


@Composable
fun SettingsTab(
    onToggleTheme: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel,
    themePreferences: ThemePreferences
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val username = sharedPrefs.getString("username", null) ?: ""

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedThemeKey by remember { mutableStateOf("light") }

    LaunchedEffect(username) {
        selectedThemeKey = themePreferences.getTheme(username)
    }

    val selectedTheme = when (selectedThemeKey) {
        "dark" -> context.getString(R.string.dark_theme)
        else -> context.getString(R.string.light_theme)
    }

    val savedLangCode = getSavedLanguage(context)
    var selectedLang by remember {
        mutableStateOf(
            when (savedLangCode) {
                "en" -> context.getString(R.string.english)
                else -> context.getString(R.string.russian)
            }
        )
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun onDeleteConfirmed() {
        coroutineScope.launch {
            isDeleting = true
            authViewModel.deleteAccount(context) { isDeleted, message ->
                isDeleting = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (isDeleted) {
                    onLogout()
                    onNavigateToRegister()
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_question)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteConfirmed()
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.choose_theme)) },
            text = {
                Column {
                    listOf(
                        context.getString(R.string.light_theme) to "light",
                        context.getString(R.string.dark_theme) to "dark"
                    ).forEach { (label, key) ->
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedThemeKey = key
                                    onToggleTheme(key)
                                    showThemeDialog = false
                                }
                                .padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.choose_language)) },
            text = {
                Column {
                    listOf(
                        context.getString(R.string.russian) to "ru",
                        context.getString(R.string.english) to "en"
                    ).forEach { (label, code) ->
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLang = label
                                    saveLanguagePreference(context, code)
                                    showLanguageDialog = false
                                    (context as? Activity)?.let { restartApp(it) }
                                }
                                .padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    Scaffold(
        // bottomBar убран, кнопка перенесена внутрь Card
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.general),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SettingItem(
                        title = stringResource(R.string.choose_theme),
                        value = selectedTheme,
                        onClick = { showThemeDialog = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingItem(
                        title = stringResource(R.string.choose_language),
                        value = selectedLang,
                        onClick = { showLanguageDialog = true }
                    )
                }

                // Новый заголовок "Аккаунт"
                Text(
                    text = "Аккаунт",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                )

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        stringResource(R.string.logout),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_account), fontWeight = FontWeight.Bold)
                }

                if (isDeleting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}





