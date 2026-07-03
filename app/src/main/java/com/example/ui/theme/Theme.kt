package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BentoPrimaryDarkTheme,
    secondary = BentoPurpleCard,
    tertiary = BentoBlueCard,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = BentoPrimaryDark,
    onSecondary = BentoPrimaryDarkTheme,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    outline = BentoBorder,
    outlineVariant = BentoBorder.copy(alpha = 0.5f)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BentoPrimary,
    secondary = BentoPurpleCard,
    tertiary = BentoBlueCard,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = BentoPrimaryDark,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    outline = BentoBorder,
    outlineVariant = BentoBorder.copy(alpha = 0.5f)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors by default so Bento Grid branding is fully applied
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
