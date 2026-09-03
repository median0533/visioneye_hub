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

private val DarkColorScheme =
  darkColorScheme(
    primary = VisionEyeBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0E254C),
    onPrimaryContainer = Color(0xFFCCE4FF),
    secondary = VisionEyeCyan,
    onSecondary = Color(0xFF070C18),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFF1F5F9),
    background = Color(0xFF080E1C),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0E1626),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF162238),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF2B3D5B),
    error = StatusSuspendedRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Respect user intent: enforce dark color scheme
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
