package com.example.familybudget.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.familybudget.getSavedLanguage
import com.example.familybudget.saveLanguagePreference
import com.example.familybudget.updateLocale
import com.example.familybudget.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import com.example.familybudget.ui.components.LanguageSwitcherButton
import androidx.compose.ui.res.stringResource
import com.example.familybudget.R


@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (String, Long) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by authViewModel.loginState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentLang = remember { mutableStateOf(getSavedLanguage(context)) }

    val loginTitle = stringResource(id = R.string.login)
    val usernameLabel = stringResource(id = R.string.username)
    val passwordLabel = stringResource(id = R.string.password)
    val loginButton = stringResource(id = R.string.login)
    val fillFieldsMessage = stringResource(id = R.string.please_fill_in_all_fields)
    val registerText = stringResource(id = R.string.dont_have_account)
    val loginSuccessMessage = stringResource(id = R.string.login_successful)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = loginTitle,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { 
                    Text(
                        usernameLabel,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { 
                    Text(
                        passwordLabel,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    coroutineScope.launch {
                        authViewModel.login(username, password, context)
                    }
                } else {
                    Toast.makeText(context, fillFieldsMessage, Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(
                    loginButton,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    registerText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            when (loginState) {
                is AuthViewModel.LoginState.Success -> {
                    val state = loginState as AuthViewModel.LoginState.Success
                    LaunchedEffect(state) {
                        Toast.makeText(context, loginSuccessMessage, Toast.LENGTH_SHORT).show()
                        onLoginSuccess(state.username, state.userId)
                    }
                }
                is AuthViewModel.LoginState.Error -> {
                    val message = (loginState as AuthViewModel.LoginState.Error).message
                    LaunchedEffect(message) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
                is AuthViewModel.LoginState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }
                AuthViewModel.LoginState.Idle -> {}
            }
        }

        LanguageSwitcherButton(
            currentLang = currentLang.value,
            onLanguageChange = { newLang ->
                currentLang.value = newLang
                saveLanguagePreference(context, newLang)
                updateLocale(context, newLang)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}




