package com.nuvio.tv.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.nuvio.tv.ui.theme.NuvioTheme
import com.nuvio.tv.ui.theme.brandWordmarkResource

/**
 * App-wide brand wordmark override, provided by MainActivity. When the
 * Titanium X experience is active (Titanium X theme OR Titanium X home
 * layout) this holds the Titanium X wordmark so every surface — sidebar
 * headers, splash, profile selection — brands as Titanium X even when the
 * profile's colour theme is a different one. Null outside Titanium X, in
 * which case per-call overrides and the theme wordmark apply as before.
 */
val LocalBrandWordmarkOverride = compositionLocalOf<Int?> { null }

@Composable
fun BrandWordmark(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1f,
    drawableOverride: Int? = null
) {
    Image(
        painter = painterResource(
            id = LocalBrandWordmarkOverride.current
                ?: drawableOverride
                ?: NuvioTheme.currentTheme.brandWordmarkResource
        ),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alpha = alpha
    )
}
