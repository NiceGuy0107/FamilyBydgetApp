package com.example.familybudget.ui.screens.tabs


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
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

    LaunchedEffect(userId) {
        if (userId != -1) {
            groupViewModel.loadGroup(context, userId)
        }
    }

    LaunchedEffect((groupState as? GroupState.Joined)?.familyGroup?.id) {
        val group = (groupState as? GroupState.Joined)?.familyGroup
        if (group != null) {
            coroutineScope.launch {
                balance = groupViewModel.getGroupBalance(group.id)
            }
        }
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
                                                val code = joinGroupCode.toLongOrNull()
                                                if (code != null && userId != -1) {
                                                    groupViewModel.joinGroup(code, userId, context)
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
                    var transactionAmount by remember { mutableStateOf("") }

                    Spacer(Modifier.height(spacing))

                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    balance?.let {
                        Text(
                            "Баланс: %.2f ₽".format(it),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))

                    Spacer(Modifier.height(spacing))

                    Text(
                        "Участники группы",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(group.members.toList()) { member ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = 2.dp,
                                    color = Color.Transparent,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = member.username ?: "Пользователь",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(spacing))

                    // Одна карточка с одним полем и двумя кнопками
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
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                "Введите сумму",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = transactionAmount,
                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                                        transactionAmount = it
                                    }
                                },
                                label = { Text("Сумма") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val amount = transactionAmount.toDoubleOrNull()
                                        if (amount != null && amount > 0) {
                                            coroutineScope.launch {
                                                groupViewModel.addTransaction(
                                                    group.id,
                                                    amount,
                                                    TransactionType.INCOME,
                                                    context
                                                )
                                                transactionAmount = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent, // прозрачный фон
                                        contentColor = MaterialTheme.colorScheme.secondary // цвет текста и иконки
                                    ),
                                    border = BorderStroke(
                                        1.5.dp,
                                        MaterialTheme.colorScheme.secondary
                                    ) // цветная обводка
                                ) {
                                    Icon(
                                        Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Пополнить", color = MaterialTheme.colorScheme.secondary)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val amount = transactionAmount.toDoubleOrNull()
                                        if (amount != null && amount > 0) {
                                            coroutineScope.launch {
                                                groupViewModel.addTransaction(
                                                    group.id,
                                                    amount,
                                                    TransactionType.EXPENSE,
                                                    context
                                                )
                                                transactionAmount = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(
                                        Icons.Default.MoneyOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Снять", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                            Spacer(Modifier.height(spacing))

                    OutlinedButton(
                        onClick = {
                            if (userId != -1) {
                                groupViewModel.leaveGroup(context, userId)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Покинуть группу",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
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










