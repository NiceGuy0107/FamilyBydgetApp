package com.example.familybudget.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familybudget.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.familybudget.model.FamilyGroup
import com.example.familybudget.model.Transaction
import com.example.familybudget.network.CreateGroupRequest
import com.example.familybudget.network.JoinGroupRequest
import com.example.familybudget.network.GroupApiService
import com.example.familybudget.repository.GroupRepository

sealed class GroupState {
    object None : GroupState()
    object Loading : GroupState()
    object Creating : GroupState()
    object Joining : GroupState()
    data class Joined(val familyGroup: FamilyGroup) : GroupState()
    data class Error(val message: String) : GroupState()
}

class GroupViewModel(
    private val api: GroupApiService,
    private val repository: GroupRepository

) : ViewModel() {

    private val _groupState = MutableStateFlow<GroupState>(GroupState.None)
    val groupState: StateFlow<GroupState> = _groupState

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions


    fun setError(message: String) {
        _groupState.value = GroupState.Error(message)
    }
    fun getCurrentUserId(context: Context): Int {
        return repository.getCurrentUserId(context)
    }

    fun createGroup(name: String, username: String, context: Context) {
        viewModelScope.launch {
            _groupState.value = GroupState.Creating
            try {
                Log.d("GroupViewModel", "Создание группы: $name для пользователя $username")
                val group = api.createGroup(CreateGroupRequest(name, username))
                _groupState.value = GroupState.Joined(group)
                Log.d("GroupViewModel", "Группа успешно создана: ${group.id}")
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка создания группы", e)
                _groupState.value = GroupState.Error("Ошибка создания группы: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    fun joinGroup(groupId: Long, userId: Int, context: Context) {
        viewModelScope.launch {
            _groupState.value = GroupState.Joining
            try {
                Log.d("GroupViewModel", "Присоединение к группе $groupId пользователем $userId")
                val group = api.joinGroup(JoinGroupRequest(groupId, userId))
                _groupState.value = GroupState.Joined(group)
                Log.d("GroupViewModel", "Успешно присоединился к группе: ${group.name}")
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка входа в группу", e)
                _groupState.value = GroupState.Error("Ошибка при входе в группу: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    fun leaveGroup(context: Context, userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("GroupViewModel", "Выход пользователя $userId из группы")
                api.leaveGroup(userId)
                _groupState.value = GroupState.None
                Log.d("GroupViewModel", "Пользователь $userId вышел из группы")
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка выхода из группы", e)
                _groupState.value = GroupState.Error("Ошибка выхода из группы: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    fun loadGroup(context: Context, userId: Int) {
        viewModelScope.launch {
            _groupState.value = GroupState.Loading
            try {
                val groups = api.getGroupByUserId(userId)
                val group = groups.firstOrNull() // Получаем первую группу, если есть
                if (group != null) {
                    _groupState.value = GroupState.Joined(group)
                } else {
                    _groupState.value = GroupState.None
                }
            } catch (e: Exception) {
                _groupState.value = GroupState.Error("Ошибка загрузки группы: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    suspend fun getGroupBalance(groupId: Long): Double {
        return try {
            Log.d("GroupViewModel", "Загрузка транзакций для группы $groupId")
            val transactions = api.getTransactionsForGroup(groupId)
            val balance = transactions.sumOf { txn ->
                if (txn.type == TransactionType.INCOME) txn.amount else -txn.amount
            }
            Log.d("GroupViewModel", "Текущий баланс: $balance")
            balance
        } catch (e: Exception) {
            Log.e("GroupViewModel", "Ошибка получения баланса", e)
            0.0
        }
    }

    fun addTransaction(groupId: Long, amount: Double, type: TransactionType, context: Context, dateTime: String) {
        viewModelScope.launch {
            try {
                repository.addTransaction(
                    groupId = groupId,
                    amount = amount,
                    type = type,
                    context = context,
                    dateTime = dateTime
                )
                loadGroup(context, repository.getCurrentUserId(context))
            } catch (e: Exception) {
                _groupState.value = GroupState.Error("Не удалось добавить транзакцию: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    fun loadTransactions(groupId: Long) {
        viewModelScope.launch {
            try {
                val result = api.getTransactionsForGroup(groupId)
                _transactions.value = result
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка загрузки транзакций", e)
                _transactions.value = emptyList()
            }
        }
    }
}


