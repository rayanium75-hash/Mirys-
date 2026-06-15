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

private val DarkColorScheme = darkColorScheme(
    primary   = MirysBlue,
    secondary = MirysBlueLight,
    tertiary  = MirysBlueLight,
    background = MirysBlack,
    surface    = MirysSurface,
    onPrimary  = MirysWhite,
    onBackground = MirysOnSurface,
    onSurface    = MirysOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary   = MirysBlue,
    secondary = MirysBlueDark,
    tertiary  = MirysBlueLight,
    background = MirysWhite,
    surface    = Color(0xFFF5F5F5),
    onPrimary  = MirysWhite,
    onBackground = MirysBlack,
    onSurface    = MirysBlack
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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
