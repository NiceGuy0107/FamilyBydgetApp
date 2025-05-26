package com.example.familybudget.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.familybudget.R
import com.example.familybudget.getSavedLanguage
import com.example.familybudget.saveLanguagePreference
import com.example.familybudget.ui.components.LanguageSwitcherButton
import com.example.familybudget.updateLocale
import com.example.familybudget.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String, Long) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val registerState by authViewModel.registerState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentLang = remember { mutableStateOf(getSavedLanguage(context)) }

    val registerTitle = stringResource(id = R.string.register)
    val emailLabel = stringResource(id = R.string.email)
    val usernameLabel = stringResource(id = R.string.username)
    val passwordLabel = stringResource(id = R.string.password)
    val registerButton = stringResource(id = R.string.register_button)
    val loginText = stringResource(id = R.string.login_text)
    val fillFieldsMessage = stringResource(id = R.string.fill_fields_message)
    val registrationSuccessMessage = stringResource(id = R.string.registration_success_message)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = registerTitle,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { 
                    Text(
                        emailLabel,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                }
            )

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
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    coroutineScope.launch {
                        authViewModel.register(username, email, password,context)
                    }
                } else {
                    Toast.makeText(context, fillFieldsMessage, Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(
                    registerButton,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    loginText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Handle register state
            when (val state = registerState) {
                is AuthViewModel.RegisterState.Success -> {
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, registrationSuccessMessage, Toast.LENGTH_SHORT).show()
                        onRegisterSuccess(state.username, state.userId)
                    }
                }

                is AuthViewModel.RegisterState.Error -> {
                    LaunchedEffect(state.message) {
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                }

                is AuthViewModel.RegisterState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }

                AuthViewModel.RegisterState.Idle -> {
                    // Nothing
                }
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





