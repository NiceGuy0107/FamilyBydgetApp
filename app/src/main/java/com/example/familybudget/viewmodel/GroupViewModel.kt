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
import com.example.familybudget.model.UpcomingExpense
import com.example.familybudget.network.CreateGroupRequest
import com.example.familybudget.network.JoinGroupRequest
import com.example.familybudget.network.GroupApiService
import com.example.familybudget.network.CreateUpcomingExpenseRequest
import com.example.familybudget.repository.GroupRepository
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

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

    private val _upcomingExpenses = MutableStateFlow<List<UpcomingExpense>>(emptyList())
    val upcomingExpenses: StateFlow<List<UpcomingExpense>> = _upcomingExpenses

    private val _groups = MutableStateFlow<List<FamilyGroup>>(emptyList())
    val groups: StateFlow<List<FamilyGroup>> = _groups

    fun setError(message: String) {
        _groupState.value = GroupState.Error(message)
    }

    fun getCurrentUserId(context: Context): Long {
        return repository.getCurrentUserId(context)
    }

    fun loadUserGroups(userId: Long) {
        viewModelScope.launch {
            try {
                val userGroups = api.getGroupByUserId(userId)
                _groups.value = userGroups
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Error loading user groups", e)
                setError("Ошибка загрузки групп: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    fun createGroup(name: String, username: String, context: Context) {
        viewModelScope.launch {
            _groupState.value = GroupState.Creating
            try {
                Log.d("GroupViewModel", "Создание группы: $name для пользователя $username")
                val group = api.createGroup(CreateGroupRequest(name, username))
                _groupState.value = GroupState.Joined(group)
                // Обновляем список групп после создания новой
                loadUserGroups(getCurrentUserId(context))
                Log.d("GroupViewModel", "Группа успешно создана: ${group.id}")
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка создания группы", e)
                _groupState.value = GroupState.Error("Ошибка создания группы: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    fun joinGroup(groupId: Long, userId: Long, context: Context) {
        viewModelScope.launch {
            _groupState.value = GroupState.Joining
            try {
                Log.d("GroupViewModel", "Присоединение к группе $groupId пользователем $userId")
                val group = api.joinGroup(JoinGroupRequest(groupId, userId))
                _groupState.value = GroupState.Joined(group)
                // Обновляем список групп после присоединения к новой
                loadUserGroups(userId)
                Log.d("GroupViewModel", "Успешно присоединился к группе: ${group.name}")
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка входа в группу", e)
                _groupState.value = GroupState.Error("Ошибка при входе в группу: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    fun leaveGroup(context: Context, userId: Long) {
        viewModelScope.launch {
            try {
                Log.d("GroupViewModel", "Выход пользователя $userId из группы")
                api.leaveGroup(userId)
                _groupState.value = GroupState.None
                // Обновляем список групп после выхода из группы
                loadUserGroups(userId)
                Log.d("GroupViewModel", "Пользователь $userId вышел из группы")
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка выхода из группы", e)
                _groupState.value = GroupState.Error("Ошибка выхода из группы: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    fun loadGroup(context: Context, userId: Long) {
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

    fun loadUpcomingExpenses(groupId: Long) {
        viewModelScope.launch {
            try {
                val expenses = api.getUpcomingExpenses(groupId)
                _upcomingExpenses.value = expenses
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка загрузки предстоящих расходов", e)
                _upcomingExpenses.value = emptyList()
            }
        }
    }

    fun createUpcomingExpense(
        description: String,
        amount: Double,
        dueDate: LocalDateTime,
        groupId: Long,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                val username = repository.getCurrentUsername(context)
                val request = CreateUpcomingExpenseRequest(
                    description = description,
                    amount = amount,
                    dueDate = dueDate.format(DateTimeFormatter.ISO_DATE_TIME),
                    groupId = groupId,
                    createdByUsername = username
                )
                api.createUpcomingExpense(request)
                loadUpcomingExpenses(groupId)
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка создания предстоящего расхода", e)
                setError("Не удалось создать предстоящий расход: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }

    fun deleteUpcomingExpense(expenseId: Long, context: Context, groupId: Long) {
        viewModelScope.launch {
            try {
                val username = repository.getCurrentUsername(context)
                api.deleteUpcomingExpense(expenseId, username)
                loadUpcomingExpenses(groupId)
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Ошибка удаления предстоящего расхода", e)
                setError("Не удалось удалить предстоящий расход: ${e.message ?: "Неизвестная ошибка"}")
            }
        }
    }
}


