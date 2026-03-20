package com.hereliesaz.reup.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// The icon dictates a dark mode leaning. We establish a rich Dark Scheme that reflects the palette best.
// The focused deep blue (EyePupilBlue) becomes the Primary accent.
// The forest green (HillForestGreen) is the Secondary accent.
// The harsh golden rays (RayGold) form the Tertiary accents.
// The deep Indigo night sky (SkyIndigo) and Purple base (HillDarkPurpleGreen) form the background and component surfaces.

private val DarkColorScheme = darkColorScheme(
    primary = EyePupilBlue,       // The watchful deep blue
    onPrimary = Color.White,
    secondary = HillForestGreen,   // The organic green
    onSecondary = Color.White,
    tertiary = RayGold,            // The geometric golden rays
    onTertiary = Color.Black,
    background = SkyIndigo,        // The deep night
    onBackground = Color.White,
    surface = HillDarkPurpleGreen,  // The base texture
    onSurface = Color.White,
    error = SunRed,                // The visceral red
    onError = Color.White,
    surfaceVariant = SkyPurple,     // Variant surfaces
    onSurfaceVariant = Color.White
)

// A Light Scheme adapted from the warmer 'daylight' side of the icon (SunsetOrangeRed, IrisYellow).
// For use in environments where the void must remain brightly lit.
private val LightColorScheme = lightColorScheme(
    primary = EyePupilBlue,
    onPrimary = Color.White,
    secondary = HillForestGreen,
    onSecondary = Color.White,
    tertiary = SunOrange,           // Warm orange accent
    onTertiary = Color.White,
    background = Color(0xFFFEFDF5), // Light cream for maximum readability
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = SunRed,
    onError = Color.White
)

@Composable
fun ReUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false by default to ensure the specific extracted palette is used.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Mapping the status bar color to the deep night sky indigo of the icon's top edge.
            window.statusBarColor = if (darkTheme) SkyIndigo.toArgb() else Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // Typography needs to be defined standard elsewhere (Type.kt)
        content = content
    )
}