package com.bit.armylight.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PurpleVibrant,
    secondary = PurpleBright,
    tertiary = PurpleCore,
    background = BackgroundDark,
    surface = GlassSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = BackgroundDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun BitArmyLightTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
