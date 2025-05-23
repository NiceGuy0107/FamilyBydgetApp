package com.example.familybudget.network

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("users/{username}/exists")
    suspend fun checkUsernameExists(@Path("username") username: String): Response<Boolean>

    @DELETE("/api/auth/{username}")
    suspend fun deleteUser(@Path("username") username: String): Response<Unit>
}

