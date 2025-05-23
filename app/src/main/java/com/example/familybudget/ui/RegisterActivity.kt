package com.example.familybudget.ui

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.example.familybudget.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setContent {
            RegisterScreen(authViewModel = authViewModel)
        }
    }
}

@Composable
fun RegisterScreen(authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val registerState by authViewModel.registerState.collectAsState()
    val context = LocalContext.current

    Column {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Button(onClick = {
            if (validateInput(email, password)) {
                // Treat email as username when calling register
                authViewModel.register(email, email, password, context) // Pass email as both username and email
            }
        }) {
            Text("Register")
        }

        when (registerState) {
            is AuthViewModel.RegisterState.Idle -> {}
            is AuthViewModel.RegisterState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthViewModel.RegisterState.Success -> {
                val context = LocalContext.current
                Toast.makeText(
                    context,
                    "Регистрация успешна!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is AuthViewModel.RegisterState.Error -> {
                val context = LocalContext.current
                val errorMessage = (registerState as AuthViewModel.RegisterState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun validateInput(email: String, password: String): Boolean {
    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return false
    }
    if (password.length < 6) {
        return false
    }
    return true
}

