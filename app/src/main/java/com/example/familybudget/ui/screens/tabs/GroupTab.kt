package com.example.familybudget.ui.screens.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.familybudget.viewmodel.GroupState
import com.example.familybudget.viewmodel.GroupViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.familybudget.model.TransactionType
import com.example.familybudget.model.UpcomingExpense
import com.example.familybudget.ui.components.AddUpcomingExpenseDialog
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupTab(
    username: String,
    userId: Int,
    navController: NavController,
    groupViewModel: GroupViewModel
) {
    val groupState by groupViewModel.groupState.collectAsState()
    var balance by remember { mutableStateOf<Double?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf("none") }
    var groupName by remember { mutableStateOf("") }
    var joinGroupCode by remember { mutableStateOf("") }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<UpcomingExpense?>(null) }

    LaunchedEffect(userId) {
        if (userId != -1) {
            groupViewModel.loadGroup(context, userId)
        }
    }

    LaunchedEffect((groupState as? GroupState.Joined)?.familyGroup?.id) {
        val group = (groupState as? GroupState.Joined)?.familyGroup
        if (group != null) {
            coroutineScope.launch {
                balance = groupViewModel.getGroupBalance(group.id.toLong())
                groupViewModel.loadUpcomingExpenses(group.id.toLong())
            }
        }
    }

    if (showAddExpenseDialog) {
        AddUpcomingExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { description, amount, dueDate ->
                val group = (groupState as? GroupState.Joined)?.familyGroup
                if (group != null) {
                    coroutineScope.launch {
                        groupViewModel.createUpcomingExpense(
                            description = description,
                            amount = amount,
                            dueDate = dueDate,
                            groupId = group.id.toLong(),
                            context = context
                        )
                    }
                }
            }
        )
    }

    if (selectedExpense != null) {
        AlertDialog(
            onDismissRequest = { selectedExpense = null },
            title = { Text("Детали расхода") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = selectedExpense!!.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Сумма: %.2f ₽".format(selectedExpense!!.amount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Добавил: ${selectedExpense!!.createdByUsername}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Дата: ${selectedExpense!!.dueDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val group = (groupState as? GroupState.Joined)?.familyGroup
                            if (group != null) {
                                groupViewModel.deleteUpcomingExpense(
                                    expenseId = selectedExpense!!.id.toLong(),
                                    groupId = group.id.toLong(),
                                    context = context
                                )
                            }
                        }
                        selectedExpense = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedExpense = null }) {
                    Text("Закрыть")
                }
            }
        )
    }

    val spacing = 16.dp
    val buttonModifier = Modifier
        .fillMaxWidth()
        .height(54.dp)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing, vertical = spacing / 2),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = groupState) {
                is GroupState.Loading, is GroupState.Creating, is GroupState.Joining -> {
                    Spacer(Modifier.height(40.dp))
                    CircularProgressIndicator()
                    Spacer(Modifier.height(spacing))
                    Text(
                        text = when (state) {
                            is GroupState.Creating -> "Создание группы..."
                            is GroupState.Joining -> "Присоединение..."
                            else -> "Загрузка..."
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is GroupState.None -> {
                    Text(
                        "Вы не в группе",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = spacing)
                    )

                    AnimatedVisibility(visible = selectedOption == "none") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { selectedOption = "create" },
                                modifier = buttonModifier,
                                shape = RoundedCornerShape(24.dp),
                                elevation = ButtonDefaults.buttonElevation(8.dp)
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Создать группу")
                            }

                            OutlinedButton(
                                onClick = { selectedOption = "join" },
                                modifier = buttonModifier,
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Присоединиться к группе")
                            }
                        }
                    }

                    AnimatedVisibility(visible = selectedOption != "none") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(spacing)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    if (selectedOption == "create") "Создание группы" else "Вход в группу",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )

                                OutlinedTextField(
                                    value = if (selectedOption == "create") groupName else joinGroupCode,
                                    onValueChange = {
                                        if (selectedOption == "create") groupName = it else joinGroupCode = it
                                    },
                                    label = {
                                        Text(if (selectedOption == "create") "Название группы" else "Код группы")
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            if (selectedOption == "create" && groupName.isNotBlank()) {
                                                groupViewModel.createGroup(groupName.trim(), username, context)
                                            } else if (selectedOption == "join") {
                                                try {
                                                    val code = joinGroupCode.trim().toLong()
                                                    if (userId != -1) {
                                                        groupViewModel.joinGroup(code, userId, context)
                                                    }
                                                } catch (e: NumberFormatException) {
                                                    // Handle invalid input
                                                    groupViewModel.setError("Неверный формат кода группы. Введите числовое значение.")
                                                }
                                            }
                                        }
                                    },
                                    modifier = buttonModifier,
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = ButtonDefaults.buttonElevation(8.dp)
                                ) {
                                    Text(if (selectedOption == "create") "Создать" else "Присоединиться")
                                }

                                TextButton(
                                    onClick = { selectedOption = "none" },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Назад")
                                }
                            }
                        }
                    }
                }

                is GroupState.Joined -> {
                    val group = state.familyGroup
                    val expenses: List<UpcomingExpense> = groupViewModel.upcomingExpenses.collectAsState().value

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        item {
                            Spacer(Modifier.height(4.dp))
                        }

                        item {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }

                        item {
                            Spacer(Modifier.height(4.dp))
                        }

                        // Список участников
                        items(group.members.toList().sortedBy { it.id }) { member ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 2.dp,
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = member.username ?: "Пользователь",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        item {
                            Spacer(Modifier.height(8.dp))
                        }

                        item {
                            balance?.let {
                                Text(
                                    "Баланс: %.2f ₽".format(it),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        item {
                            Spacer(Modifier.height(8.dp))
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Предстоящие расходы",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { showAddExpenseDialog = true },
                                    modifier = Modifier.height(36.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF495D92)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Добавить",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Добавить",
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Группировка по датам
                        val groupedExpenses = expenses
                            .sortedByDescending { it.dueDate }
                            .groupBy { it.dueDate.toLocalDate() }

                        groupedExpenses.forEach { (date, expensesForDate) ->
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = date.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "%.2f ₽".format(expensesForDate.sumOf { it.amount }),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            items(expensesForDate) { expense ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = { },
                                                onLongClick = { selectedExpense = expense }
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = expense.description,
                                                style = MaterialTheme.typography.bodyLarge,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = String.format(Locale("ru"), "%,.2f ₽", expense.amount),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Добавил: ${expense.createdByUsername}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                if (expensesForDate.last() != expense) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }

                is GroupState.Error -> {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        "Ошибка: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}










