package com.wortik.app.ui.screens.saved

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wortik.app.R
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.ui.components.GenderBadge
import com.wortik.app.ui.components.PronunciationButtons
import com.wortik.app.ui.components.WordListItem
import com.wortik.app.ui.components.groupedItemShape
import com.wortik.app.ui.components.rememberPronunciationController
import com.wortik.app.ui.theme.LocalGenderColors

@Composable
fun SavedScreen(
    viewModel: SavedViewModel,
    onWordClick: (Long) -> Unit,
    onVolumeMuted: () -> Unit,
    onVolumeRestored: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedWords by viewModel.savedWords.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val breakdown by viewModel.genderBreakdown.collectAsStateWithLifecycle()
    var showFlashcards by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.saved_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (savedWords.isNotEmpty()) {
                FilledTonalButton(
                    onClick = { showFlashcards = true },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Style,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(stringResource(R.string.saved_review))
                }
            }
        }

        if (breakdown.total > 0) {
            GenderRatioBar(breakdown = breakdown)
            Spacer(modifier = Modifier.height(6.dp))
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(SavedFilter.entries) { filter ->
                FilterChip(
                    selected = filter == selectedFilter,
                    onClick = { viewModel.selectFilter(filter) },
                    label = { Text(filter.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (savedWords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bookmarks,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = if (selectedFilter == SavedFilter.ALL) {
                            stringResource(R.string.saved_empty_all)
                        } else {
                            stringResource(R.string.saved_empty_filtered, selectedFilter.label)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(savedWords, key = { _, item -> item.id }) { index, entry ->
                    WordListItem(
                        entry = entry,
                        shape = groupedItemShape(index, savedWords.size),
                        onClick = { onWordClick(entry.id) }
                    )
                }
            }
        }
    }

    if (showFlashcards && savedWords.isNotEmpty()) {
        FlashcardReviewDialog(
            words = savedWords,
            onVolumeMuted = onVolumeMuted,
            onVolumeRestored = onVolumeRestored,
            onDismiss = { showFlashcards = false }
        )
    }
}

@Composable
private fun GenderRatioBar(breakdown: GenderBreakdown) {
    val colors = LocalGenderColors.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (breakdown.der > 0) {
                Box(
                    modifier = Modifier
                        .weight(breakdown.der.toFloat())
                        .fillMaxHeight()
                        .background(colors.der.onContainer.copy(alpha = 0.72f))
                )
            }
            if (breakdown.die > 0) {
                Box(
                    modifier = Modifier
                        .weight(breakdown.die.toFloat())
                        .fillMaxHeight()
                        .background(colors.die.onContainer.copy(alpha = 0.72f))
                )
            }
            if (breakdown.das > 0) {
                Box(
                    modifier = Modifier
                        .weight(breakdown.das.toFloat())
                        .fillMaxHeight()
                        .background(colors.das.onContainer.copy(alpha = 0.72f))
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "der ${breakdown.der}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.der.onContainer
            )
            Text(
                text = "die ${breakdown.die}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.die.onContainer
            )
            Text(
                text = "das ${breakdown.das}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.das.onContainer
            )
        }
    }
}

@Composable
private fun FlashcardReviewDialog(
    words: List<WordEntry>,
    onVolumeMuted: () -> Unit,
    onVolumeRestored: () -> Unit,
    onDismiss: () -> Unit
) {
    var deck by remember(words) { mutableStateOf(words) }
    var index by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    val safeIndex = index.coerceIn(deck.indices)
    val current = deck[safeIndex]
    val haptic = LocalHapticFeedback.current
    val pronunciation = rememberPronunciationController(
        onVolumeMuted = onVolumeMuted,
        onVolumeRestored = onVolumeRestored
    )
    val genderColors = LocalGenderColors.current.forGender(current.gender)

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "flashcardRotation"
    )
    val progress by animateFloatAsState(
        targetValue = (safeIndex + 1).toFloat() / deck.size.toFloat(),
        animationSpec = tween(durationMillis = 220),
        label = "flashcardProgress"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${safeIndex + 1} / ${deck.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (deck.size > 1) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    deck = deck.shuffled()
                                    index = 0
                                    flipped = false
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = stringResource(R.string.flashcard_shuffle_cd),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.flashcard_close_cd),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { progress },
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                )

                val cardSurfaceColor = genderColors?.container?.copy(alpha = 0.28f)
                    ?: MaterialTheme.colorScheme.surfaceContainerLowest

                val wordStyle = when {
                    current.word.length > 22 -> MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    current.word.length > 16 -> MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    current.word.length > 12 -> MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 23.sp,
                        lineHeight = 29.sp
                    )
                    else -> MaterialTheme.typography.headlineMedium
                }

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = cardSurfaceColor,
                    border = BorderStroke(
                        width = 1.dp,
                        color = (genderColors?.onContainer ?: MaterialTheme.colorScheme.outlineVariant)
                            .copy(alpha = 0.14f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(242.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            flipped = !flipped
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotation <= 90f) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GenderBadge(
                                    gender = current.gender,
                                    partOfSpeech = current.partOfSpeech
                                )
                                Text(
                                    text = current.word,
                                    style = wordStyle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                PronunciationButtons(
                                    hasNativeAudio = !current.audioUrl.isNullOrBlank(),
                                    onPlayNative = {
                                        current.audioUrl?.let { pronunciation.playNativeAudio(it) }
                                    },
                                    onPlayTts = { pronunciation.speakEntryTts(current) },
                                    compact = true,
                                    isPlayingNative = pronunciation.isPlayingNative,
                                    isSpeakingTts = pronunciation.isSpeakingTts,
                                    containerColor = genderColors?.container?.copy(alpha = 0.75f)
                                        ?: MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = genderColors?.onContainer
                                        ?: MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.flashcard_tap_to_flip),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.graphicsLayer { rotationY = 180f },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = current.meanings.take(3).joinToString(" · "),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                                current.plural?.let { plural ->
                                    Text(
                                        text = "Pl: $plural",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            flipped = false
                            index = if (safeIndex > 0) safeIndex - 1 else deck.lastIndex
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.flashcard_prev))
                    }
                    Button(
                        onClick = {
                            flipped = false
                            index = (safeIndex + 1) % deck.size
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.flashcard_next))
                    }
                }
            }
        }
    }
}
