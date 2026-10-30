package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VsCodeDarkColorScheme = darkColorScheme(
    primary = Color(0xFF007ACC),
    onPrimary = Color.White,
    secondary = Color(0xFF3794FF),
    onSecondary = Color.White,
    tertiary = Color(0xFF89D185),
    background = VsCodeEditorBg,
    onBackground = Color(0xFFCCCCCC),
    surface = VsCodeSidebarBg,
    onSurface = Color(0xFFCCCCCC),
    surfaceVariant = VsCodeTabBg,
    onSurfaceVariant = VsCodeTabTextInactive,
    error = VsCodeError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = VsCodeDarkColorScheme,
        typography = Typography,
        content = content
    )
}
