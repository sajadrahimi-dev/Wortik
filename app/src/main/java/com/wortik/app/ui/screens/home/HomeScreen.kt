package com.wortik.app.ui.screens.home

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wortik.app.R
import com.wortik.app.data.remote.TranslationResult
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.ui.components.GenderBadge
import com.wortik.app.ui.components.PronunciationButtons
import com.wortik.app.ui.components.UmlautStrip
import com.wortik.app.ui.components.WordListItem
import com.wortik.app.ui.components.groupedItemShape
import com.wortik.app.ui.components.rememberPronunciationController
import com.wortik.app.ui.theme.LocalGenderColors

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    isSearchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    externalQuery: String?,
    onExternalQueryConsumed: () -> Unit,
    onWordClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onVolumeMuted: () -> Unit,
    onVolumeRestored: () -> Unit,
    modifier: Modifier = Modifier
) {
    val queryField by viewModel.queryField.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val translationState by viewModel.translationState.collectAsStateWithLifecycle()
    val shouldFocusSearch by viewModel.shouldFocusSearch.collectAsStateWithLifecycle()
    val wordOfTheDay by viewModel.wordOfTheDay.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val voicePrompt = stringResource(R.string.search_voice_prompt)
    val pronunciation = rememberPronunciationController(
        onVolumeMuted = onVolumeMuted,
        onVolumeRestored = onVolumeRestored
    )

    val voiceSearchAvailable = remember(context) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.resolveActivity(context.packageManager) != null
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
            if (!spoken.isNullOrEmpty()) {
                onSearchActiveChange(true)
                viewModel.setQueryText(spoken)
                keyboardController?.hide()
            }
        }
    }

    fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
            putExtra(RecognizerIntent.EXTRA_PROMPT, voicePrompt)
        }
        runCatching { voiceLauncher.launch(intent) }
    }

    LaunchedEffect(externalQuery) {
        if (!externalQuery.isNullOrBlank()) {
            onSearchActiveChange(true)
            viewModel.setQueryText(externalQuery)
            onExternalQueryConsumed()
        }
    }

    BackHandler(enabled = isSearchActive) {
        onSearchActiveChange(false)
        viewModel.onSearchActiveChanged(false)
        keyboardController?.hide()
    }

    LaunchedEffect(shouldFocusSearch) {
        if (shouldFocusSearch) {
            viewModel.moveCursorToEnd()
            focusRequester.requestFocus()
            viewModel.consumeFocusRequest()
        }
    }

    LaunchedEffect(queryField.text) {
        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
            listState.scrollToItem(0)
        }
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val centerOffset = remember(screenHeight) {
        (screenHeight * 0.26f).coerceAtLeast(136.dp)
    }
    val motionSpec = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = 0.88f,
        stiffness = Spring.StiffnessMediumLow
    )
    val searchTopPadding by animateDpAsState(
        targetValue = if (isSearchActive) 10.dp else centerOffset,
        animationSpec = motionSpec,
        label = "searchBarTopOffset"
    )
    val searchHorizontalPadding by animateDpAsState(
        targetValue = if (isSearchActive) 16.dp else 28.dp,
        animationSpec = motionSpec,
        label = "searchBarHorizontalPadding"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 12.dp)
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn() + scaleIn(initialScale = 0.92f) + slideInVertically { -it / 4 },
            exit = fadeOut() + scaleOut(targetScale = 0.92f) + slideOutVertically { -it / 4 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (centerOffset - 78.dp).coerceAtLeast(52.dp))
        ) {
            WortikWordmark()
        }

        AnimatedVisibility(
            visible = !isSearchActive && wordOfTheDay != null,
            enter = fadeIn() + slideInVertically { it / 8 },
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = centerOffset + 80.dp, start = 28.dp, end = 28.dp)
        ) {
            wordOfTheDay?.let { dailyWord ->
                WordOfTheDayCard(
                    entry = dailyWord,
                    onClick = { onWordClick(dailyWord.id) },
                    onRollRandom = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.rollRandomWord()
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = searchTopPadding)
                .padding(horizontal = searchHorizontalPadding)
        ) {

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isSearchActive) 4.dp else 2.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(enabled = !isSearchActive) {
                        onSearchActiveChange(true)
                        viewModel.onSearchActiveChanged(true)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            onSearchActiveChange(false)
                            viewModel.onSearchActiveChanged(false)
                            keyboardController?.hide()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.search_close_cd),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.padding(start = 12.dp, end = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (queryField.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_placeholder),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isSearchActive) {
                            BasicTextField(
                                value = queryField,
                                onValueChange = viewModel::onQueryFieldChange,
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = { keyboardController?.hide() }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                        }
                    }

                    if (isSearchActive && queryField.text.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearQuery) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.search_clear_cd),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (voiceSearchAvailable) {
                        IconButton(onClick = ::launchVoiceSearch) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = stringResource(R.string.search_voice_cd),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically { it / 10 },
                exit = fadeOut(),
                modifier = Modifier
                    .weight(1f)
                    .imePadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    UmlautStrip(
                        onCharacterClick = viewModel::appendCharacter
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val showEmptyMessage = queryField.text.isNotBlank() &&
                        results.isEmpty() &&
                        translationState is TranslationUiState.Unavailable

                    if (showEmptyMessage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.search_no_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            when (val tState = translationState) {
                                is TranslationUiState.Loading -> {
                                    item(key = "translation_loading") {
                                        Surface(
                                            shape = RoundedCornerShape(22.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp
                                                )
                                                Text(
                                                    text = stringResource(R.string.translation_loading),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                is TranslationUiState.Success -> {
                                    item(key = "translation_result") {
                                        OnlineTranslationCard(
                                            result = tState.result,
                                            isSpeakingTts = pronunciation.isSpeakingTts,
                                            onPlayTts = {
                                                pronunciation.speakTts(tState.result.germanText)
                                            },
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )
                                    }
                                }

                                else -> Unit
                            }

                            itemsIndexed(results, key = { _, item -> item.id }) { index, entry ->
                                WordListItem(
                                    entry = entry,
                                    shape = groupedItemShape(index, results.size),
                                    highlightQuery = queryField.text,
                                    onClick = { onWordClick(entry.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnlineTranslationCard(
    result: TranslationResult,
    isSpeakingTts: Boolean,
    onPlayTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val langBadge = "${result.sourceLang.uppercase()} → ${result.targetLang.uppercase()}"

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.translation_card_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = langBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            SelectionContainer {
                Text(
                    text = result.translatedText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PronunciationButtons(
                    hasNativeAudio = false,
                    onPlayNative = {},
                    onPlayTts = onPlayTts,
                    compact = true,
                    isSpeakingTts = isSpeakingTts
                )

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        clipboard.setText(AnnotatedString(result.translatedText))
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = stringResource(R.string.translation_copy_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WordOfTheDayCard(
    entry: WordEntry,
    onClick: () -> Unit,
    onRollRandom: () -> Unit
) {
    val genderColors = LocalGenderColors.current.forGender(entry.gender)
    val cardColor = genderColors?.container?.copy(alpha = 0.28f)
        ?: MaterialTheme.colorScheme.surfaceContainerLow

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = cardColor,
        border = BorderStroke(
            width = 1.dp,
            color = (genderColors?.onContainer ?: MaterialTheme.colorScheme.outlineVariant)
                .copy(alpha = 0.12f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = genderColors?.onContainer ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stringResource(R.string.word_of_the_day),
                        style = MaterialTheme.typography.labelMedium,
                        color = genderColors?.onContainer ?: MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onRollRandom,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Casino,
                            contentDescription = stringResource(R.string.random_word_cd),
                            tint = genderColors?.onContainer ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GenderBadge(
                    gender = entry.gender,
                    partOfSpeech = entry.partOfSpeech
                )
                Text(
                    text = entry.word,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (entry.meanings.isNotEmpty()) {
                Text(
                    text = entry.meanings.take(2).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WortikWordmark() {
    val genderColors = LocalGenderColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(genderColors.der.onContainer.copy(alpha = 0.75f))
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(genderColors.die.onContainer.copy(alpha = 0.75f))
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(genderColors.das.onContainer.copy(alpha = 0.75f))
            )
        }
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 38.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-1).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
