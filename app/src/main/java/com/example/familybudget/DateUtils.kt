package com.example.familybudget

import java.util.*

fun formatIsoDateWithThreeTen(dateString: String): String {
    return try {
        val dateTime = org.threeten.bp.LocalDateTime.parse(dateString)
        val formatter = org.threeten.bp.format.DateTimeFormatter.ofPattern("d MMMM, HH:mm", Locale("ru"))
        dateTime.format(formatter)
    } catch (e: Exception) {
        "Неверная дата"
    }
}

