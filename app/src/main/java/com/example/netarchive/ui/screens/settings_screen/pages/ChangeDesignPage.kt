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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.netarchive.R

@Composable
fun ChangeDesignPage(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val circleOffset by animateDpAsState(
        targetValue = if (isDarkTheme) dimensionResource(id = R.dimen.toggle_circle_offset_on)
        else dimensionResource(id = R.dimen.toggle_circle_offset_off),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "circleOffset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.padding_medium),
                vertical = dimensionResource(id = R.dimen.screen_padding_top)
            ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_large)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation_default))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.card_padding)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_medium))
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.NightsStay else Icons.Default.WbSunny,
                    contentDescription = stringResource(
                        if (isDarkTheme) R.string.theme_dark_content_description
                        else R.string.theme_light_content_description
                    ),
                    tint = if (isDarkTheme)
                        colorResource(id = R.color.theme_icon_dark)
                    else
                        colorResource(id = R.color.theme_icon_light),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.theme_icon_size))
                )
                Text(
                    text = stringResource(if (isDarkTheme) R.string.theme_dark else R.string.theme_light),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .size(
                        width = dimensionResource(id = R.dimen.toggle_width),
                        height = dimensionResource(id = R.dimen.toggle_height)
                    )
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.toggle_corner_radius)))
                    .background(
                        if (isDarkTheme)
                            colorResource(id = R.color.toggle_background_dark)
                        else
                            colorResource(id = R.color.toggle_background_light)
                    )
                    .clickable { onThemeChange(!isDarkTheme) }
                    .padding(dimensionResource(id = R.dimen.toggle_padding)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.toggle_circle_size))
                        .offset(x = circleOffset)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.toggle_circle))
                        .shadow(
                            dimensionResource(id = R.dimen.toggle_circle_shadow),
                            CircleShape
                        )
                )
            }
        }
    }
}