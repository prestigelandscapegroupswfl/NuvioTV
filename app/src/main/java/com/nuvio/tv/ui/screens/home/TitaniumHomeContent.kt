package com.nuvio.tv.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.R
import com.nuvio.tv.core.poster.CustomPosterScreen
import com.nuvio.tv.core.poster.patternForScreen
import com.nuvio.tv.core.poster.withCustomPosterUrls
import com.nuvio.tv.domain.model.CatalogRow
import com.nuvio.tv.domain.model.MetaPreview
import com.nuvio.tv.domain.model.stableKey
import com.nuvio.tv.ui.components.CatalogRowSection
import com.nuvio.tv.ui.components.CollectionRowSection
import com.nuvio.tv.ui.components.ContinueWatchingSection
import com.nuvio.tv.ui.components.PosterCardStyle
import com.nuvio.tv.ui.screens.titanium.TitaniumSection
import com.nuvio.tv.ui.screens.titanium.TitaniumTile
import com.nuvio.tv.ui.screens.titanium.TitaniumTileCard
import com.nuvio.tv.ui.theme.NuvioTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Titanium X home layout, built to Jay's v2 mockup:
 * brand hero band with a live clock, then GENRES / COLLECTIONS / DECADES
 * neon tile rows leading, followed by Continue Watching and the account's
 * existing home rows (catalogs + collections) rendered with the shared
 * section composables so all data, addons and playback stay functional.
 */

@Composable
internal fun TitaniumHomeRoute(
    viewModel: HomeViewModel,
    uiState: HomeUiState,
    posterCardStyle: PosterCardStyle,
    onNavigateToDetail: (String, String, String) -> Unit,
    onContinueWatchingClick: (ContinueWatchingItem) -> Unit,
    onContinueWatchingStartFromBeginning: (ContinueWatchingItem) -> Unit,
    onContinueWatchingPlayManually: (ContinueWatchingItem) -> Unit,
    showContinueWatchingManualPlayOption: Boolean,
    onNavigateToCatalogSeeAll: (String, String, String) -> Unit,
    onNavigateToFolderDetail: (String, String) -> Unit = { _, _ -> },
    isCatalogItemWatched: (MetaPreview) -> Boolean,
    onCatalogItemLongPress: (MetaPreview, String) -> Unit,
    onTitaniumTileClick: (TitaniumTile) -> Unit
) {
    val scrollToTopTrigger by viewModel.scrollToTopTrigger.collectAsStateWithLifecycle()
    TitaniumHomeContent(
        uiState = uiState,
        posterCardStyle = posterCardStyle,
        scrollToTopTrigger = scrollToTopTrigger,
        onNavigateToDetail = onNavigateToDetail,
        onContinueWatchingClick = onContinueWatchingClick,
        onContinueWatchingStartFromBeginning = onContinueWatchingStartFromBeginning,
        onContinueWatchingPlayManually = onContinueWatchingPlayManually,
        showContinueWatchingManualPlayOption = showContinueWatchingManualPlayOption,
        onNavigateToCatalogSeeAll = onNavigateToCatalogSeeAll,
        onNavigateToFolderDetail = onNavigateToFolderDetail,
        onMarkContinueWatchingWatched = remember(viewModel) {
            { item -> viewModel.onEvent(HomeEvent.OnMarkContinueWatchingWatched(item)) }
        },
        onRemoveContinueWatching = remember(viewModel) {
            { contentId, season, episode, isNextUp ->
                viewModel.onEvent(HomeEvent.OnRemoveContinueWatching(contentId, season, episode, isNextUp))
            }
        },
        isCatalogItemWatched = isCatalogItemWatched,
        onCatalogItemLongPress = onCatalogItemLongPress,
        onTitaniumTileClick = onTitaniumTileClick
    )
}

@Composable
fun TitaniumHomeContent(
    uiState: HomeUiState,
    posterCardStyle: PosterCardStyle,
    onNavigateToDetail: (String, String, String) -> Unit,
    onContinueWatchingClick: (ContinueWatchingItem) -> Unit,
    onContinueWatchingStartFromBeginning: (ContinueWatchingItem) -> Unit = {},
    onContinueWatchingPlayManually: (ContinueWatchingItem) -> Unit = {},
    showContinueWatchingManualPlayOption: Boolean = false,
    onNavigateToCatalogSeeAll: (String, String, String) -> Unit,
    onNavigateToFolderDetail: (String, String) -> Unit = { _, _ -> },
    onRemoveContinueWatching: (String, Int?, Int?, Boolean) -> Unit,
    onMarkContinueWatchingWatched: (ContinueWatchingItem) -> Unit = {},
    isCatalogItemWatched: (MetaPreview) -> Boolean = { false },
    onCatalogItemLongPress: (MetaPreview, String) -> Unit = { _, _ -> },
    onTitaniumTileClick: (TitaniumTile) -> Unit = {},
    scrollToTopTrigger: Int = 0
) {
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            listState.scrollToItem(0, 0)
        }
    }

    // NOTE: no initial-focus request here. Auto-focusing the first category
    // tile on entry made the LazyColumn scroll the tile fully into view,
    // which pushed the hero band off the top of the screen (only a sliver
    // stayed visible on TV). The hero must be fully visible on entry; the
    // first D-pad press moves focus into the rows naturally.

    val visibleHomeRows = remember(uiState.homeRows, uiState.catalogRows) {
        if (uiState.homeRows.isNotEmpty()) {
            uiState.homeRows
        } else {
            uiState.catalogRows.filter { it.items.isNotEmpty() }.map { HomeRow.Catalog(it) }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(NuvioTheme.colors.Background),
        contentPadding = PaddingValues(bottom = 56.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        item(key = "titanium_hero", contentType = "titanium_hero") {
            TitaniumHeroHeader()
        }

        TitaniumSection.entries.forEach { section ->
            item(key = "titanium_row_${section.name}", contentType = "titanium_row") {
                TitaniumCategoryRow(
                    section = section,
                    onTileClick = onTitaniumTileClick
                )
            }
        }

        if (uiState.continueWatchingEnabled && uiState.continueWatchingItems.isNotEmpty()) {
            item(key = "continue_watching", contentType = "continue_watching") {
                ContinueWatchingSection(
                    items = remember(
                        uiState.continueWatchingItems,
                        uiState.customPosterUrlPattern,
                        uiState.customPosterEnabledScreens
                    ) {
                        uiState.continueWatchingItems.withCustomPosterUrls(
                            patternForScreen(
                                uiState.customPosterUrlPattern,
                                CustomPosterScreen.CONTINUE_WATCHING,
                                uiState.customPosterEnabledScreens
                            )
                        )
                    },
                    onItemClick = onContinueWatchingClick,
                    onStartFromBeginning = onContinueWatchingStartFromBeginning,
                    showManualPlayOption = showContinueWatchingManualPlayOption,
                    onPlayManually = onContinueWatchingPlayManually,
                    onDetailsClick = { item -> onNavigateToDetail(item.contentId(), item.contentType(), "") },
                    onMarkWatched = onMarkContinueWatchingWatched,
                    onRemoveItem = { item ->
                        onRemoveContinueWatching(
                            item.contentId(),
                            when (item) {
                                is ContinueWatchingItem.InProgress -> item.progress.season
                                is ContinueWatchingItem.NextUp -> item.info.seedSeason
                            },
                            when (item) {
                                is ContinueWatchingItem.InProgress -> item.progress.episode
                                is ContinueWatchingItem.NextUp -> item.info.seedEpisode
                            },
                            item is ContinueWatchingItem.NextUp
                        )
                    },
                    blurUnwatchedEpisodes = uiState.blurUnwatchedEpisodes,
                    useEpisodeThumbnails = uiState.useEpisodeThumbnailsInCw,
                    cardStyle = uiState.continueWatchingCardStyle,
                    cornerRadius = posterCardStyle.cornerRadius
                )
            }
        }

        if (uiState.continueWatchingEnabled && uiState.upcomingItems.isNotEmpty()) {
            item(key = "upcoming_section", contentType = "upcoming_section") {
                ContinueWatchingSection(
                    items = remember(
                        uiState.upcomingItems,
                        uiState.customPosterUrlPattern,
                        uiState.customPosterEnabledScreens
                    ) {
                        uiState.upcomingItems.withCustomPosterUrls(
                            patternForScreen(
                                uiState.customPosterUrlPattern,
                                CustomPosterScreen.CONTINUE_WATCHING,
                                uiState.customPosterEnabledScreens
                            )
                        )
                    },
                    title = stringResource(R.string.upcoming_section_title),
                    onItemClick = onContinueWatchingClick,
                    onStartFromBeginning = onContinueWatchingStartFromBeginning,
                    showManualPlayOption = showContinueWatchingManualPlayOption,
                    onPlayManually = onContinueWatchingPlayManually,
                    onDetailsClick = { item -> onNavigateToDetail(item.contentId(), item.contentType(), "") },
                    onMarkWatched = onMarkContinueWatchingWatched,
                    onRemoveItem = { item ->
                        onRemoveContinueWatching(
                            item.contentId(),
                            when (item) {
                                is ContinueWatchingItem.InProgress -> item.progress.season
                                is ContinueWatchingItem.NextUp -> item.info.seedSeason
                            },
                            when (item) {
                                is ContinueWatchingItem.InProgress -> item.progress.episode
                                is ContinueWatchingItem.NextUp -> item.info.seedEpisode
                            },
                            item is ContinueWatchingItem.NextUp
                        )
                    },
                    blurUnwatchedEpisodes = uiState.blurUnwatchedEpisodes,
                    useEpisodeThumbnails = uiState.useEpisodeThumbnailsInCw,
                    cardStyle = uiState.continueWatchingCardStyle,
                    cornerRadius = posterCardStyle.cornerRadius
                )
            }
        }

        itemsIndexed(
            items = visibleHomeRows,
            key = { index, row ->
                when (row) {
                    is HomeRow.Catalog -> "${row.row.stableKey()}_$index"
                    is HomeRow.CollectionRow -> "collection_${row.collection.id}"
                    is HomeRow.PlaceholderCatalog -> "${row.stableCatalogKey}_$index"
                }
            },
            contentType = { _, row ->
                when (row) {
                    is HomeRow.Catalog -> "catalog_row"
                    is HomeRow.CollectionRow -> "collection_row"
                    is HomeRow.PlaceholderCatalog -> "catalog_row"
                }
            }
        ) { _, homeRow ->
            when (homeRow) {
                is HomeRow.Catalog -> {
                    val catalogRow: CatalogRow = homeRow.row
                    CatalogRowSection(
                        catalogRow = catalogRow,
                        posterCardStyle = posterCardStyle,
                        showPosterLabels = uiState.posterLabelsEnabled,
                        showImdbRatings = uiState.homeImdbRatingsVisibility.showRatings,
                        showAddonName = uiState.catalogAddonNameEnabled,
                        showCatalogTypeSuffix = uiState.catalogTypeSuffixEnabled,
                        isItemWatched = isCatalogItemWatched,
                        onItemLongPress = onCatalogItemLongPress,
                        onItemClick = onNavigateToDetail,
                        onSeeAll = {
                            onNavigateToCatalogSeeAll(
                                catalogRow.catalogId,
                                catalogRow.addonId,
                                catalogRow.apiType
                            )
                        }
                    )
                }
                is HomeRow.CollectionRow -> {
                    CollectionRowSection(
                        collection = homeRow.collection,
                        onFolderClick = onNavigateToFolderDetail,
                        posterCardStyle = posterCardStyle
                    )
                }
                is HomeRow.PlaceholderCatalog -> {
                    // Matches Classic: placeholders render nothing until data lands.
                }
            }
        }
    }
}

@Composable
private fun TitaniumHeroHeader() {
    var clockText by remember { mutableStateOf(currentClockText()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000)
            clockText = currentClockText()
        }
    }
    val background = NuvioTheme.colors.Background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(302.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.titanium_hero_backdrop),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Fade the bottom of the hero into the screen background.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.55f to Color.Transparent,
                            1f to background
                        )
                    )
                )
        )
        Text(
            text = clockText,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 22.dp, end = 30.dp)
        )
    }
}

@Composable
private fun TitaniumCategoryRow(
    section: TitaniumSection,
    onTileClick: (TitaniumTile) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 48.dp, bottom = 14.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .width(5.dp)
                    .height(24.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF22D3EE), Color(0xFFE879F9))
                        ),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = NuvioTheme.colors.TextPrimary
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(section.tiles, key = { it.label }) { tile ->
                TitaniumTileCard(
                    tile = tile,
                    onClick = { onTileClick(tile) }
                )
            }
        }
    }
}

private fun currentClockText(): String =
    SimpleDateFormat("h:mm a", Locale.US).format(Date())
