package com.nuvio.tv.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.nuvio.tv.domain.model.CollectionFolder
import com.nuvio.tv.ui.theme.NuvioTheme

/** Wide artwork is composed from a textless backdrop and a separate, safely fitted logo/title.
 * Legacy cover images and focus GIFs can contain baked-in poster typography: do not stretch them.
 */
@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
fun TitaniumLandscapeArtwork(folder: CollectionFolder, focused: Boolean) {
    val palette = listOf(
        Color(0xFF36D8FF) to Color(0xFF8352ED),
        Color(0xFFFF579C) to Color(0xFF754ACF),
        Color(0xFFFF793F) to Color(0xFFDB346C),
        Color(0xFF2AE2B1) to Color(0xFF16779C)
    )
    val (categoryAccent, categorySecondary) = palette[Math.floorMod(folder.title.hashCode(), palette.size)]
    val accent = lerp(categoryAccent, NuvioTheme.palette.secondary, 0.2f)
    val secondary = lerp(categorySecondary, NuvioTheme.palette.secondaryVariant, 0.2f)
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF0B0B1C), secondary.copy(alpha = 0.55f), Color(0xFF070A15))))) {
        folder.heroBackdropUrl?.takeIf { it.isNotBlank() }?.let { backdrop ->
            AsyncImage(model = backdrop, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize())
        }
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.12f), Color(0xFF080A18).copy(alpha = 0.95f))))
            drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.82f, size.height * 0.18f), radius = size.width * 0.7f),
                radius = size.width, center = center)
            drawCircle(accent.copy(alpha = 0.17f), radius = size.height * 0.66f,
                center = Offset(size.width * 0.84f, size.height * 0.34f), style = Stroke(1.dp.toPx()))
            drawLine(Brush.horizontalGradient(listOf(accent, secondary)),
                Offset(size.width * 0.07f, size.height * 0.86f), Offset(size.width * 0.93f, size.height * 0.86f),
                strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        }
        val logo = folder.titleLogoUrl?.takeIf { it.isNotBlank() }
        if (logo != null) {
            AsyncImage(model = logo, contentDescription = folder.title, contentScale = ContentScale.Fit,
                modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.76f).height(52.dp).padding(bottom = 10.dp))
        } else {
            Text(text = folder.title.uppercase(), color = Color.White, fontSize = 22.sp,
                fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center).padding(horizontal = 22.dp, vertical = 24.dp))
        }
        Text(text = folder.title, color = Color.White.copy(alpha = 0.92f), fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 7.dp))
        if (focused) {
            val transition = rememberInfiniteTransition(label = "titaniumTile")
            val pulse by transition.animateFloat(0.2f, 0.65f,
                infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse), label = "neonPulse")
            Canvas(Modifier.fillMaxSize()) {
                drawLine(accent.copy(alpha = pulse), Offset(size.width * 0.07f, 1.dp.toPx()),
                    Offset(size.width * 0.93f, 1.dp.toPx()), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            }
        }
    }
}
