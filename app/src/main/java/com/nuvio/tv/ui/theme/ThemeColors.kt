package com.nuvio.tv.ui.theme

import androidx.compose.ui.graphics.Color
import com.nuvio.tv.domain.model.AppTheme
import com.nuvio.tv.domain.model.CustomThemeColors

data class ThemeColorPalette(
    val secondary: Color,
    val secondaryVariant: Color,
    val onSecondary: Color = NuvioPrimitives.white,
    val onSecondaryVariant: Color = NuvioPrimitives.white,
    val accentGradient: List<Color> = listOf(secondary),
    val focusRing: Color,
    val focusRingGradient: List<Color> = listOf(focusRing),
    val focusBackground: Color,
    val background: Color = NuvioPrimitives.neutral950,
    val backgroundElevated: Color = NuvioPrimitives.neutral900,
    val backgroundCard: Color = NuvioPrimitives.neutral825,
    val surface: Color = NuvioPrimitives.neutral875,
    val surfaceVariant: Color = NuvioPrimitives.neutral800,
    val panel: Color = NuvioPrimitives.neutral900,
    val overlay: Color = Color(0xD9000000),
    val field: Color = NuvioPrimitives.neutral850,
    val menu: Color = NuvioPrimitives.neutral875,
    val modal: Color = NuvioPrimitives.neutral900,
    val playerOverlay: Color = Color(0xCC000000)
)

object ThemeColors {
    val Crimson = ThemeColorPalette(
        secondary = Color(0xFFF25C65),
        secondaryVariant = Color(0xFFA82446),
        accentGradient = listOf(Color(0xFFF25C65), Color(0xFFFF9C8D)),
        focusRing = Color(0xFFFF9C8D),
        focusRingGradient = listOf(Color(0xFFF25C65), Color(0xFFFF9C8D)),
        focusBackground = Color(0xFF3D2028),
        background = Color(0xFF251217),
        backgroundElevated = Color(0xFF3D2028),
        backgroundCard = Color(0xFF3D2028),
        surface = Color(0xFF3D2028)
    )

    val Ocean = ThemeColorPalette(
        secondary = Color(0xFF36C8FF),
        secondaryVariant = Color(0xFF356AA3),
        accentGradient = listOf(Color(0xFF36C8FF), Color(0xFFD2ECFF)),
        focusRing = Color(0xFFD2ECFF),
        focusRingGradient = listOf(Color(0xFF36C8FF), Color(0xFFD2ECFF)),
        focusBackground = Color(0xFF142B43),
        background = Color(0xFF071B30),
        backgroundElevated = Color(0xFF142B43),
        backgroundCard = Color(0xFF142B43),
        surface = Color(0xFF142B43)
    )

    val Violet = ThemeColorPalette(
        secondary = Color(0xFFFF5399),
        secondaryVariant = Color(0xFF754ACF),
        accentGradient = listOf(Color(0xFFFF5399), Color(0xFF46E6FC)),
        focusRing = Color(0xFF46E6FC),
        focusRingGradient = listOf(Color(0xFFFF5399), Color(0xFF46E6FC)),
        focusBackground = Color(0xFF302043),
        background = Color(0xFF14162D),
        backgroundElevated = Color(0xFF302043),
        backgroundCard = Color(0xFF302043),
        surface = Color(0xFF302043)
    )

    val Emerald = ThemeColorPalette(
        secondary = Color(0xFF20DC8A),
        secondaryVariant = Color(0xFF177D69),
        accentGradient = listOf(Color(0xFF20DC8A), Color(0xFFC9ED60)),
        focusRing = Color(0xFFC9ED60),
        focusRingGradient = listOf(Color(0xFF20DC8A), Color(0xFFC9ED60)),
        focusBackground = Color(0xFF153B30),
        background = Color(0xFF082C23),
        backgroundElevated = Color(0xFF153B30),
        backgroundCard = Color(0xFF153B30),
        surface = Color(0xFF153B30)
    )

    val Amber = ThemeColorPalette(
        secondary = Color(0xFFFF6A32),
        secondaryVariant = Color(0xFFD53922),
        accentGradient = listOf(Color(0xFFFF6A32), Color(0xFFFFC451)),
        focusRing = Color(0xFFFFC451),
        focusRingGradient = listOf(Color(0xFFFF6A32), Color(0xFFFFC451)),
        focusBackground = Color(0xFF4B2519),
        background = Color(0xFF300B0B),
        backgroundElevated = Color(0xFF4B2519),
        backgroundCard = Color(0xFF4B2519),
        surface = Color(0xFF4B2519)
    )

    val Rose = ThemeColorPalette(
        secondary = Color(0xFFFF5399),
        secondaryVariant = Color(0xFFF56B3E),
        accentGradient = listOf(Color(0xFFFF5399), Color(0xFFFFD29B)),
        focusRing = Color(0xFFFFD29B),
        focusRingGradient = listOf(Color(0xFFFF5399), Color(0xFFFFD29B)),
        focusBackground = Color(0xFF45233A),
        background = Color(0xFF29122E),
        backgroundElevated = Color(0xFF45233A),
        backgroundCard = Color(0xFF45233A),
        surface = Color(0xFF45233A)
    )

    val White = ThemeColorPalette(
        secondary = NuvioPrimitives.neutral100,
        secondaryVariant = NuvioPrimitives.neutral200,
        onSecondary = NuvioPrimitives.neutral925,
        onSecondaryVariant = NuvioPrimitives.neutral925,
        focusRing = NuvioPrimitives.white,
        focusBackground = Color(0xFF303030),
        backgroundCard = NuvioPrimitives.neutral850
    )

    fun getColorPalette(
        theme: AppTheme,
        customColors: CustomThemeColors = CustomThemeColors.Default
    ): ThemeColorPalette {
        return when (theme) {
            AppTheme.CUSTOM -> customColors.toColorPalette()
            AppTheme.GOLD -> SupporterThemeColors.Gold
            AppTheme.JADE -> SupporterThemeColors.Jade
            AppTheme.ROSE_GOLD -> SupporterThemeColors.RoseGold
            AppTheme.ARCTIC_BLUE -> SupporterThemeColors.ArcticBlue
            AppTheme.GRAPHITE -> SupporterThemeColors.Graphite
            AppTheme.CRIMSON -> Crimson
            AppTheme.OCEAN -> Ocean
            AppTheme.VIOLET -> Violet
            AppTheme.EMERALD -> Emerald
            AppTheme.AMBER -> Amber
            AppTheme.ROSE -> Rose
            AppTheme.WHITE -> White
        }
    }
}
