package com.wortik.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wortik.app.R
import com.wortik.app.domain.model.TranslationHistoryEntry
import com.wortik.app.ui.components.WordListItem
import com.wortik.app.ui.components.groupedItemShape
import com.wortik.app.ui.components.rememberPronunciationController
import java.util.Calendar

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onWordClick: (Long) -> Unit,
    onTranslationClick: (String) -> Unit,
    onVolumeMuted: () -> Unit,
    onVolumeRestored: () -> Unit,
    modifier: Modifier = Modifier
) {
    val history by viewModel.historyItems.collectAsStateWithLifecycle()
    var showClearConfirm by remember { mutableStateOf(false) }
    val pronunciation = rememberPronunciationController(
        onVolumeMuted = onVolumeMuted,
        onVolumeRestored = onVolumeRestored
    )

    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayItems = remember(history, startOfToday) {
        history.filter { it.timestamp >= startOfToday }
    }
    val earlierItems = remember(history, startOfToday) {
        history.filter { it.timestamp < startOfToday }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (history.isNotEmpty()) {
                TextButton(onClick = { showClearConfirm = true }) {
                    Text(stringResource(R.string.history_clear))
                }
            }
        }

        if (history.isEmpty()) {
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
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = stringResource(R.string.history_empty),
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
                if (todayItems.isNotEmpty()) {
                    item(key = "header_today") {
                        SectionHeader(stringResource(R.string.history_today))
                    }
                    itemsIndexed(todayItems, key = { _, it -> "today_${it.stableKey}" }) { index, item ->
                        SwipeableHistoryItem(
                            shape = groupedItemShape(index, todayItems.size),
                            onDelete = { viewModel.removeItem(item) }
                        ) {
                            when (item) {
                                is HistoryItem.Word -> WordListItem(
                                    entry = item.entry,
                                    shape = groupedItemShape(index, todayItems.size),
                                    onClick = { onWordClick(item.entry.id) }
                                )
                                is HistoryItem.Translation -> TranslationHistoryListItem(
                                    entry = item.entry,
                                    shape = groupedItemShape(index, todayItems.size),
                                    onClick = { onTranslationClick(item.entry.sourceText) },
                                    onSpeak = { pronunciation.speakTts(item.entry.germanText) }
                                )
                            }
                        }
                    }
                }

                if (earlierItems.isNotEmpty()) {
                    item(key = "header_earlier") {
                        Spacer(modifier = Modifier.height(10.dp))
                        SectionHeader(stringResource(R.string.history_earlier))
                    }
                    itemsIndexed(earlierItems, key = { _, it -> "earlier_${it.stableKey}" }) { index, item ->
                        SwipeableHistoryItem(
                            shape = groupedItemShape(index, earlierItems.size),
                            onDelete = { viewModel.removeItem(item) }
                        ) {
                            when (item) {
                                is HistoryItem.Word -> WordListItem(
                                    entry = item.entry,
                                    shape = groupedItemShape(index, earlierItems.size),
                                    onClick = { onWordClick(item.entry.id) }
                                )
                                is HistoryItem.Translation -> TranslationHistoryListItem(
                                    entry = item.entry,
                                    shape = groupedItemShape(index, earlierItems.size),
                                    onClick = { onTranslationClick(item.entry.sourceText) },
                                    onSpeak = { pronunciation.speakTts(item.entry.germanText) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.history_clear_dialog_title)) },
            text = { Text(stringResource(R.string.history_clear_dialog_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        viewModel.clearHistory()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.history_clear),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun TranslationHistoryListItem(
    entry: TranslationHistoryEntry,
    shape: Shape,
    onClick: () -> Unit,
    onSpeak: () -> Unit
) {
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                        .size(16.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = entry.sourceText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.translatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onSpeak,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.RecordVoiceOver,
                    contentDescription = stringResource(R.string.pronounce_tts_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableHistoryItem(
    shape: Shape,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.58f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.history_delete_cd),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        content = { content() }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    )
}
