package com.nuvio.tv.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.nuvio.tv.R
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

internal data class TitaniumCover(
    val origin: IntOffset,
    val extent: IntSize,
    val glow: Color,
    val duration: Int,
    val atlasId: Int = R.drawable.titanium_cover_artwork,
    val label: String? = null,
    val motion: CoverMotion = CoverMotion.RISING
)

internal enum class CoverMotion { RISING, DUST, RAIN }

// Coordinates address the twelve landscape panels in the approved 1672 × 941 concept.
private fun panel(
    left: Int, top: Int, right: Int, bottom: Int, color: Long, duration: Int
) = TitaniumCover(IntOffset(left, top), IntSize(right - left, bottom - top), Color(color), duration)

// The added genre sheet has two columns and three rows. Inset by two pixels
// to keep neighboring panels clear of the sampler at the boundaries.
private fun genrePanel(
    column: Int,
    row: Int,
    label: String,
    color: Long,
    duration: Int,
    motion: CoverMotion = CoverMotion.RISING
): TitaniumCover {
    val top = row * 1024 / 3
    val bottom = (row + 1) * 1024 / 3
    return TitaniumCover(
        origin = IntOffset(column * 768 + 2, top + 2),
        extent = IntSize(764, bottom - top - 4),
        glow = Color(color),
        duration = duration,
        atlasId = R.drawable.titanium_genre_cover_artwork,
        label = label,
        motion = motion
    )
}

internal fun titaniumCoverFor(title: String): TitaniumCover? = when (
    title.trim().lowercase().replace('’', '\'').removePrefix("'")
) {
    "western", "westerns" -> genrePanel(0, 0, "WESTERN", 0xFFFF9F45, 5700, CoverMotion.DUST)
    "comedy" -> genrePanel(1, 0, "COMEDY", 0xFFFF5BB7, 4400)
    "adventure" -> genrePanel(0, 1, "ADVENTURE", 0xFFFFC46A, 5300)
    "crime" -> genrePanel(1, 1, "CRIME", 0xFF62B5FF, 3800, CoverMotion.RAIN)
    "drama" -> genrePanel(0, 2, "DRAMA", 0xFFB785FF, 6500, CoverMotion.RAIN)
    "romance" -> genrePanel(1, 2, "ROMANCE", 0xFFFF80BE, 6300)
    "action" -> panel(296, 348, 629, 486, 0xFFFF7129, 3900)
    "sci-fi", "sci fi", "science fiction" -> panel(646, 348, 973, 486, 0xFF35CBFF, 5700)
    "horror" -> panel(992, 348, 1310, 486, 0xFF77E5CB, 6700)
    "fantasy" -> panel(1328, 348, 1655, 486, 0xFFFFB457, 4900)
    "marvel", "marvel mcu", "marvel collection" -> panel(293, 562, 646, 699, 0xFFF849B9, 4300)
    "dc", "dc comics", "dc collection" -> panel(657, 563, 978, 697, 0xFF349FFF, 6200)
    "star wars", "star wars collection" -> panel(995, 563, 1310, 697, 0xFFFF4569, 4700)
    "jurassic park", "jurassic park collection" -> panel(1328, 563, 1655, 697, 0xFFB2E866, 5400)
    "70s", "1970s" -> panel(297, 772, 629, 909, 0xFFFFB949, 5100)
    "80s", "1980s" -> panel(647, 772, 972, 909, 0xFFF947E3, 5600)
    "90s", "1990s" -> panel(995, 772, 1312, 909, 0xFFBA65FF, 3900)
    "2000s" -> panel(1328, 772, 1655, 909, 0xFF35CCFF, 4500)
    else -> null
}

// Share decoded sheets between lazy cards using the density-independent resources.
private val cachedCoverAtlases = mutableMapOf<Int, ImageBitmap>()

@OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
@Composable
internal fun TitaniumAnimatedCover(cover: TitaniumCover, focused: Boolean) {
    val resources = LocalContext.current.resources
    val atlas = remember(resources, cover.atlasId) {
        cachedCoverAtlases.getOrPut(cover.atlasId) { ImageBitmap.imageResource(resources, cover.atlasId) }
    }
    val animation = if (focused) {
        rememberInfiniteTransition(label = "titaniumCover").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(cover.duration, easing = LinearEasing)),
            label = "coverMotion"
        )
    } else null

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val phase = animation?.value ?: 0f
            val wave = sin(phase * 2 * PI).toFloat()
            val zoom = if (focused) 1.012f + wave * 0.012f else 1f
            val fit = minOf(size.width / cover.extent.width, size.height / cover.extent.height)
            val width = (cover.extent.width * fit * zoom).roundToInt().coerceAtLeast(1)
            val height = (cover.extent.height * fit * zoom).roundToInt().coerceAtLeast(1)
            val x = ((size.width - width) * 0.5f).roundToInt()
            val y = ((size.height - height) * 0.5f).roundToInt()

            drawRect(
                Brush.verticalGradient(listOf(Color(0xFF080C16), cover.glow.copy(alpha = 0.26f), Color(0xFF080C16)))
            )
            clipRect {
                drawImage(
                    image = atlas,
                    srcOffset = cover.origin,
                    srcSize = cover.extent,
                    dstOffset = IntOffset(x, y),
                    dstSize = IntSize(width, height),
                    filterQuality = FilterQuality.Medium
                )
                if (focused) {
                    val beamWidth = size.width * 0.22f
                    val beamLeft = phase * (size.width + beamWidth) - beamWidth
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, cover.glow.copy(alpha = 0.24f), Color.Transparent),
                            startX = beamLeft,
                            endX = beamLeft + beamWidth
                        ),
                        topLeft = Offset(beamLeft, 0f),
                        size = Size(beamWidth, size.height)
                    )

                    for (index in 0 until 9) {
                        val particle = (phase + index / 9f) % 1f
                        val alpha = sin(particle * PI).toFloat() * 0.72f
                        val drift = sin((particle + index) * 5.0).toFloat() * size.width * 0.05f
                        val track = (index * 37 % 97) / 100f
                        val particleColor = cover.glow.copy(alpha = alpha)
                        val center = when (cover.motion) {
                            CoverMotion.RISING -> Offset(
                                size.width * track + drift, size.height * (0.98f - particle * 0.70f)
                            )
                            CoverMotion.DUST -> Offset(
                                size.width * particle, size.height * (0.60f + track * 0.28f) + drift * 0.25f
                            )
                            CoverMotion.RAIN -> Offset(
                                size.width * (track - particle * 0.09f), size.height * particle
                            )
                        }
                        if (cover.motion == CoverMotion.RAIN) {
                            drawLine(particleColor, center, center + Offset(-2f, size.height * 0.13f), 1f)
                        } else {
                            drawCircle(particleColor, radius = 1.1f + index % 3, center = center)
                        }
                    }
                }
            }
        }
        cover.label?.let { label ->
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6080C16))))
                    .padding(start = 6.dp, end = 6.dp, top = 10.dp, bottom = 5.dp)
            )
        }
    }
}
