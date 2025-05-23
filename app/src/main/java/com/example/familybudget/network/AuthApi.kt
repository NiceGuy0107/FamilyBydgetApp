package com.example.familybudget.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequest(val username: String, val password: String)
data class AuthResponse(val id: Long?, val username: String)

interface AuthApi {
    @POST("/api/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

}

