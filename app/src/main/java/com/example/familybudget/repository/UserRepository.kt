package com.example.familybudget.repository

import com.example.familybudget.model.User
import com.example.familybudget.network.AuthRequest
import com.example.familybudget.network.AuthResponse
import com.example.familybudget.network.RetrofitInstance
import retrofit2.Response

class UserRepository {
    suspend fun register(user: User): Response<AuthResponse> {
        val request = AuthRequest(user.username, user.password)
        return RetrofitInstance.api.register(request)
    }

    suspend fun login(user: User): Response<AuthResponse> {
        val request = AuthRequest(user.username, user.password)
        return RetrofitInstance.api.login(request)
    }
}

