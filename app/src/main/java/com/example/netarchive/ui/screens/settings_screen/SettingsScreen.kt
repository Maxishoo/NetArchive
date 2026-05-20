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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.R
import com.example.netarchive.ui.screens.settings_screen.pages.AboutPage
import com.example.netarchive.ui.screens.settings_screen.pages.AppDataPage
import com.example.netarchive.ui.screens.settings_screen.pages.ChangeDesignPage
import com.example.netarchive.ui.screens.settings_screen.pages.ImportContactsPage

enum class SettingsPages(
    val icon: ImageVector,
    val titleResId: Int, // ✅ Было: title: String
) {
    Data(
        icon = Icons.Outlined.Folder,
        titleResId = R.string.settings_page_data
    ),
    ContactsImport(
        icon = Icons.Filled.EmojiPeople,
        titleResId = R.string.settings_page_import_contacts
    ),
    Design(
        icon = Icons.Outlined.FormatPaint,
        titleResId = R.string.settings_page_design
    ),
    Notifications(
        icon = Icons.Outlined.Notifications,
        titleResId = R.string.settings_page_notifications
    ),
    AI(
        icon = Icons.Outlined.AutoAwesome,
        titleResId = R.string.settings_page_ai
    ),
    Language(
        icon = Icons.Outlined.Language,
        titleResId = R.string.settings_page_language
    ),
    Security(
        icon = Icons.Outlined.Lock,
        titleResId = R.string.settings_page_security
    ),
    Subscription(
        icon = Icons.Outlined.Wallet,
        titleResId = R.string.settings_page_subscription
    ),
    About(
        icon = Icons.Outlined.Info,
        titleResId = R.string.settings_page_about
    ),
    Help(
        icon = Icons.Filled.QuestionAnswer,
        titleResId = R.string.settings_page_help
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
        else viewModel.changeSelectedPage(0, R.string.settings_title)
    }

    AnimatedContent(
        targetState = viewState.selectedPage,
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(R.integer.settings_animation_duration_ms)
                ) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(R.integer.settings_animation_duration_ms)
                        )
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(R.integer.settings_animation_duration_ms)
                ) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(R.integer.settings_animation_duration_ms)
                        )
            }
        },
        label = "SettingsPageTransition"
    ) { page ->
        when (page) {
            0 -> MainSettings(viewModel)
            1 -> AppDataPage()
            2 -> ImportContactsPage()
            3 -> ChangeDesignPage(
                isDarkTheme = viewState.isDarkTheme,
                onThemeChange = { viewModel.setDarkTheme(it) }
            )
            9 -> AboutPage()
            else -> EmptyPage()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.top_bar_background).copy(alpha = 0.95f)
            )
            .padding(
                top = dimensionResource(id = R.dimen.top_bar_padding_top_small),
                bottom = dimensionResource(id = R.dimen.top_bar_padding_bottom),
                start = dimensionResource(id = R.dimen.padding_small),
                end = dimensionResource(id = R.dimen.padding_small)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onArrowClick) {
            Icon(
                modifier = Modifier.size(dimensionResource(id = R.dimen.settings_back_icon_size)),
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(
                    if (viewState.selectedPage == 0) R.string.back_to_main else R.string.back_to_settings
                )
            )
        }
        Text(
            text = stringResource(viewState.topBarTextResId), // ✅ Конвертация в @Composable-контексте
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
fun MainSettings(viewModel: SettingsViewModel) {
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.settings_card_spacing)),
        contentPadding = PaddingValues(
            top = dimensionResource(id = R.dimen.settings_list_top_padding)
        )
    ) {
        itemsIndexed(SettingsPages.entries) { index, page ->
            PageCard(page) {
                viewModel.changeSelectedPage(index + 1, page.titleResId) // ✅ Передаём Int, а не String
            }
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
            modifier = Modifier.wrapContentSize(),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_medium)),
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background)),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.settings_card_elevation))
        ) {
            Column(
                modifier = Modifier.padding(all = dimensionResource(id = R.dimen.settings_empty_page_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.settings_empty_icon_size)),
                    imageVector = Icons.Outlined.SentimentDissatisfied,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.settings_page_coming_soon),
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
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_zero)),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_background)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.settings_card_elevation))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_medium),
                    vertical = dimensionResource(id = R.dimen.settings_card_vertical_padding)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
        ) {
            Icon(
                modifier = Modifier.size(dimensionResource(id = R.dimen.settings_page_icon_size)),
                imageVector = element.icon,
                contentDescription = stringResource(element.titleResId) // ✅ Вызов в @Composable
            )
            Text(
                text = stringResource(element.titleResId), // ✅ Вызов в @Composable
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}