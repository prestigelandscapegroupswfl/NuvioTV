package com.nuvio.tv.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.nuvio.tv.LocalContentFocusRequester
import com.nuvio.tv.ui.components.BrandWordmark
import com.nuvio.tv.ui.components.TitaniumAnimatedCover
import com.nuvio.tv.ui.components.titaniumCoverFor
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
import androidx.compose.ui.platform.LocalContext
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
import com.nuvio.tv.ui.theme.NuvioTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

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
    onTitaniumTileClick: (TitaniumHomeSection, String) -> Unit
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
        onTitaniumTileClick = onTitaniumTileClick,
        availableCollectionCatalogs = {
            viewModel.addonsCache.asSequence().filter { it.enabled }.flatMap { addon ->
                addon.catalogs.asSequence().map { catalog -> Triple(addon.id, catalog.id, catalog.apiType) }
            }.toSet()
        }
    )
}

@Composable
internal fun TitaniumHomeContent(
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
    onTitaniumTileClick: (TitaniumHomeSection, String) -> Unit = { _, _ -> },
    scrollToTopTrigger: Int = 0,
    availableCollectionCatalogs: () -> Set<Triple<String, String, String>> = { emptySet() }
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val collectionArtwork = remember(context) { TitaniumCollectionArtwork.load(context) }

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            listState.scrollToItem(0, 0)
        }
    }

    val visibleHomeRows = remember(uiState.homeRows, uiState.catalogRows) {
        if (uiState.homeRows.isNotEmpty()) {
            uiState.homeRows
        } else {
            uiState.catalogRows.filter { it.items.isNotEmpty() }.map { HomeRow.Catalog(it) }
        }
    }

    val contentFocusRequester = LocalContentFocusRequester.current
    BoxWithConstraints(Modifier.fillMaxSize().background(NuvioTheme.colors.Background)) {
        val coverWidth = ((maxWidth - 78.dp) / 4f).coerceAtLeast(92.dp)
        val coverHeight = coverWidth / 2.25f
        val heroHeight = maxHeight * 0.30f
        Column(Modifier.fillMaxSize()) {
            TitaniumHeroHeader(height = heroHeight)
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .focusRequester(contentFocusRequester).focusGroup(),
                contentPadding = PaddingValues(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TitaniumHomeSection.entries.forEach { section ->
                    item(key = "titanium_row_${section.name}", contentType = "titanium_row") {
                        TitaniumCategoryRow(section, coverWidth, coverHeight) { title ->
                            val names = titaniumTitleAliases(title)
                            val availableCatalogs = availableCollectionCatalogs()
                            val folder = visibleHomeRows.asSequence()
                                .filterIsInstance<HomeRow.CollectionRow>()
                                .flatMap { entry -> entry.collection.folders.asSequence().map { entry.collection.id to it } }
                                .firstOrNull { (_, folder) ->
                                    folder.title.trim().lowercase() in names && folder.sources.isNotEmpty() &&
                                        (folder.catalogSources.isEmpty() ||
                                            folder.catalogSources.any { Triple(it.addonId, it.catalogId, it.type) in availableCatalogs })
                                }
                            if (folder != null) onNavigateToFolderDetail(folder.first, folder.second.id)
                            else onTitaniumTileClick(section, title)
                        }
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
                            val styledCollection = remember(homeRow.collection, collectionArtwork) {
                                collectionArtwork.applyTo(homeRow.collection)
                            }
                            CollectionRowSection(
                                collection = styledCollection,
                                onFolderClick = onNavigateToFolderDetail,
                                posterCardStyle = posterCardStyle,
                                animateStillCovers = true
                            )
                        }
                        is HomeRow.PlaceholderCatalog -> {
                            // Matches Classic: placeholders render nothing until data lands.
                        }
                    }
                }
            }
        }
    }
}

internal enum class TitaniumHomeSection(val displayName: String, val titles: List<String>) {
    GENRES("GENRES", listOf("Action", "Sci-Fi", "Horror", "Fantasy", "Western", "Comedy", "Adventure", "Crime", "Drama", "Romance")),
    COLLECTIONS("COLLECTIONS", listOf("Marvel", "DC", "Star Wars", "Jurassic Park")),
    DECADES("DECADES", listOf("70s", "80s", "90s", "2000s"))
}

private fun titaniumTitleAliases(title: String): Set<String> = when (title) {
    "Sci-Fi" -> setOf("sci-fi", "sci fi", "science fiction")
    "Western" -> setOf("western", "westerns")
    "Marvel" -> setOf("marvel", "marvel mcu", "marvel collection")
    "DC" -> setOf("dc", "dc comics", "dc collection")
    "70s", "80s", "90s" -> setOf(title, "19$title")
    else -> setOf(title.lowercase(), "${title.lowercase()} collection")
}

@Composable
private fun TitaniumHeroHeader(height: Dp) {
    var clockText by remember { mutableStateOf(currentClockText()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000)
            clockText = currentClockText()
        }
    }
    val background = NuvioTheme.colors.Background
    Box(Modifier.fillMaxWidth().height(height)) {
        Image(
            painter = painterResource(R.drawable.titanium_cinema_panorama),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(0.56f to Color.Transparent, 1f to background)
        ))
        BrandWordmark(
            contentDescription = "Titanium X",
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.64f).height(height * 0.62f)
        )
        Text(
            text = clockText,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 22.dp)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TitaniumCategoryRow(
    section: TitaniumHomeSection,
    width: Dp,
    height: Dp,
    onTileClick: (String) -> Unit
) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF33DDFF), Color(0xFF9657FF), Color(0xFFED5DDD)))
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 24.dp, bottom = 5.dp)
        ) {
            Box(Modifier.width(4.dp).height(15.dp).background(gradient, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(8.dp))
            Text(
                text = section.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(section.titles, key = { it }) { title ->
                val cover = remember(title) { titaniumCoverFor(title) }
                if (cover != null) {
                    var focused by remember { mutableStateOf(false) }
                    val shape = RoundedCornerShape(8.dp)
                    Card(
                        onClick = { onTileClick(title) },
                        modifier = Modifier.width(width).height(height)
                            .onFocusChanged { focused = it.hasFocus }
                            .border(if (focused) 2.dp else 1.dp, gradient, shape)
                            .semantics { contentDescription = title },
                        shape = CardDefaults.shape(shape = shape),
                        scale = CardDefaults.scale(focusedScale = 1.035f)
                    ) {
                        TitaniumAnimatedCover(cover, focused)
                    }
                }
            }
        }
    }
}

private fun currentClockText(): String =
    SimpleDateFormat("h:mm a", Locale.US).format(Date())
