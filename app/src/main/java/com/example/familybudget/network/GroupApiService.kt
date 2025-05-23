package com.example.familybudget.network

import com.example.familybudget.model.FamilyGroup
import com.example.familybudget.model.Transaction
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface GroupApiService {
    @GET("group/user")
    suspend fun getGroupByUserId(@Query("userid") userId: Int): List<FamilyGroup>

    @POST("group/create")
    suspend fun createGroup(@Body createGroupRequest: CreateGroupRequest): FamilyGroup

    @POST("group/leave")
    suspend fun leaveGroup(@Query("userId") userId: Int)

    @GET("group/{groupId}/transactions")
    suspend fun getTransactionsForGroup(@Path("groupId") groupId: String): List<Transaction>

    @POST("group/add-transaction")
    suspend fun addTransaction(@Body request: AddTransactionRequest): Transaction

    @POST("group/join")
    suspend fun joinGroup(@Body joinGroupRequest: JoinGroupRequest): FamilyGroup

    companion object {
        fun create(): GroupApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(GroupApiService::class.java)
        }
    }
}



