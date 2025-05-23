package com.example.familybudget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familybudget.network.GroupApiService
import com.example.familybudget.repository.GroupRepository

class GroupViewModelFactory(
    private val apiService: GroupApiService,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = GroupRepository(apiService)
        if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupViewModel(apiService, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
