package com.example.familybudget.repository

import android.content.Context
import android.util.Log
import com.example.familybudget.model.FamilyGroup
import com.example.familybudget.model.Transaction
import com.example.familybudget.model.TransactionType
import com.example.familybudget.network.AddTransactionRequest
import com.example.familybudget.network.CreateGroupRequest
import com.example.familybudget.network.GroupApiService

class GroupRepository(private val groupApiService: GroupApiService) {

    suspend fun createGroup(name: String, context: Context): FamilyGroup {
        val username = getCurrentUsername(context) // Получаем имя текущего пользователя
        val createGroupRequest = CreateGroupRequest(name, username) // Создаем объект запроса
        return groupApiService.createGroup(createGroupRequest) // Передаем объект в API
    }

    // Сделали public, чтобы ViewModel мог получить имя пользователя
    fun getCurrentUsername(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("username", "") ?: ""
    }

    fun getCurrentUserId(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val id = sharedPreferences.getInt("userId", -1)
        Log.d("PrefsDebug", "getCurrentUserId returned: $id")
        return id
    }

    suspend fun addTransaction(
        groupId: Long,
        amount: Double,
        type: TransactionType,
        context: Context,
        dateTime: String
    ): Transaction {
        val username = getCurrentUsername(context)
        val request = AddTransactionRequest(groupId, amount, username, type.name, dateTime)
        return groupApiService.addTransaction(request)
    }

    suspend fun getTransactionsForGroup(groupId: Long): List<Transaction> {
        return groupApiService.getTransactionsForGroup(groupId)
    }
}


