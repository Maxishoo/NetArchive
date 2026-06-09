package com.example.netarchive.ui.screens.settings_screen.pages

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.R
import com.example.netarchive.ui.screens.settings_screen.ThemeViewModel
import com.example.netarchive.ui.theme.AppTheme

@Composable
fun ChangeDesignPage(
    viewModel: ThemeViewModel = hiltViewModel(),
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val colors = AppTheme.colors
    val circleOffset by animateDpAsState(
        targetValue = if (isDarkTheme) 24.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "circleOffset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.NightsStay else Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = if (isDarkTheme) colors.themeIconDarkMode else colors.themeIconLightMode,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = if (isDarkTheme) {
                        stringResource(R.string.design_dark_theme)
                    } else {
                        stringResource(R.string.design_light_theme)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isDarkTheme) colors.themeSwitchTrackDark else colors.themeSwitchTrackLight
                    )
                    .clickable { viewModel.toggleTheme() }
                    .padding(4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .offset(x = circleOffset)
                        .clip(CircleShape)
                        .background(colors.themeSwitchThumb)
                        .shadow(2.dp, CircleShape)
                )
            }
        }
    }
}
