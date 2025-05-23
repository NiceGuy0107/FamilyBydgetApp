package com.example.familybudget.model

data class FamilyGroup(
    val id: String,
    val name: String,
    val members: List<User>
)
