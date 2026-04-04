package com.example.netarchive.ui.screens.settings_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.ui.theme.CardBackground

enum class SettingsPages(
    val icon: ImageVector,
    val title: String,
){
    Main(
        icon = Icons.Outlined.Settings,
        title = "Настройки"
    ),
    Data(
        icon = Icons.Outlined.Folder,
        title = "Данные приложения"
    ),
    Design(
        icon = Icons.Outlined.FormatPaint,
        title = "Дизайн интерфейса"
    ),
    Notifications(
        icon = Icons.Outlined.Notifications,
        title = "Уведомления"
    ),
    AI(
        icon = Icons.Outlined.AutoAwesome,
        title = "ИИ ассистент"
    ),
    ContactsImport(
        icon = Icons.Filled.EmojiPeople,
        title = "Импорт контактов"
    ),
    Language(
        icon = Icons.Outlined.Language,
        title = "Язык"
    ),
    Security(
        icon = Icons.Outlined.Lock,
        title = "Безопасность"
    ),
    Subscription(
        icon = Icons.Outlined.Wallet,
        title = "NetArchive премиум"
    ),
    About(
        icon = Icons.Outlined.Info,
        title = "О приложении"
    ),
    Help(
        icon = Icons.Filled.QuestionAnswer,
        title = "Задать вопрос"
    );

    companion object {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
){
    val viewState by viewModel.viewState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFECEBF4).copy(alpha = 0.95f))
            .padding(top = 30.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
    ){
        IconButton(onClick = onBackClick) {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = viewState.topBarText
            )
        }
        Text(viewState.topBarText, style = MaterialTheme.typography.headlineLarge)
    }
    when (viewState.selectedPage){
        0 -> MainSettings(viewModel)
    }
}

@Composable
fun MainSettings(viewModel: SettingsViewModel) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(1.dp),
        contentPadding = PaddingValues(
            top = 85.dp,
        )
    ) {
        itemsIndexed(SettingsPages.entries) { index, page ->
            PageCard(page) { viewModel.changeSelectedPage(index) }
        }
    }
}

@Composable
fun PageCard(
    element: SettingsPages,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ){
            Icon(
                modifier = Modifier.size(35.dp),
                imageVector = element.icon,
                contentDescription = element.title
            )
            Text(element.title, style = MaterialTheme.typography.labelLarge)
        }
    }
}