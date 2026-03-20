package com.hereliesaz.reup.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HemorrhageRed,
    background = VoidBlack,
    surface = ConcreteGray,
    onPrimary = VoidBlack,
    onBackground = AshGray,
    onSurface = AshGray
)

@Composable
fun ReUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
