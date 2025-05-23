package com.example.familybudget.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familybudget.R
import com.example.familybudget.network.AuthRequest
import com.example.familybudget.network.ErrorResponse
import com.example.familybudget.network.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class AuthViewModel : ViewModel() {

    private var currentUsername: String? = null
    val username: String
        get() = currentUsername ?: ""

    private var currentUserId: Int? = null
    val userId: Int
        get() = currentUserId ?: -1

    fun setCurrentUserId(id: Int?) {
        currentUserId = id
    }

    fun setCurrentUsername(username: String?) {
        currentUsername = username
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val username: String, val userId: Int) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        data class Success(val username: String, val userId: Int) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun login(username: String, password: String, context: Context) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitInstance.api.login(AuthRequest(username, password))
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUsername = user.username
                        currentUserId = user.id?.toInt()

                        // --- Сохраняем userId и username в SharedPreferences ---
                        saveUserDataToPrefs(context, currentUserId, currentUsername)

                        _loginState.value = LoginState.Success(user.username, user.id?.toInt() ?: -1)
                    } else {
                        _loginState.value = LoginState.Error("Пустой ответ от сервера")
                    }
                } else {
                    val message = when (response.code()) {
                        401 -> "Неверный логин или пароль"
                        else -> "Ошибка входа: ${response.code()}"
                    }
                    _loginState.value = LoginState.Error(message)
                }
            } catch (e: IOException) {
                _loginState.value = LoginState.Error("Нет интернета")
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Сервер недоступен")
            }
        }
    }

    fun register(username: String, email: String, password: String, context: Context) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                Log.d("REGISTER", "Attempting to register with username: $username, email: $email")

                val checkUserResponse = RetrofitInstance.apiService.checkUsernameExists(username)
                if (checkUserResponse.isSuccessful && checkUserResponse.body() == true) {
                    Log.d("REGISTER", "User with username '$username' already exists.")
                    _registerState.value =
                        RegisterState.Error("Пользователь с таким именем уже существует")
                    return@launch
                }

                val response = RetrofitInstance.api.register(AuthRequest(username, password))
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUsername = user.username
                        currentUserId = user.id?.toInt()

                        // --- Сохраняем userId и username в SharedPreferences ---
                        saveUserDataToPrefs(context, currentUserId, currentUsername)

                        Log.d("REGISTER", "Registration successful for username: ${user.username}")
                        _registerState.value = RegisterState.Success(user.username, user.id?.toInt() ?: -1)
                    } else {
                        _registerState.value = RegisterState.Error("Пустой ответ от сервера")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponseType = object : TypeToken<ErrorResponse>() {}.type
                    val errorMessage = try {
                        Gson().fromJson<ErrorResponse>(errorBody, errorResponseType)?.message
                            ?: "Неизвестная ошибка"
                    } catch (e: Exception) {
                        "Неизвестная ошибка"
                    }
                    Log.e("REGISTER", "Registration failed: $errorMessage")
                    _registerState.value = RegisterState.Error(errorMessage)
                }
            } catch (e: IOException) {
                Log.e("REGISTER", "IOException during registration: ${e.message}")
                _registerState.value = RegisterState.Error("Нет интернета")
            } catch (e: Exception) {
                Log.e("REGISTER", "Exception during registration: ${e.message}")
                _registerState.value = RegisterState.Error("Ошибка сервера")
            }
        }
    }

    private fun saveUserDataToPrefs(context: Context, userId: Int?, username: String?) {
        if (userId == null || username == null) return
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("userId", userId)
            .putString("username", username)
            .apply()
    }

    fun deleteAccount(context: Context, onResult: (Boolean, String) -> Unit) {
        val username = currentUsername
        if (username == null) {
            val message = context.getString(R.string.delete_user_missing_username)
            Log.e("DELETE_ACCOUNT", message)
            onResult(false, message)
            return
        }

        Log.d("DELETE_ACCOUNT", "Попытка удалить пользователя: $username")
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.deleteUser(username)
                if (response.isSuccessful) {
                    currentUsername = null
                    currentUserId = null

                    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().clear().apply()

                    val msg = context.getString(R.string.delete_user_success)
                    onResult(true, msg)
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> context.getString(R.string.delete_user_not_found)
                        403 -> context.getString(R.string.delete_user_forbidden)
                        500 -> context.getString(R.string.delete_user_server_error)
                        else -> context.getString(
                            R.string.delete_user_unknown_error,
                            response.code()
                        )
                    }
                    Log.e("DELETE_ACCOUNT", errorMessage)
                    onResult(false, errorMessage)
                }
            } catch (e: IOException) {
                val msg = context.getString(R.string.delete_user_no_internet)
                Log.e("DELETE_ACCOUNT", "IOException: ${e.message}")
                onResult(false, msg)
            } catch (e: Exception) {
                val msg = context.getString(R.string.delete_user_exception)
                Log.e("DELETE_ACCOUNT", "Exception: ${e.message}")
                onResult(false, msg)
            }
        }
    }
}



