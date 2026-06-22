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

private val DarkColorScheme =
  darkColorScheme(
    primary = BoldPrimaryDark,
    onPrimary = BoldOnPrimaryDark,
    primaryContainer = BoldPrimaryContainerDark,
    onPrimaryContainer = BoldOnPrimaryContainerDark,
    background = BoldBackgroundDark,
    onBackground = BoldOnBackgroundDark,
    surface = BoldSurfaceDark,
    onSurface = BoldOnSurfaceDark,
    surfaceVariant = BoldSurfaceVariantDark,
    onSurfaceVariant = BoldOnSurfaceVariantDark,
    outline = BoldOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BoldPrimaryLight,
    onPrimary = BoldOnPrimaryLight,
    primaryContainer = BoldPrimaryContainerLight,
    onPrimaryContainer = BoldOnPrimaryContainerLight,
    background = BoldBackgroundLight,
    onBackground = BoldOnBackgroundLight,
    surface = BoldSurfaceLight,
    onSurface = BoldOnSurfaceLight,
    surfaceVariant = BoldSurfaceVariantLight,
    onSurfaceVariant = BoldOnSurfaceVariantLight,
    outline = BoldOutlineLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Force our custom themed look, avoiding system overlays
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
