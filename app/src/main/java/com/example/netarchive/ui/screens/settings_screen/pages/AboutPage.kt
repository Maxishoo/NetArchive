package com.example.netarchive.ui.screens.settings_screen.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AboutPage() {
    Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp, start = 20.dp, end = 20.dp)) {
        Text("NetArchive - мобильное приложение для поддержания слабых связей. Многие специалисты теряют ценные связи из-за отсутствия удобного инструмента для регулярного поддержания контактов: они забывают важные детали общения, пропускают поводы для взаимодействия и упускают карьерные возможности. Приложение решает эту проблему с помощью метода «картотеки» — фиксации взаимодействий, системы проактивных напоминаний и ИИ ассистента.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}