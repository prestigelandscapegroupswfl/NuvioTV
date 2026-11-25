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
    val playerOverlay: Color = Color(0xCC000000),
    // Full-pass tokens: NuvioColorScheme used to hardcode these to neutral grays
    // for every theme, which left Titanium X screens with gray accents, text and
    // borders. Defaults reproduce the old scheme exactly; Titanium X overrides
    // them so the whole app reads in its cyan/violet/magenta-on-navy language.
    val primary: Color = NuvioPrimitives.neutral500,
    val primaryVariant: Color = NuvioPrimitives.neutral650,
    val textSecondary: Color = NuvioPrimitives.neutral400,
    val textTertiary: Color = NuvioPrimitives.neutral600,
    val textDisabled: Color = NuvioPrimitives.neutral700,
    val schemeBorder: Color = NuvioPrimitives.neutral750,
    val schemeDivider: Color = NuvioPrimitives.neutral750
)

object ThemeColors {
    /**
     * Titanium X brand palette: neon cyan -> violet -> magenta on deep navy,
     * matching the Titanium X home mockup. This is the fork's default theme.
     */
    val TitaniumX = ThemeColorPalette(
        secondary = Color(0xFF8B5CF6),
        secondaryVariant = Color(0xFF7C3AED),
        accentGradient = listOf(
            Color(0xFF22D3EE),
            Color(0xFF8B5CF6),
            Color(0xFFE879F9)
        ),
        focusRing = Color(0xFF22D3EE),
        focusRingGradient = listOf(
            Color(0xFF22D3EE),
            Color(0xFF8B5CF6),
            Color(0xFFE879F9)
        ),
        focusBackground = Color(0xFF1B1140),
        background = Color(0xFF050914),
        backgroundElevated = Color(0xFF0A1024),
        backgroundCard = Color(0xFF0D1530),
        surface = Color(0xFF0B1226),
        surfaceVariant = Color(0xFF131E3D),
        panel = Color(0xFF0A1024),
        field = Color(0xFF0D1530),
        menu = Color(0xFF0B1226),
        modal = Color(0xFF0A1024),
        primary = Color(0xFF22D3EE),
        primaryVariant = Color(0xFFE879F9),
        textSecondary = Color(0xFFB9C8E8),
        textTertiary = Color(0xFF7F90B8),
        textDisabled = Color(0xFF526180),
        schemeBorder = Color(0xFF2B3D66),
        schemeDivider = Color(0xFF22345C)
    )

    val Crimson = ThemeColorPalette(
        secondary = NuvioPrimitives.red500,
        secondaryVariant = NuvioPrimitives.red600,
        focusRing = NuvioPrimitives.red300,
        focusBackground = Color(0xFF3D1A1A),
        backgroundCard = Color(0xFF241A1A)
    )

    val Ocean = ThemeColorPalette(
        secondary = NuvioPrimitives.blue500,
        secondaryVariant = NuvioPrimitives.blue700,
        focusRing = NuvioPrimitives.blue300,
        focusBackground = Color(0xFF1A2D3D),
        background = Color(0xFF0D0D0F),
        backgroundElevated = Color(0xFF1A1A1E),
        backgroundCard = Color(0xFF1A1F24)
    )

    val Violet = ThemeColorPalette(
        secondary = NuvioPrimitives.violet500,
        secondaryVariant = NuvioPrimitives.violet700,
        focusRing = NuvioPrimitives.violet300,
        focusBackground = Color(0xFF2D1A3D),
        background = Color(0xFF0D0D0F),
        backgroundElevated = Color(0xFF1A1A1E),
        backgroundCard = Color(0xFF1F1A24)
    )

    val Emerald = ThemeColorPalette(
        secondary = NuvioPrimitives.green500,
        secondaryVariant = NuvioPrimitives.green700,
        focusRing = NuvioPrimitives.green300,
        focusBackground = Color(0xFF1A3D1E),
        backgroundCard = Color(0xFF1A241A)
    )

    val Amber = ThemeColorPalette(
        secondary = NuvioPrimitives.amber500,
        secondaryVariant = NuvioPrimitives.amber700,
        focusRing = NuvioPrimitives.amber300,
        focusBackground = Color(0xFF3D2D1A),
        background = Color(0xFF0F0D0D),
        backgroundElevated = Color(0xFF1E1A1A),
        backgroundCard = Color(0xFF24201A)
    )

    val Rose = ThemeColorPalette(
        secondary = NuvioPrimitives.rose500,
        secondaryVariant = NuvioPrimitives.rose700,
        focusRing = NuvioPrimitives.rose300,
        focusBackground = Color(0xFF3D1A2D),
        backgroundCard = Color(0xFF241A1F)
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
            AppTheme.TITANIUM_X -> TitaniumX
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
