package com.kaixuan.weightloss.ui.theme

import android.app.Activity
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

private val DarkColorScheme = darkColorScheme(
    primary = Orange80,
    secondary = OrangeGrey80,
    tertiary = Coral80,
    primaryContainer = Color(0xFF4E2600),
    onPrimaryContainer = Orange80,
    surface = Color(0xFF2D2520),
    surfaceVariant = Color(0xFF3D3530),
    background = Color(0xFF1C1815),
    secondaryContainer = Color(0xFF4E3D35),
    onSecondaryContainer = Color(0xFFFFE0C0),
)

private val LightColorScheme = lightColorScheme(
    primary = Orange40,
    secondary = OrangeGrey40,
    tertiary = Coral40,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF4E2600),
    secondaryContainer = Color(0xFFFFF3E0),
    onSecondaryContainer = Color(0xFF4E2600),
    surface = Color(0xFFFFF5EB),           // 柔和奶油色
    surfaceVariant = Color(0xFFFFEEDD),    // 淡杏色
    background = Color(0xFFFFE8D6),        // 柔和橙调背景
)

@Composable
fun WeightLossTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 禁用动态颜色，使用自定义橙色主题
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}