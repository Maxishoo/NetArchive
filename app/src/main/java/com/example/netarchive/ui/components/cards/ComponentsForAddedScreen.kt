package com.example.netarchive.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*


import androidx.compose.ui.res.stringResource
import com.example.netarchive.R


import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

import androidx.compose.material3.darkColorScheme

@Composable
fun SimpleContactCard(
    contactName: String,
    contactAvatar: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (contactAvatar != "") {
                AsyncImage(
                    model = contactAvatar,
                    contentDescription = "Avatar of $contactName",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color = Color(0xFFDBE0F7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contactName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Text(
                text = contactName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
fun DateTimeSelector(
    selectedDate: Long,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(selectedDate))

    OutlinedTextField(
        value = dateString,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.date))},
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = onDateClick) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = stringResource(R.string.icon_event_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun ActionButtonsSaveCancel(
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    saveButtonText: String,
    isEnabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCancelClick,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(stringResource(R.string.cancel))
        }

        Button(
            onClick = onSaveClick,
            modifier = Modifier.weight(1f),
            enabled = !isLoading && isEnabled
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(saveButtonText)
            }
        }
    }
}



// ---------------------------------------------------------------------------
// PREVIEWS ДЛЯ КОМПОНЕНТОВ
// ---------------------------------------------------------------------------

// ========== SimpleContactCard PREVIEWS ==========

@Preview(
    name = "SimpleContactCard - С аватаром",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "ContactCard"
)
@Composable
private fun SimpleContactCardWithAvatarPreview() {
    MaterialTheme {
        Surface {
            SimpleContactCard(
                contactName = "Иванов Иван",
                contactAvatar = "https://i.pravatar.cc/150?img=12",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "SimpleContactCard - Без аватара",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "ContactCard"
)
@Composable
private fun SimpleContactCardWithoutAvatarPreview() {
    MaterialTheme {
        Surface {
            SimpleContactCard(
                contactName = "Петрова Анна",
                contactAvatar = null,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "SimpleContactCard - Пустой аватар (строка)",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "ContactCard"
)
@Composable
private fun SimpleContactCardEmptyStringAvatarPreview() {
    MaterialTheme {
        Surface {
            SimpleContactCard(
                contactName = "Сидоров Пётр",
                contactAvatar = "",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "SimpleContactCard - Длинное имя",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "ContactCard"
)
@Composable
private fun SimpleContactCardLongNamePreview() {
    MaterialTheme {
        Surface {
            SimpleContactCard(
                contactName = "Константинопольский Константин Константинович",
                contactAvatar = null,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "SimpleContactCard - Одна буква",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "ContactCard"
)
@Composable
private fun SimpleContactCardSingleLetterPreview() {
    MaterialTheme {
        Surface {
            SimpleContactCard(
                contactName = "А",
                contactAvatar = null,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

// ========== DateTimeSelector PREVIEWS ==========

@Preview(
    name = "DateTimeSelector - Текущая дата",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "DateTimeSelector"
)
@Composable
private fun DateTimeSelectorCurrentDatePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            DateTimeSelector(
                selectedDate = System.currentTimeMillis(),
                onDateClick = { }
            )
        }
    }
}

@Preview(
    name = "DateTimeSelector - Конкретная дата",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "DateTimeSelector"
)
@Composable
private fun DateTimeSelectorCustomDatePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            // 15 марта 2025
            val calendar = Calendar.getInstance().apply {
                set(2025, Calendar.MARCH, 15)
            }
            DateTimeSelector(
                selectedDate = calendar.timeInMillis,
                onDateClick = { }
            )
        }
    }
}

@Preview(
    name = "DateTimeSelector - Старая дата",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "DateTimeSelector"
)
@Composable
private fun DateTimeSelectorOldDatePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            // 1 января 2020
            val calendar = Calendar.getInstance().apply {
                set(2020, Calendar.JANUARY, 1)
            }
            DateTimeSelector(
                selectedDate = calendar.timeInMillis,
                onDateClick = { }
            )
        }
    }
}

// ========== ActionButtonsSaveCancel PREVIEWS ==========

@Preview(
    name = "ActionButtons - Enabled",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "ActionButtons"
)
@Composable
private fun ActionButtonsEnabledPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            ActionButtonsSaveCancel(
                onCancelClick = { },
                onSaveClick = { },
                saveButtonText = "Сохранить",
                isEnabled = true,
                isLoading = false
            )
        }
    }
}

@Preview(
    name = "ActionButtons - Disabled",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "ActionButtons"
)
@Composable
private fun ActionButtonsDisabledPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            ActionButtonsSaveCancel(
                onCancelClick = { },
                onSaveClick = { },
                saveButtonText = "Создать",
                isEnabled = false,
                isLoading = false
            )
        }
    }
}

@Preview(
    name = "ActionButtons - Loading",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "ActionButtons"
)
@Composable
private fun ActionButtonsLoadingPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            ActionButtonsSaveCancel(
                onCancelClick = { },
                onSaveClick = { },
                saveButtonText = "Сохранить",
                isEnabled = true,
                isLoading = true
            )
        }
    }
}

@Preview(
    name = "ActionButtons - Edit mode text",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5,
    group = "ActionButtons"
)
@Composable
private fun ActionButtonsEditModePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            ActionButtonsSaveCancel(
                onCancelClick = { },
                onSaveClick = { },
                saveButtonText = "Обновить",
                isEnabled = true,
                isLoading = false
            )
        }
    }
}

@Composable
private fun ActionButtonsDarkThemePreview() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface {
            ActionButtonsSaveCancel(
                onCancelClick = { },
                onSaveClick = { },
                saveButtonText = "Сохранить",
                isEnabled = true,
                isLoading = false,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

