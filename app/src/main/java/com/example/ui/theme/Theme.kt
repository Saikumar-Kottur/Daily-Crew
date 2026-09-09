package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandSuccessGreen,
    onPrimary = BrandDeepBlueDark,
    primaryContainer = BrandSuccessGreenDim,
    onPrimaryContainer = BrandSuccessGreen,
    secondary = BrandSecondaryCyan,
    onSecondary = BrandDeepBlue,
    secondaryContainer = BrandSecondaryCyanDim,
    onSecondaryContainer = BrandSecondaryCyan,
    tertiary = BrandAmber,
    background = BrandBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = BrandSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = BrandCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BrandCardBorderDark,
    error = BrandErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = BrandSuccessGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F8E5),
    onPrimaryContainer = Color(0xFF005228),
    secondary = BrandDeepBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2EBF5),
    onSecondaryContainer = BrandDeepBlue,
    tertiary = Color(0xFFE65100),
    background = BrandBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = BrandSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEAEFF5),
    onSurfaceVariant = TextSecondaryLight,
    outline = BrandCardBorderLight,
    error = BrandErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
