package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==================== vISIONeYe BRAND PALETTE ====================
val VisionEyeBlue = Color(0xFF0A84FF)      // Apple System Electric Blue (High-Vibrancy Dark)
val VisionEyeCyan = Color(0xFF64D2FF)      // Apple System Cyan
val VisionEyeIndigo = Color(0xFF5E5CE6)    // Apple System Indigo
val VisionEyePurple = Color(0xFFBF5AF2)    // Apple System Purple
val VisionEyeDeepNavy = Color(0xFF070C18)  // Luxury Midnight Black/Navy Base
val VisionEyeCanvas = Color(0xFF080E1C)    // Dark Obsidian Canvas

// Admin Dark Primary
val AdminPrimary = VisionEyeBlue
val AdminPrimaryDark = Color(0xFF0056B3)
val AdminPrimaryContainer = Color(0xFF0E254C)
val AdminOnPrimaryContainer = Color(0xFFCCE4FF)

val AdminSecondary = Color(0xFF94A3B8)
val AdminSecondaryContainer = Color(0xFF1E293B)
val AdminOnSecondaryContainer = Color(0xFFF1F5F9)

val AdminBackground = Color(0xFF080E1C)
val AdminSurface = Color(0xFF0F172A)
val AdminSurfaceVariant = Color(0xFF1E293B)
val AdminBorder = Color(0xFF26354D)

// ==================== APPLE GLASSMORPHISM TOKENS (DARK MODE) ====================
// Translucent Frosted Dark Glass Colors (Replaces white glass with obsidian dark glass)
val GlassSurfaceLight = Color(0xE0131E34)          // Deep Frosted Dark Glass
val GlassSurfaceSubtleLight = Color(0xB817243D)    // Subtle Dark Glass Card
val GlassSurfaceElevated = Color(0xF50E1626)       // Elevated Dark Glass Bar / Dialog
val GlassBorderLight = Color(0x35FFFFFF)           // Crisp Top Specular Highlight Edge
val GlassBorderStroke = Color(0x3839527D)          // Crisp Dark Blue Edge Definition

val GlassSurfaceDark = Color(0xF50E1626)           // Dark Frosted Glass
val GlassSurfaceSubtleDark = Color(0xB817243D)     // Dark Subtle Glass
val GlassBorderDark = Color(0x35FFFFFF)            // Dark Specular Edge
val GlassBorderStrokeDark = Color(0x3839527D)      // Dark Edge Definition

// Apple System Status Colors (High-Vibrancy Dark Optimized)
val StatusActiveGreen = Color(0xFF30D158)          // Apple iOS Dark Mode Green
val StatusActiveGreenBg = Color(0x2830D158)        // Translucent Glass Green Fill
val StatusActiveGreenBorder = Color(0x6030D158)

val StatusSuspendedRed = Color(0xFFFF453A)         // Apple iOS Dark Mode Red
val StatusSuspendedRedBg = Color(0x28FF453A)       // Translucent Glass Red Fill
val StatusSuspendedRedBorder = Color(0x60FF453A)

val StatusPendingAmber = Color(0xFFFF9F0A)         // Apple iOS Dark Mode Amber/Orange
val StatusPendingAmberBg = Color(0x28FF9F0A)       // Translucent Glass Amber Fill
val StatusPendingAmberBorder = Color(0x60FF9F0A)

// Gradients
val VisionEyeGlassGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xF0131E34),
        Color(0xDD0C1322)
    )
)

val VisionEyeHeroGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0A84FF),
        Color(0xFF64D2FF)
    )
)

val AppleDarkGlassGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xF0131E34),
        Color(0xDD090F1C)
    )
)
