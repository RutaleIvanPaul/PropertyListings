package io.github.rutaleivanpaul.propertylistings.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = OnBrandOrange,
    background = LightBackground,
    onBackground = OnLight,
    surface = LightSurface,
    onSurface = OnLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightVariant,
    outline = LightOutline,
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrangeDark,
    onPrimary = OnLight,
    background = DarkBackground,
    onBackground = OnDark,
    surface = DarkSurface,
    onSurface = OnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkVariant,
    outline = DarkOutline,
)

/**
 * App theme.
 *
 * Dynamic colour (Material You) is deliberately **not** used: the brand identity is a specific
 * vibrant orange on neutral surfaces, and letting the device wallpaper recolour the app would
 * undermine that. The scheme is therefore fixed, with a light and a dark variant.
 */
@Composable
fun PropertyListingsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
