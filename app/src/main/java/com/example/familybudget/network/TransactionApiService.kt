package com.example.familybudget.network

import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.model.Transaction
import retrofit2.http.*

interface TransactionApiService {

    @GET("api/transactions/{groupId}/transactions")
    suspend fun getTransactions(@Path("groupId") groupId: Long?): List<TransactionDto>

    @GET("api/transactions/{username}/transactions")
    suspend fun getUserTransactions(@Path("username") username: String): List<TransactionDto>

    @POST("api/transactions")
    suspend fun addTransaction(@Body request: AddTransactionRequest): TransactionDto

    @GET("api/transactions/user/{userId}/transactions")
    suspend fun getUserTransactions(@Path("userId") userId: Int): List<TransactionDto>

}

