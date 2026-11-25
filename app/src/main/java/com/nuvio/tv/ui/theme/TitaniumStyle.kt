package com.nuvio.tv.ui.theme

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import com.nuvio.tv.domain.model.AppTheme

/**
 * Full-pass Titanium X styling helpers.
 *
 * These are the single place where the Titanium X "one build" language lives:
 * a deep-navy vertical wash for screen backgrounds and the cyan -> violet ->
 * magenta accent gradient for selected states (the same gradient the sidebar
 * pill uses). Every helper is a no-op under any other theme, so stock themes
 * keep their exact previous rendering.
 */

@Composable
@ReadOnlyComposable
fun isTitaniumXTheme(): Boolean = NuvioTheme.currentTheme == AppTheme.TITANIUM_X

/**
 * Screen-root background. Titanium X gets a subtle vertical navy wash
 * (background -> backgroundElevated -> background); other themes keep the
 * flat theme background color, including AMOLED handling.
 */
@Composable
fun Modifier.titaniumScreenBackground(): Modifier {
    val colors = NuvioTheme.colors
    return if (isTitaniumXTheme()) {
        background(
            Brush.verticalGradient(
                colors = listOf(
                    colors.Background,
                    colors.BackgroundElevated,
                    colors.Background
                )
            )
        )
    } else {
        background(colors.Background)
    }
}

/**
 * Selected-state fill. When [active] under Titanium X, paints the theme's
 * accent gradient (cyan -> violet -> magenta) in [shape]; the caller is
 * responsible for making its own container color transparent in that case
 * (see [titaniumSelectedContainer]). Under other themes, or when inactive,
 * this leaves the modifier untouched.
 */
@Composable
fun Modifier.titaniumSelectedFill(active: Boolean, shape: Shape): Modifier {
    return if (active && isTitaniumXTheme()) {
        background(brush = NuvioTheme.palette.accentBrush(), shape = shape)
    } else {
        this
    }
}

/**
 * Container color companion for [titaniumSelectedFill]: transparent when the
 * gradient fill is showing, otherwise [fallback].
 */
@Composable
@ReadOnlyComposable
fun titaniumSelectedContainer(active: Boolean, fallback: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
    return if (active && isTitaniumXTheme()) {
        androidx.compose.ui.graphics.Color.Transparent
    } else {
        fallback
    }
}
