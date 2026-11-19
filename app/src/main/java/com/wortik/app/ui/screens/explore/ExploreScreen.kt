package com.wortik.app.ui.screens.explore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsTransit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wortik.app.R
import com.wortik.app.data.local.WordCategory
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.ui.components.WordListItem
import com.wortik.app.ui.components.groupedItemShape

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onWordClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val categorySearchQuery by viewModel.categorySearchQuery.collectAsStateWithLifecycle()
    val filteredCategories by viewModel.filteredCategories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categoryWords by viewModel.categoryWords.collectAsStateWithLifecycle()
    val isLoadingCategory by viewModel.isLoadingCategory.collectAsStateWithLifecycle()
    val selectedLetter by viewModel.selectedLetter.collectAsStateWithLifecycle()
    val letterWords by viewModel.letterWords.collectAsStateWithLifecycle()
    val isLoadingLetter by viewModel.isLoadingLetter.collectAsStateWithLifecycle()

    BackHandler(enabled = selectedCategory != null) {
        viewModel.closeCategory()
    }

    val activeCategory = selectedCategory
    if (activeCategory != null) {
        CategoryDetailContent(
            category = activeCategory,
            words = categoryWords,
            isLoading = isLoadingCategory,
            onBackClick = viewModel::closeCategory,
            onWordClick = onWordClick,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.explore_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val tabs = listOf(
                ExploreMode.CATEGORIES to stringResource(R.string.explore_tab_categories),
                ExploreMode.ALPHABET to stringResource(R.string.explore_tab_az)
            )
            tabs.forEachIndexed { index, (tabMode, label) ->
                SegmentedButton(
                    selected = mode == tabMode,
                    onClick = { viewModel.selectMode(tabMode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size)
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (mode) {
            ExploreMode.CATEGORIES -> {

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (categorySearchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.explore_search_categories),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = categorySearchQuery,
                                onValueChange = viewModel::onCategorySearchChange,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (categorySearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onCategorySearchChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.search_clear_cd),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item(key = "group_all") {
                        FilterChip(
                            selected = selectedGroupId == null,
                            onClick = { viewModel.selectGroup(null) },
                            label = { Text(stringResource(R.string.explore_all_groups)) }
                        )
                    }
                    items(viewModel.groups, key = { it.id }) { group ->
                        FilterChip(
                            selected = selectedGroupId == group.id,
                            onClick = { viewModel.selectGroup(group.id) },
                            label = { Text(group.title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredCategories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.explore_no_categories),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 110.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredCategories, key = { it.id }) { category ->
                            CategoryCard(
                                category = category,
                                onClick = { viewModel.openCategory(category) }
                            )
                        }
                    }
                }
            }

            ExploreMode.ALPHABET -> {
                AlphabetBrowseContent(
                    letters = viewModel.letters,
                    selectedLetter = selectedLetter,
                    words = letterWords,
                    isLoading = isLoadingLetter,
                    onSelectLetter = viewModel::selectLetter,
                    onLoadMore = viewModel::loadMoreForSelectedLetter,
                    onWordClick = onWordClick
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: WordCategory,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = categoryIconFor(category.id),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Text(
                    text = category.germanTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Text(
                text = stringResource(R.string.explore_words_count, category.words.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoryDetailContent(
    category: WordCategory,
    words: List<WordEntry>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onWordClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back_cd)
                )
            }
            Column {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category.germanTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isLoading && words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(words, key = { _, item -> item.id }) { index, entry ->
                    WordListItem(
                        entry = entry,
                        shape = groupedItemShape(index, words.size),
                        onClick = { onWordClick(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlphabetBrowseContent(
    letters: List<String>,
    selectedLetter: String,
    words: List<WordEntry>,
    isLoading: Boolean,
    onSelectLetter: (String) -> Unit,
    onLoadMore: () -> Unit,
    onWordClick: (Long) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedLetter) {
        listState.scrollToItem(0)
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 12
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(letters, key = { it }) { letter ->
                val selected = letter == selectedLetter
                Surface(
                    onClick = { onSelectLetter(letter) },
                    shape = CircleShape,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading && words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.explore_no_words_letter, selectedLetter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(words, key = { _, item -> item.id }) { index, entry ->
                    WordListItem(
                        entry = entry,
                        shape = groupedItemShape(index, words.size),
                        onClick = { onWordClick(entry.id) }
                    )
                }

                if (isLoading) {
                    item(key = "loading_more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

private fun groupIcon(groupId: String): ImageVector = when (groupId) {
    "essentials" -> Icons.Rounded.AutoAwesome
    "daily_life" -> Icons.Rounded.Home
    "food_drink" -> Icons.Rounded.Restaurant
    "people_society" -> Icons.Rounded.Groups
    "travel_places" -> Icons.Rounded.DirectionsTransit
    "nature_animals" -> Icons.Rounded.WbSunny
    "work_study_tech" -> Icons.Rounded.Work
    "health_sports" -> Icons.Rounded.Favorite
    "culture_leisure" -> Icons.Rounded.Palette
    "law_politics_abstract" -> Icons.Rounded.Balance
    else -> Icons.Rounded.AutoAwesome
}
