package com.example.familybudget.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.formatIsoDateWithThreeTen
import com.example.familybudget.ui.components.DatePickerDialog
import com.example.familybudget.ui.components.TimePickerDialog
import kotlin.math.abs
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

private fun getMonthInGenitiveCase(month: Month): String {
    return when (month) {
        Month.JANUARY -> "января"
        Month.FEBRUARY -> "февраля"
        Month.MARCH -> "марта"
        Month.APRIL -> "апреля"
        Month.MAY -> "мая"
        Month.JUNE -> "июня"
        Month.JULY -> "июля"
        Month.AUGUST -> "августа"
        Month.SEPTEMBER -> "сентября"
        Month.OCTOBER -> "октября"
        Month.NOVEMBER -> "ноября"
        Month.DECEMBER -> "декабря"
    }
}

@Composable
fun AddTransactionDialog(
    isIncome: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, dateTime: LocalDateTime) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isIncome) "Добавить доход" else "Добавить расход",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                            amount = it
                            error = null
                        }
                    },
                    label = { Text("Сумма") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))))
                    }

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                error = "Введите корректную сумму"
                                return@Button
                            }
                            val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                            onConfirm(amountValue, dateTime)
                            onDismiss()
                        }
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { 
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            initialDate = selectedDate
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = {
                selectedTime = it
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            initialTime = selectedTime
        )
    }
}

@Composable
fun TransactionsPage(
    transactions: List<TransactionDto>,
    isIncome: Boolean,
    startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    endDate: LocalDate = LocalDate.now(),
    onAddTransaction: (amount: Double, dateTime: LocalDateTime) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val highlightColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    
    // Вычисляем общую сумму за период
    val totalAmount = transactions.sumOf { abs(it.amount) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(1) {
                    SimpleLineChart(
                        transactions = transactions,
                        isIncome = isIncome,
                        startDate = startDate,
                        endDate = endDate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isIncome) 
                            "Доходы: ${String.format(Locale("ru"), "%,.2f", totalAmount)} ₽"
                        else 
                            "Расходы: ${String.format(Locale("ru"), "%,.2f", totalAmount)} ₽",
                        style = MaterialTheme.typography.titleLarge,
                        color = highlightColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (transactions.isEmpty()) {
                    item {
                        Text(
                            text = "Нет данных для отображения",
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    // Группируем транзакции по датам
                    val groupedTransactions = transactions
                        .groupBy { 
                            try {
                                LocalDateTime.parse(it.date).toLocalDate().toString()
                            } catch (_: Exception) {
                                it.date.take(10)
                            }
                        }
                        .toSortedMap(compareByDescending { it })

                    groupedTransactions.forEach { (date, dailyTransactions) ->
                        item {
                            val formattedDate = try {
                                val dateTime = LocalDateTime.parse(date)
                                val month = getMonthInGenitiveCase(dateTime.month).lowercase()
                                "${dateTime.dayOfMonth} $month"
                            } catch (_: Exception) {
                                date.split("-").let { parts ->
                                    if (parts.size >= 2) {
                                        val month = Month.of(parts[1].toInt())
                                        "${parts[2]} ${getMonthInGenitiveCase(month).lowercase()}"
                                    } else {
                                        date
                                    }
                                }
                            }
                            
                            val dailyTotal = dailyTransactions.sumOf { abs(it.amount) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = String.format(Locale("ru"), "%,.2f ₽", dailyTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = highlightColor
                                )
                            }
                        }

                        // Транзакции за этот день
                        items(dailyTransactions.sortedByDescending { it.date }) { transaction ->
                            val amountAbs = abs(transaction.amount)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = transaction.username,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = if (isIncome) 
                                                "+${String.format(Locale("ru"), "%,.2f", amountAbs)}₽" 
                                            else 
                                                "-${String.format(Locale("ru"), "%,.2f", amountAbs)}₽",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = highlightColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = LocalDateTime.parse(transaction.date).toLocalTime().toString().take(5),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Добавляем пространство в конце списка
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            containerColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336),
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isIncome) Icons.Default.Add else Icons.Default.Remove,
                contentDescription = if (isIncome) "Добавить доход" else "Добавить расход",
                tint = Color.White,
                modifier = Modifier.height(28.dp)
            )
        }

        if (showAddDialog) {
            AddTransactionDialog(
                isIncome = isIncome,
                onDismiss = { showAddDialog = false },
                onConfirm = { amount, dateTime ->
                    onAddTransaction(amount, dateTime)
                    showAddDialog = false
                }
            )
        }
    }
}
