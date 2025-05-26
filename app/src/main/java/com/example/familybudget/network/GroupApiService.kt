package com.example.familybudget.network

import com.example.familybudget.model.FamilyGroup
import com.example.familybudget.model.Transaction
import com.example.familybudget.model.UpcomingExpense
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.annotations.SerializedName
import com.google.gson.GsonBuilder
import com.google.gson.Gson

interface GroupApiService {
    @GET("group/user")
    suspend fun getGroupByUserId(@Query("userid") userId: Long): List<FamilyGroup>

    @POST("group/create")
    suspend fun createGroup(@Body createGroupRequest: CreateGroupRequest): FamilyGroup

    @POST("group/leave")
    suspend fun leaveGroup(@Query("userId") userId: Long)

    @GET("group/{groupId}/transactions")
    suspend fun getTransactionsForGroup(@Path("groupId") groupId: Long): List<Transaction>

    @POST("group/add-transaction")
    suspend fun addTransaction(@Body request: AddTransactionRequest): Transaction

    @POST("group/join")
    suspend fun joinGroup(@Body joinGroupRequest: JoinGroupRequest): FamilyGroup

    @GET("api/expenses/upcoming/group/{groupId}")
    suspend fun getUpcomingExpenses(@Path("groupId") groupId: Long): List<UpcomingExpense>

    @POST("api/expenses/upcoming")
    suspend fun createUpcomingExpense(@Body request: CreateUpcomingExpenseRequest): UpcomingExpense

    @DELETE("api/expenses/upcoming/{expenseId}")
    suspend fun deleteUpcomingExpense(
        @Path("expenseId") expenseId: Long,
        @Query("username") username: String
    )

    companion object {
        fun create(): GroupApiService {
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(GroupApiService::class.java)
        }
    }
}

data class CreateUpcomingExpenseRequest(
    @SerializedName("description")
    val description: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("dueDate")
    val dueDate: String,
    
    @SerializedName("groupId")
    val groupId: Long,
    
    @SerializedName("createdByUsername")
    val createdByUsername: String
)



