package com.example.familybudget

import android.app.Activity
import android.content.Context
import java.util.*

// Сохраняем выбранный язык
fun saveLanguagePreference(context: Context, langCode: String) {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("lang", langCode).apply()
}

// Обновляем локаль приложения
fun updateLocale(context: Context, langCode: String): Context {
    val locale = Locale(langCode)
    Locale.setDefault(locale)

    val config = context.resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)

    // Применение изменений конфигурации
    context.resources.updateConfiguration(config, context.resources.displayMetrics)

    return context.createConfigurationContext(config)
}

// Получаем сохранённый язык из SharedPreferences
fun getSavedLanguage(context: Context): String {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return prefs.getString("lang", "ru") ?: "ru"  // Если язык не найден, по умолчанию используем русский
}

// Перезапускаем приложение для применения изменений
fun restartApp(activity: Activity) {
    // Перезапуск активности
    activity.recreate()
}



