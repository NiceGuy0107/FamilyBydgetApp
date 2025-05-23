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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.familybudget.dto.TransactionDto
import com.example.familybudget.formatIsoDateWithThreeTen
import androidx.compose.foundation.lazy.items
import kotlin.math.abs


@Composable
fun TransactionsPage(transactions: List<TransactionDto>, isIncome: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (isIncome) "История пополнений" else "История расходов",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (transactions.isEmpty()) {
            Text("Нет данных для отображения", color = Color.Gray)
        } else {
            SimpleLineChart(transactions = transactions, isIncome = isIncome)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isIncome) "Список пополнений:" else "Список расходов:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(transactions.asReversed()) { transaction ->
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
                                    color = if (isIncome) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatIsoDateWithThreeTen(transaction.date),
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
