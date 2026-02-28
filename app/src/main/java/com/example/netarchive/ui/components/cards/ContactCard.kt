package com.example.netarchive.ui.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.netarchive.domain.model.Contact
import com.example.netarchive.ui.theme.*


import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults



@Composable
fun ContactCard(
    contact: Contact,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // чтобы полоска растянулась по высоте контента
        ) {
            // Красная полоска слева — вплотную к краю
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(19.dp)
                    .background(PinkLetCard)
            )

            // Контейнер для основного контента с внутренними отступами
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp) // отступы для контента
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Имя контакта
                Text(
                    text = contact.username,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                // Аватар или инициал справа
                if (contact.avatar != null) {
                    Image(
                        painter = rememberAsyncImagePainter(contact.avatar),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Purple200),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.username.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactCardPreview() {
    NetArchiveTheme {
        ContactCard(
            contact = Contact(
                id = 1,
                username = "Иванов Иван",
                phone = null,
                telegram = null,
                max = null,
                email = null,
                job = null,
                avatar = null
            )
        )
    }
}