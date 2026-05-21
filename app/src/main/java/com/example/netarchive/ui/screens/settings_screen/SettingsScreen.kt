package com.example.netarchive.ui.screens.settings_screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.material.icons.outlined.SentimentDissatisfied
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
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.ui.theme.AppTheme
import com.example.netarchive.R
import com.example.netarchive.ui.screens.settings_screen.pages.AboutPage
import com.example.netarchive.ui.screens.settings_screen.pages.AppDataPage
import com.example.netarchive.ui.screens.settings_screen.pages.ChangeDesignPage
import com.example.netarchive.ui.screens.settings_screen.pages.GoogleCalendarPage
import com.example.netarchive.ui.screens.settings_screen.pages.ImportContactsPage
import com.example.netarchive.ui.theme.CardBackground

enum class SettingsPages(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
) {
    Data(
        icon = Icons.Outlined.Folder,
        titleRes = R.string.settings_app_data
    ),
    ContactsImport(
        icon = Icons.Filled.EmojiPeople,
        titleRes = R.string.settings_import_contacts
    ),
    Design(
        icon = Icons.Outlined.FormatPaint,
        titleRes = R.string.settings_design
    ),
    Notifications(
        icon = Icons.Outlined.Notifications,
        titleRes = R.string.settings_notifications
    ),
    AI(
        icon = Icons.Outlined.AutoAwesome,
        titleRes = R.string.settings_ai
    ),
    Language(
        icon = Icons.Outlined.Language,
        titleRes = R.string.settings_language
    ),
    Security(
        icon = Icons.Outlined.Lock,
        titleRes = R.string.settings_security
    ),
    Subscription(
        icon = Icons.Outlined.Wallet,
        titleRes = R.string.settings_premium
    ),
    About(
        icon = Icons.Outlined.Info,
        titleRes = R.string.settings_about
    ),
    Help(
        icon = Icons.Filled.QuestionAnswer,
        titleRes = R.string.settings_help
    );
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()

    val onArrowClick = {
        if (viewState.selectedPage == 0) onBackClick()
        else viewModel.changeSelectedPage(0)
    }

    AnimatedContent(
        targetState = viewState.selectedPage,
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) togetherWith
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            }
        },
        label = "SettingsPageTransition"
    ) { page ->
        when (page) {
            0 -> MainSettings(viewModel)
            1 -> AppDataPage()
            2 -> ImportContactsPage()
            3 -> ChangeDesignPage()
            4 -> GoogleCalendarPage()
            9 -> AboutPage()
            else -> EmptyPage()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.topBarBackground())
            .padding(top = 30.dp, bottom = 8.dp, start = 10.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onArrowClick) {
            Icon(
                modifier = Modifier.size(30.dp),
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(viewState.topBarTextRes)
            )
        }
        Text(stringResource(viewState.topBarTextRes), style = MaterialTheme.typography.headlineLarge)
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
            PageCard(page) { viewModel.changeSelectedPage(index + 1, page.titleRes) }
        }
    }
}

@Composable
fun EmptyPage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .wrapContentSize(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(all = 50.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(55.dp),
                    imageVector = Icons.Outlined.SentimentDissatisfied,
                    contentDescription = null
                )
                Text(
                    stringResource(R.string.settings_page_not_ready),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                modifier = Modifier.size(35.dp),
                imageVector = element.icon,
                contentDescription = stringResource(element.titleRes)
            )
            Text(stringResource(element.titleRes), style = MaterialTheme.typography.labelLarge)
        }
    }
}