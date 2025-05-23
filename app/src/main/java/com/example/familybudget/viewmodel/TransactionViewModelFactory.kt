package com.example.familybudget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.familybudget.network.TransactionApiService

class TransactionViewModelFactory(
    private val transactionApiService: TransactionApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(transactionApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
