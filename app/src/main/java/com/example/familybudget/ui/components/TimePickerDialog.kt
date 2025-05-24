package com.example.familybudget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.threeten.bp.LocalTime

@Composable
fun TimePickerDialog(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    initialTime: LocalTime = LocalTime.now()
) {
    var hour by remember { mutableStateOf(initialTime.hour.toString()) }
    var minute by remember { mutableStateOf(initialTime.minute.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Выберите время",
                    style = MaterialTheme.typography.titleLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Часы
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { 
                            if (it.length <= 2 && it.all { ch -> ch.isDigit() }) {
                                hour = it
                                error = null
                            }
                        },
                        label = { Text("Часы") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        isError = error != null
                    )

                    // Минуты
                    OutlinedTextField(
                        value = minute,
                        onValueChange = { 
                            if (it.length <= 2 && it.all { ch -> ch.isDigit() }) {
                                minute = it
                                error = null
                            }
                        },
                        label = { Text("Минуты") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        isError = error != null
                    )
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
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
                            val h = hour.toIntOrNull()
                            val m = minute.toIntOrNull()
                            
                            when {
                                h == null || h < 0 || h > 23 -> {
                                    error = "Введите часы от 0 до 23"
                                    return@Button
                                }
                                m == null || m < 0 || m > 59 -> {
                                    error = "Введите минуты от 0 до 59"
                                    return@Button
                                }
                                else -> {
                                    onTimeSelected(LocalTime.of(h, m))
                                }
                            }
                        }
                    ) {
                        Text("Ок")
                    }
                }
            }
        }
    }
} 