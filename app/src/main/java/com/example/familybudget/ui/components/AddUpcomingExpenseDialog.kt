package com.example.familybudget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun AddUpcomingExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (description: String, amount: Double, dueDate: LocalDateTime) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDateTime.now().plusDays(7)) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDate = date.atTime(12, 0)
                showDatePicker = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Добавить предстоящий расход",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { 
                        amountText = it
                        isError = false
                    },
                    label = { Text("Сумма") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    singleLine = true
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Дата: ${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            try {
                                val amount = amountText.toDouble()
                                if (description.isNotBlank() && amount > 0) {
                                    onConfirm(description, amount, selectedDate)
                                    onDismiss()
                                }
                            } catch (e: NumberFormatException) {
                                isError = true
                            }
                        }
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
} 