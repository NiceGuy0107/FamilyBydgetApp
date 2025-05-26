package com.example.familybudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.familybudget.dto.TransactionDto
import kotlin.random.Random
import org.threeten.bp.LocalDate
import org.threeten.bp.Month

@Composable
fun HomeScreen(username: String, transactions: List<TransactionDto>) {
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

    val cardNumber = "**** **** **** ${Random.nextInt(1000, 9999)}"
    val expiryDate = "${Random.nextInt(1, 13).toString().padStart(2, '0')}/${Random.nextInt(25, 30)}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Приветствие
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = username,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Карта
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0050AC), Color(0xFF9354B9))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "VIRTUAL CARD",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = cardNumber,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXP: $expiryDate",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Выводим блок доходов и расходов
        SummarySection(totalIncome = totalIncome, totalExpense = totalExpense)
    }
}

@Composable
fun SummarySection(totalIncome: Double, totalExpense: Double) {
    // Получаем текущий месяц в родительном падеже
    val currentMonth = remember {
        val month = LocalDate.now().month
        when (month) {
            Month.JANUARY -> "январе"
            Month.FEBRUARY -> "феврале"
            Month.MARCH -> "марте"
            Month.APRIL -> "апреле"
            Month.MAY -> "мае"
            Month.JUNE -> "июне"
            Month.JULY -> "июле"
            Month.AUGUST -> "августе"
            Month.SEPTEMBER -> "сентябре"
            Month.OCTOBER -> "октябре"
            Month.NOVEMBER -> "ноябре"
            Month.DECEMBER -> "декабре"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryValueBlock(
            label = "Доходы в $currentMonth",
            amount = totalIncome,
            amountColor = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        SummaryValueBlock(
            label = "Расходы в $currentMonth",
            amount = totalExpense,
            amountColor = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryValueBlock(label: String, amount: Double, amountColor: Color, modifier: Modifier = Modifier) {
    // Фон rgb(73,93,146)
    Card(
        modifier = modifier
            .height(90.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF495D92))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "%.2f ₽".format(amount),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = amountColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

