package com.example.familybudget.model

import com.google.gson.annotations.SerializedName

data class FamilyGroup(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("ownerUsername")
    val ownerUsername: String,
    
    @SerializedName("members")
    val members: List<User>
)
