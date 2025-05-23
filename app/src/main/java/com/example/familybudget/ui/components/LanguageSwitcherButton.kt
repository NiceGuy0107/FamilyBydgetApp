package com.example.familybudget.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.familybudget.R
import com.example.familybudget.saveLanguagePreference
import com.example.familybudget.updateLocale

@Composable
fun LanguageSwitcherButton(
    currentLang: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val langText = if (currentLang == "ru") stringResource(id = R.string.lang_ru) else stringResource(id = R.string.lang_en)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable {
                val newLang = if (currentLang == "ru") "en" else "ru"
                onLanguageChange(newLang)
                saveLanguagePreference(context, newLang)
                updateLocale(context, newLang)
                val activity = context as? Activity
                activity?.recreate()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = langText,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}





