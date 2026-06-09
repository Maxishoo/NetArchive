package com.example.netarchive.ui.theme

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.netarchive.R

private val LightColorScheme @Composable get() = lightColorScheme(
    primary = appColor(R.color.md_primary),
    onPrimary = appColor(R.color.md_on_primary),
    primaryContainer = appColor(R.color.md_primary_container),
    onPrimaryContainer = appColor(R.color.md_on_primary_container),
    secondary = appColor(R.color.md_secondary),
    onSecondary = appColor(R.color.md_on_secondary),
    background = appColor(R.color.md_background),
    onBackground = appColor(R.color.md_on_background),
    surface = appColor(R.color.md_surface),
    onSurface = appColor(R.color.md_on_surface),
    surfaceVariant = appColor(R.color.md_surface_variant),
    onSurfaceVariant = appColor(R.color.md_on_surface_variant),
    outline = appColor(R.color.md_outline),
)

private val DarkColorScheme @Composable get() = darkColorScheme(
    primary = appColor(R.color.md_primary),
    onPrimary = appColor(R.color.md_on_primary),
    primaryContainer = appColor(R.color.md_primary_container),
    onPrimaryContainer = appColor(R.color.md_on_primary_container),
    secondary = appColor(R.color.md_secondary),
    onSecondary = appColor(R.color.md_on_secondary),
    background = appColor(R.color.md_background),
    onBackground = appColor(R.color.md_on_background),
    surface = appColor(R.color.md_surface),
    onSurface = appColor(R.color.md_on_surface),
    surfaceVariant = appColor(R.color.md_surface_variant),
    onSurfaceVariant = appColor(R.color.md_on_surface_variant),
    outline = appColor(R.color.md_outline),
)

@Composable
fun NetArchiveTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val activityContext = LocalContext.current
    val configuration = LocalConfiguration.current

    val themedConfiguration = remember(darkTheme, configuration) {
        Configuration(configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (darkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        }
    }

    val themedContext = remember(darkTheme, activityContext) {
        activityContext.createConfigurationContext(themedConfiguration)
    }

    CompositionLocalProvider(
        LocalConfiguration provides themedConfiguration,
        LocalContext provides themedContext,
    ) {
        val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
        val appColors = rememberAppColors()

        CompositionLocalProvider(
            LocalContext provides activityContext,
            LocalConfiguration provides themedConfiguration,
            LocalAppColors provides appColors,
        ) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content,
            )
        }
    }
}
