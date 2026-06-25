package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val CosmicColorScheme = darkColorScheme(
    primary = CosmicPrimary,
    secondary = CosmicSecondary,
    tertiary = CosmicAccent,
    background = CosmicDarkBg,
    surface = CosmicCardBg,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = CosmicCardBorder,
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for Cosmic Slate vibe
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve designed aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme = CosmicColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
