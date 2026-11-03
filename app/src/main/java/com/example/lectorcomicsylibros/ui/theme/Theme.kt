package com.example.lectorcomicsylibros.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.lectorcomicsylibros.data.settings.AppThemeMode
import kotlin.math.abs

fun colorIntToHsv(colorInt: Int, hsv: FloatArray) {
    val r = ((colorInt shr 16) and 0xFF) / 255f
    val g = ((colorInt shr 8) and 0xFF) / 255f
    val b = (colorInt and 0xFF) / 255f

    val max = maxOf(r, maxOf(g, b))
    val min = minOf(r, minOf(g, b))
    val delta = max - min

    var h = 0f
    val s = if (max == 0f) 0f else delta / max
    val v = max

    if (delta != 0f) {
        h = when (max) {
            r -> ((g - b) / delta) % 6f
            g -> ((b - r) / delta) + 2f
            else -> ((r - g) / delta) + 4f
        }
        h *= 60f
        if (h < 0f) h += 360f
    }

    hsv[0] = h
    hsv[1] = s
    hsv[2] = v
}

fun hsvToColorInt(hsv: FloatArray): Int {
    val h = (hsv[0] % 360f + 360f) % 360f
    val s = hsv[1].coerceIn(0f, 1f)
    val v = hsv[2].coerceIn(0f, 1f)

    val c = v * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = v - c

    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    val r = ((r1 + m) * 255f).toInt().coerceIn(0, 255)
    val g = ((g1 + m) * 255f).toInt().coerceIn(0, 255)
    val b = ((b1 + m) * 255f).toInt().coerceIn(0, 255)

    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}

fun createDynamicM3ColorScheme(seedColorInt: Int, isDark: Boolean): ColorScheme {
    val hsv = FloatArray(3)
    colorIntToHsv(seedColorInt, hsv)
    val hue = hsv[0]
    val sat = hsv[1]
    val valVal = hsv[2]

    fun hsvToColor(h: Float, s: Float, v: Float): Color {
        val colorInt = hsvToColorInt(floatArrayOf(h, s, v))
        return Color(colorInt)
    }

    return if (isDark) {
        val primary = hsvToColor(hue, sat.coerceAtMost(0.6f), (valVal * 1.2f).coerceIn(0.7f, 0.95f))
        val onPrimary = hsvToColor(hue, sat, 0.15f)
        val primaryContainer = hsvToColor(hue, sat.coerceAtLeast(0.5f), 0.35f)
        val onPrimaryContainer = hsvToColor(hue, (sat * 0.3f).coerceAtMost(0.3f), 0.95f)

        val secondary = hsvToColor(hue + 25f, (sat * 0.5f).coerceAtMost(0.5f), 0.85f)
        val onSecondary = hsvToColor(hue + 25f, sat, 0.15f)
        val secondaryContainer = hsvToColor(hue + 25f, sat, 0.3f)
        val onSecondaryContainer = hsvToColor(hue + 25f, 0.2f, 0.95f)

        val tertiary = hsvToColor(hue + 60f, (sat * 0.5f).coerceAtMost(0.5f), 0.85f)
        val onTertiary = hsvToColor(hue + 60f, sat, 0.15f)
        val tertiaryContainer = hsvToColor(hue + 60f, sat, 0.3f)
        val onTertiaryContainer = hsvToColor(hue + 60f, 0.2f, 0.95f)

        val background = Color(0xFF121316)
        val onBackground = Color(0xFFE2E2E6)
        val surface = Color(0xFF121316)
        val onSurface = Color(0xFFE2E2E6)
        val surfaceVariant = Color(0xFF43474E)
        val onSurfaceVariant = Color(0xFFC3C7CE)

        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant
        )
    } else {
        val primary = Color(seedColorInt)
        val onPrimary = if (valVal > 0.7f && sat < 0.3f) Color.Black else Color.White
        val primaryContainer = hsvToColor(hue, (sat * 0.25f).coerceAtMost(0.3f), 0.95f)
        val onPrimaryContainer = hsvToColor(hue, (sat * 0.9f).coerceAtLeast(0.6f), 0.25f)

        val secondary = hsvToColor(hue + 25f, sat * 0.6f, valVal * 0.8f)
        val onSecondary = Color.White
        val secondaryContainer = hsvToColor(hue + 25f, 0.2f, 0.92f)
        val onSecondaryContainer = hsvToColor(hue + 25f, 0.8f, 0.25f)

        val tertiary = hsvToColor(hue + 60f, sat * 0.6f, valVal * 0.8f)
        val onTertiary = Color.White
        val tertiaryContainer = hsvToColor(hue + 60f, 0.2f, 0.92f)
        val onTertiaryContainer = hsvToColor(hue + 60f, 0.8f, 0.25f)

        val background = Color(0xFFFBFCFF)
        val onBackground = Color(0xFF191C1E)
        val surface = Color(0xFFF8F9FC)
        val onSurface = Color(0xFF191C1E)
        val surfaceVariant = Color(0xFFE0E3E8)
        val onSurfaceVariant = Color(0xFF43474E)

        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant
        )
    }
}

@Composable
fun LectorComicsyLibrosTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    primaryColorHex: Int = 0xFF6750A4.toInt(),
    useDynamicColorIfSystem: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColorIfSystem && themeMode == AppThemeMode.SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            createDynamicM3ColorScheme(primaryColorHex, darkTheme)
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            if (activity != null) {
                val window = activity.window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
