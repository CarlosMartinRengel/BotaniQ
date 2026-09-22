package com.botaniq.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GreenDarkPrimary,
    onPrimary = GreenDarkOnPrimary,
    primaryContainer = GreenDarkPrimaryContainer,
    onPrimaryContainer = GreenDarkOnPrimaryContainer,
    inversePrimary = GreenDarkInversePrimary,
    secondary = GreenDarkSecondary,
    onSecondary = GreenDarkOnSecondary,
    secondaryContainer = GreenDarkSecondaryContainer,
    onSecondaryContainer = GreenDarkOnSecondaryContainer,
    tertiary = GreenDarkTertiary,
    onTertiary = GreenDarkOnTertiary,
    tertiaryContainer = GreenDarkTertiaryContainer,
    onTertiaryContainer = GreenDarkOnTertiaryContainer,
    background = GreenDarkBackground,
    onBackground = GreenDarkOnBackground,
    surface = GreenDarkSurface,
    onSurface = GreenDarkOnSurface,
    surfaceVariant = GreenDarkSurfaceVariant,
    onSurfaceVariant = GreenDarkOnSurfaceVariant,
    surfaceContainerLowest = GreenDarkSurfaceContainerLowest,
    surfaceContainerLow = GreenDarkSurfaceContainerLow,
    surfaceContainer = GreenDarkSurfaceContainer,
    surfaceContainerHigh = GreenDarkSurfaceContainerHigh,
    surfaceContainerHighest = GreenDarkSurfaceContainerHighest,
    outline = GreenDarkOutline,
    outlineVariant = GreenDarkOutlineVariant,
    inverseSurface = GreenDarkInverseSurface,
    inverseOnSurface = GreenDarkInverseOnSurface,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    scrim = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenOnPrimaryContainer,
    inversePrimary = GreenInversePrimary,
    secondary = GreenSecondary,
    onSecondary = GreenOnSecondary,
    secondaryContainer = GreenSecondaryContainer,
    onSecondaryContainer = GreenOnSecondaryContainer,
    tertiary = GreenTertiary,
    onTertiary = GreenOnTertiary,
    tertiaryContainer = GreenTertiaryContainer,
    onTertiaryContainer = GreenOnTertiaryContainer,
    background = GreenBackground,
    onBackground = GreenOnBackground,
    surface = GreenSurface,
    onSurface = GreenOnSurface,
    surfaceVariant = GreenSurfaceVariant,
    onSurfaceVariant = GreenOnSurfaceVariant,
    surfaceContainerLowest = GreenSurfaceContainerLowest,
    surfaceContainerLow = GreenSurfaceContainerLow,
    surfaceContainer = GreenSurfaceContainer,
    surfaceContainerHigh = GreenSurfaceContainerHigh,
    surfaceContainerHighest = GreenSurfaceContainerHighest,
    outline = GreenOutline,
    outlineVariant = GreenOutlineVariant,
    inverseSurface = GreenInverseSurface,
    inverseOnSurface = GreenInverseOnSurface,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    scrim = Color.Black
)

@Composable
fun BotaniQTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
