package com.example.familybudget.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.formatIsoDateWithThreeTen
import kotlin.math.abs
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

private fun getMonthInGenitiveCase(month: Month): String {
    return when (month) {
        Month.JANUARY -> "январь"
        Month.FEBRUARY -> "февраль"
        Month.MARCH -> "март"
        Month.APRIL -> "апрель"
        Month.MAY -> "май"
        Month.JUNE -> "июнь"
        Month.JULY -> "июль"
        Month.AUGUST -> "август"
        Month.SEPTEMBER -> "сентябрь"
        Month.OCTOBER -> "октябрь"
        Month.NOVEMBER -> "ноябрь"
        Month.DECEMBER -> "декабрь"
    }
}

@Composable
fun TransactionsPage(
    transactions: List<TransactionDto>,
    isIncome: Boolean,
    startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
    endDate: LocalDate = LocalDate.now()
) {
    val highlightColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    
    // Вычисляем общую сумму за период
    val totalAmount = transactions.sumOf { abs(it.amount) }

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
                        "Доходы: ${String.format("%.0f", totalAmount)} ₽"
                    else 
                        "Расходы: ${String.format("%.0f", totalAmount)} ₽",
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
                            "${dateTime.dayOfMonth}.${dateTime.monthValue}"
                        } catch (_: Exception) {
                            date.split("-").let { parts ->
                                if (parts.size >= 2) "${parts[2]}.${parts[1]}" else date
                            }
                        }
                        
                        val dailyTotal = dailyTransactions.sumOf { abs(it.amount) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = String.format("%.0f ₽", dailyTotal),
                                style = MaterialTheme.typography.bodyLarge,
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
                                        text = if (isIncome) "+${amountAbs}₽" else "-${amountAbs}₽",
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
        }
    }
}
