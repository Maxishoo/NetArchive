package com.example.netarchive.ui.screens.contacts_list_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.netarchive.R
import com.example.netarchive.ui.theme.LightBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsTopBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    var showSearchField by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
        .background(color = Color(0xFFECEBF4))
        .padding(top = 30.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showSearchField) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "Поиск"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        showSearchField = false
                        onQueryChange("")
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = "Закрыть"
                        )
                    }
                },
                placeholder = { Text("Поиск контактов") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = LightBlue,
                    unfocusedContainerColor = LightBlue,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        } else {
            Text("Контакты", style = MaterialTheme.typography.headlineLarge)
            IconButton(onClick = { showSearchField = true }) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = "Поиск"
                )
            }
        }
    }
}