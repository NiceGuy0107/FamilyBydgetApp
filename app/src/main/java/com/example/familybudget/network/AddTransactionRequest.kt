package com.example.familybudget.network

import com.example.familybudget.model.TransactionType
import com.google.gson.annotations.SerializedName

data class AddTransactionRequest(
    @SerializedName("groupId")
    val groupId: Long,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("username")
    val username: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("description")
    val description: String = ""
)