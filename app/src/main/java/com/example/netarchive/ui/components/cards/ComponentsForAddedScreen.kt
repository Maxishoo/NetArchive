package com.example.netarchive.ui.components.cards

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.netarchive.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SimpleContactCard(
    contactName: String,
    contactAvatar: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_medium)),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.card_padding)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
        ) {
            if (!contactAvatar.isNullOrEmpty()) {
                AsyncImage(
                    model = contactAvatar,
                    contentDescription = stringResource(R.string.contact_avatar_description, contactName),
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.contact_card_avatar_size))
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.contact_card_avatar_size))
                        .clip(CircleShape)
                        .background(color = colorResource(id = R.color.reminder_contact_avatar_placeholder)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contactName.firstOrNull()?.uppercaseChar()?.toString() ?: stringResource(R.string.default_avatar),
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
        label = { Text(stringResource(R.string.date)) },
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_small)),
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
                    contentDescription = stringResource(R.string.select_date),
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
fun DateTimeSelector_with_valid(
    selectedDate: Long,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(selectedDate))

    OutlinedTextField(
        value = dateString,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.date)) },
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_small)),
        leadingIcon = {
            Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onDateClick) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = stringResource(R.string.select_date),
                    tint = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.outline
        ),
        supportingText = if (isError) {
            { Text(stringResource(R.string.error_invalid_date), color = MaterialTheme.colorScheme.error) }
        } else null
    )
}

@Composable
fun TimeSelectorCard(
    selectedTime: Long,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val formattedTime = remember(selectedTime) {
        if (selectedTime > 0) {
            val df = SimpleDateFormat("HH:mm", Locale.getDefault())
            df.format(Date(selectedTime))
        } else {
            "--:--"
        }
    }

    OutlinedTextField(
        value = formattedTime,
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.content_description_clock)) },
        modifier = modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(id = R.dimen.padding_small)),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = stringResource(R.string.content_description_clock)
            )
        },
        trailingIcon = {
            IconButton(onClick = onTimeClick) {
                Icon(
                    imageVector = Icons.Default.ArrowOutward,
                    contentDescription = stringResource(R.string.select_time),
                    tint = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.outline
        ),
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null
    )
}

@Composable
private fun NumberPickerColumn(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    wrapSelectorWheel = true
                    this.value = value
                    setOnValueChangedListener { _, _, newVal ->
                        onValueChange(newVal)
                    }
                }
            },
            modifier = Modifier
                .width(dimensionResource(id = R.dimen.number_picker_width))
                .height(dimensionResource(id = R.dimen.number_picker_height)),
            update = { picker ->
                if (picker.value != value) {
                    picker.value = value
                }
            }
        )
    }
}

@Composable
private fun ShowTimePickerDialog(
    initialTime: Long,
    selectedDate: Long,
    onTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    onPastTimeError: () -> Unit
) {
    var hour by remember {
        mutableIntStateOf(
            if (initialTime > 0) {
                Calendar.getInstance().apply { timeInMillis = initialTime }.get(Calendar.HOUR_OF_DAY)
            } else {
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            }
        )
    }
    var minute by remember {
        mutableIntStateOf(
            if (initialTime > 0) {
                Calendar.getInstance().apply { timeInMillis = initialTime }.get(Calendar.MINUTE)
            } else {
                Calendar.getInstance().get(Calendar.MINUTE)
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_time)) },
        text = {
            Column(
                modifier = Modifier
                    .padding(vertical = dimensionResource(id = R.dimen.spacing_small))
                    .width(dimensionResource(id = R.dimen.time_picker_dialog_width)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberPickerColumn(
                        value = hour,
                        onValueChange = { hour = it },
                        range = 0..23,
                        label = stringResource(R.string.hour_abbr)
                    )
                    Text(
                        text = stringResource(R.string.time_separator),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.time_separator_vertical_padding))
                    )
                    NumberPickerColumn(
                        value = minute,
                        onValueChange = { minute = it },
                        range = 0..59,
                        label = stringResource(R.string.minute_abbr)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selectedCalendar = Calendar.getInstance().apply {
                        if (selectedDate > 0) {
                            timeInMillis = selectedDate
                        }
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val selectedTimeMillis = selectedCalendar.timeInMillis
                    if (selectedTimeMillis < System.currentTimeMillis()) {
                        onPastTimeError()
                    } else {
                        onTimeSelected(selectedTimeMillis)
                    }
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
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
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
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
                    modifier = Modifier.size(dimensionResource(id = R.dimen.progress_indicator_size)),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(saveButtonText)
            }
        }
    }
}