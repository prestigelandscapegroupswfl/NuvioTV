package com.nuvio.tv.ui.screens.titanium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil3.compose.AsyncImage
import com.nuvio.tv.ui.theme.NuvioTheme

/**
 * Titanium X category tiles (Genres / Collections / Decades), matching the
 * Titanium X home mockup: wide landscape art with neon gradient borders.
 * Art is Jay's finished tile set, hosted on jsDelivr (nuvio-covers repo).
 */

sealed interface TitaniumTileAction {
    /** Open Discover filtered to this genre (TMDB genre naming). */
    data class Genre(val genre: String) : TitaniumTileAction

    /** Open Search pre-filled and submitted with this query. */
    data class Search(val query: String) : TitaniumTileAction

    /** Open the user's Collections screen (decade folders live there). */
    data object Collections : TitaniumTileAction
}

data class TitaniumTile(
    val label: String,
    val imageUrl: String,
    val action: TitaniumTileAction
)

enum class TitaniumSection(
    val title: String,
    val tiles: List<TitaniumTile>
) {
    GENRES(
        title = "GENRES",
        tiles = listOf(
            TitaniumTile("Action", tileUrl("genre-action-tile"), TitaniumTileAction.Genre("Action")),
            TitaniumTile("Sci-Fi", tileUrl("genre-scifi-tile"), TitaniumTileAction.Genre("Science Fiction")),
            TitaniumTile("Horror", tileUrl("genre-horror-tile"), TitaniumTileAction.Genre("Horror")),
            TitaniumTile("Fantasy", tileUrl("genre-fantasy-tile"), TitaniumTileAction.Genre("Fantasy")),
            TitaniumTile("Adventure", tileUrl("genre-adventure-tile"), TitaniumTileAction.Genre("Adventure")),
            TitaniumTile("Comedy", tileUrl("genre-comedy-tile"), TitaniumTileAction.Genre("Comedy"))
        )
    ),
    COLLECTIONS(
        title = "COLLECTIONS",
        tiles = listOf(
            TitaniumTile("Marvel", tileUrl("collection-marvel-tile"), TitaniumTileAction.Search("Marvel")),
            TitaniumTile("DC", tileUrl("collection-dc-tile"), TitaniumTileAction.Search("Batman")),
            TitaniumTile("Star Wars", tileUrl("collection-star-wars-tile"), TitaniumTileAction.Search("Star Wars")),
            TitaniumTile("Jurassic Park", tileUrl("collection-jurassic-park-tile"), TitaniumTileAction.Search("Jurassic")),
            TitaniumTile("Fast & Furious", tileUrl("collection-fast-furious-tile"), TitaniumTileAction.Search("Fast & Furious")),
            TitaniumTile("Avatar", tileUrl("collection-avatar-tile"), TitaniumTileAction.Search("Avatar"))
        )
    ),
    DECADES(
        title = "DECADES",
        tiles = listOf(
            TitaniumTile("70s", tileUrl("decade-70s-tile"), TitaniumTileAction.Collections),
            TitaniumTile("80s", tileUrl("decade-80s-tile"), TitaniumTileAction.Collections),
            TitaniumTile("90s", tileUrl("decade-90s-tile"), TitaniumTileAction.Collections),
            TitaniumTile("2000s", tileUrl("decade-2000s-tile"), TitaniumTileAction.Collections),
            TitaniumTile("2010s", tileUrl("decade-2010s-tile"), TitaniumTileAction.Collections),
            TitaniumTile("2020s", tileUrl("decade-2020s-tile"), TitaniumTileAction.Collections)
        )
    )
}

private fun tileUrl(name: String): String =
    "https://cdn.jsdelivr.net/gh/prestigelandscapegroupswfl/nuvio-covers@main/mockup-tiles/$name.jpg"

/** Cyan -> violet -> magenta neon gradient used for tile borders and glow. */
@Composable
fun titaniumGradientBrush(): Brush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF22D3EE),
        Color(0xFF8B5CF6),
        Color(0xFFE879F9)
    )
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TitaniumTileCard(
    tile: TitaniumTile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 296.dp,
    height: Dp = 166.dp
) {
    var isFocused by remember { mutableStateOf(false) }
    val brush = titaniumGradientBrush()
    val shape = RoundedCornerShape(14.dp)
    val scale = if (isFocused) 1.05f else 1f

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            // Soft neon glow behind the focused tile
            .then(
                if (isFocused) {
                    Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0x5522D3EE),
                                    Color(0x558B5CF6),
                                    Color(0x55E879F9)
                                )
                            ),
                            shape = shape
                        )
                        .padding(3.dp)
                } else {
                    Modifier.padding(3.dp)
                }
            )
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { isFocused = it.isFocused },
            shape = CardDefaults.shape(shape = shape),
            colors = CardDefaults.colors(
                containerColor = NuvioTheme.colors.BackgroundCard,
                focusedContainerColor = NuvioTheme.colors.BackgroundCard
            ),
            border = CardDefaults.border(
                border = androidx.tv.material3.Border(
                    border = BorderStroke(1.dp, brush),
                    shape = shape
                ),
                focusedBorder = androidx.tv.material3.Border(
                    border = BorderStroke(2.5.dp, brush),
                    shape = shape
                )
            ),
            scale = CardDefaults.scale(focusedScale = 1f, pressedScale = 1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(NuvioTheme.colors.BackgroundCard)
            ) {
                AsyncImage(
                    model = tile.imageUrl,
                    contentDescription = tile.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
