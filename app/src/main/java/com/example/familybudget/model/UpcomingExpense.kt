package com.example.familybudget.model

import org.threeten.bp.LocalDateTime
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter
import com.example.familybudget.network.LocalDateTimeAdapter

data class UpcomingExpense(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("dueDate")
    @JsonAdapter(LocalDateTimeAdapter::class)
    val dueDate: LocalDateTime,
    
    @SerializedName("groupId")
    val groupId: String,
    
    @SerializedName("createdByUsername")
    val createdByUsername: String
) 