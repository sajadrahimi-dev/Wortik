package com.wortik.app.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wortik.app.R
import com.wortik.app.data.remote.WordImage
import com.wortik.app.domain.model.Gender
import com.wortik.app.domain.model.NounDeclension
import com.wortik.app.domain.model.VerbConjugation
import com.wortik.app.domain.model.VerbForms
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.ui.components.GenderBadge
import com.wortik.app.ui.components.GermanFlagIcon
import com.wortik.app.ui.components.PronunciationButtons
import com.wortik.app.ui.components.rememberPronunciationController
import com.wortik.app.ui.theme.LocalGenderColors

@Composable
fun WordDetailScreen(
    viewModel: WordDetailViewModel,
    onBackClick: () -> Unit,
    onWordClick: (Long) -> Unit,
    onVolumeMuted: () -> Unit,
    onVolumeRestored: () -> Unit,
    modifier: Modifier = Modifier
) {
    val word by viewModel.word.collectAsStateWithLifecycle()
    val wordImage by viewModel.wordImage.collectAsStateWithLifecycle()
    val relatedWords by viewModel.relatedWords.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val pronunciation = rememberPronunciationController(
        onVolumeMuted = onVolumeMuted,
        onVolumeRestored = onVolumeRestored
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back_cd)
                )
            }

            word?.let { entry ->
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.toggleSaved()
                }) {
                    Icon(
                        imageVector = if (entry.isSaved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(
                            if (entry.isSaved) R.string.bookmark_remove_cd else R.string.bookmark_add_cd
                        ),
                        tint = if (entry.isSaved) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        val entry = word
        if (entry == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            WordDetailContent(
                entry = entry,
                wordImage = wordImage,
                relatedWords = relatedWords,
                isPlayingNative = pronunciation.isPlayingNative,
                isSpeakingTts = pronunciation.isSpeakingTts,
                slowSpeed = pronunciation.slowSpeed,
                onToggleSlowSpeed = pronunciation::toggleSlowSpeed,
                onPlayNativeAudio = {
                    entry.audioUrl?.let { url ->
                        pronunciation.playNativeAudio(url)
                    }
                },
                onPlayTts = { pronunciation.speakEntryTts(entry) },
                onSpeakSentenceTts = pronunciation::speakTts,
                onRelatedWordClick = onWordClick
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordDetailContent(
    entry: WordEntry,
    wordImage: WordImage?,
    relatedWords: List<WordEntry>,
    isPlayingNative: Boolean,
    isSpeakingTts: Boolean,
    slowSpeed: Boolean,
    onToggleSlowSpeed: () -> Unit,
    onPlayNativeAudio: () -> Unit,
    onPlayTts: () -> Unit,
    onSpeakSentenceTts: (String) -> Unit,
    onRelatedWordClick: (Long) -> Unit
) {
    val genderColors = LocalGenderColors.current.forGender(entry.gender)
    val surfaceLow = MaterialTheme.colorScheme.surfaceContainerLow

    val titleStyle = when {
        entry.word.length > 24 -> MaterialTheme.typography.titleLarge.copy(
            fontSize = 19.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.3).sp
        )
        entry.word.length > 19 -> MaterialTheme.typography.headlineSmall.copy(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.25).sp
        )
        entry.word.length > 14 -> MaterialTheme.typography.headlineMedium.copy(
            fontSize = 26.sp,
            lineHeight = 32.sp
        )
        else -> MaterialTheme.typography.displaySmall
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        item(key = "hero_header") {
            val heroBrush = if (genderColors != null) {
                Brush.verticalGradient(
                    colors = listOf(
                        genderColors.container.copy(alpha = 0.55f),
                        surfaceLow
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainer,
                        surfaceLow
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = surfaceLow,
                border = BorderStroke(
                    width = 1.dp,
                    color = (genderColors?.onContainer ?: MaterialTheme.colorScheme.outlineVariant)
                        .copy(alpha = 0.14f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroBrush)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GenderBadge(
                            gender = entry.gender,
                            partOfSpeech = entry.partOfSpeech
                        )
                        val genderLabel = when (entry.gender) {
                            Gender.DER -> "Maskulin · ${entry.partOfSpeech.label}"
                            Gender.DIE -> "Feminin · ${entry.partOfSpeech.label}"
                            Gender.DAS -> "Neutrum · ${entry.partOfSpeech.label}"
                            Gender.NONE -> entry.partOfSpeech.label
                        }
                        Text(
                            text = genderLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    SelectionContainer {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = entry.word,
                                style = titleStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2
                            )

                            if (entry.ipa != null || entry.plural != null) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    entry.ipa?.let { ipa ->
                                        Text(
                                            text = ipa,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    entry.plural?.let { plural ->
                                        Text(
                                            text = "Pl. $plural",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PronunciationButtons(
                        hasNativeAudio = !entry.audioUrl.isNullOrBlank(),
                        onPlayNative = onPlayNativeAudio,
                        onPlayTts = onPlayTts,
                        isPlayingNative = isPlayingNative,
                        isSpeakingTts = isSpeakingTts,
                        slowSpeed = slowSpeed,
                        onToggleSlowSpeed = onToggleSlowSpeed,
                        containerColor = genderColors?.container?.copy(alpha = 0.7f)
                            ?: MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = genderColors?.onContainer
                            ?: MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item(key = "word_photo") {
            AnimatedVisibility(
                visible = wordImage != null,
                enter = fadeIn(animationSpec = tween(280)) + expandVertically(animationSpec = tween(280))
            ) {
                val photo = wordImage ?: return@AnimatedVisibility
                val context = LocalContext.current
                val imageRequest = remember(photo.url) {
                    ImageRequest.Builder(context)
                        .data(photo.url)
                        .addHeader("User-Agent", "WortikAndroid/0.1 (https://github.com/wortik)")
                        .crossfade(true)
                        .build()
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = entry.word,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                    Text(
                        text = photo.attribution,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        if (entry.meanings.isNotEmpty()) {
            item(key = "meanings") {
                DetailSectionCard(title = stringResource(R.string.section_meanings)) {
                    SelectionContainer {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            entry.meanings.forEachIndexed { index, meaning ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = meaning,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        entry.nounDeclension?.let { declension ->
            item(key = "declension") {
                DetailSectionCard(title = stringResource(R.string.section_declension)) {
                    NounDeclensionSection(declension)
                }
            }
        }

        entry.verbConjugation?.let { conjugation ->
            item(key = "conjugation") {
                DetailSectionCard(title = stringResource(R.string.section_conjugation)) {
                    VerbConjugationSection(conjugation)
                }
            }
        }

        if (entry.examples.isNotEmpty()) {
            item(key = "examples") {
                DetailSectionCard(title = stringResource(R.string.section_examples)) {
                    val ttsSentenceCd = stringResource(R.string.pronounce_example_cd)
                    val haptic = LocalHapticFeedback.current

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        entry.examples.forEachIndexed { index, example ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SelectionContainer {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = example.german,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = example.english,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSpeakSentenceTts(example.german)
                                    },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    ),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .semantics {
                                            contentDescription = ttsSentenceCd
                                            role = Role.Button
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.pronounce_tts_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (relatedWords.isNotEmpty()) {
            item(key = "related_words") {
                DetailSectionCard(title = stringResource(R.string.section_related)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        relatedWords.forEach { related ->
                            val relGender = LocalGenderColors.current.forGender(related.gender)
                            Surface(
                                onClick = { onRelatedWordClick(related.id) },
                                shape = CircleShape,
                                color = relGender?.container?.copy(alpha = 0.45f)
                                    ?: MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = (relGender?.onContainer ?: MaterialTheme.colorScheme.outlineVariant)
                                        .copy(alpha = 0.16f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (related.gender.article.isNotEmpty()) {
                                        Text(
                                            text = related.gender.article,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = relGender?.onContainer ?: MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = related.word,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun NounDeclensionSection(declension: NounDeclension) {
    val numbers = listOf("Singular", "Plural")
    var selectedTab by remember { mutableIntStateOf(0) }

    val rows = if (selectedTab == 0) {
        listOf(
            "Nom · Subject" to declension.nominativSg,
            "Gen · Possessive" to declension.genitivSg,
            "Dat · Indirect (to/for)" to declension.dativSg,
            "Akk · Direct object" to declension.akkusativSg
        )
    } else {
        listOf(
            "Nom · Subject" to declension.nominativPl,
            "Gen · Possessive" to declension.genitivPl,
            "Dat · Indirect (to/for)" to declension.dativPl,
            "Akk · Direct object" to declension.akkusativPl
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            numbers.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = numbers.size)
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { (kasus, form) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = kasus,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = form,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (form.length > 22) 12.5.sp else 14.sp
                            ),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerbConjugationSection(conjugation: VerbConjugation) {
    val tenses = listOf(
        "Present" to "Präsens",
        "Past" to "Präteritum",
        "Perfect" to "Perfekt"
    )
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            tenses.forEachIndexed { index, (enLabel, deLabel) ->
                SegmentedButton(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tenses.size)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(enLabel, style = MaterialTheme.typography.labelMedium)
                        Text(
                            deLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        SelectionContainer {
            when (selectedTab) {
                0 -> VerbFormsGrid(conjugation.praesens)
                1 -> VerbFormsGrid(conjugation.praeteritum)
                2 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Helping verb: ${conjugation.auxiliary}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${conjugation.auxiliary} ${conjugation.partizipII}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerbFormsGrid(forms: VerbForms) {
    val pairs = listOf(
        "ich (I)" to forms.ich,
        "du (you)" to forms.du,
        "er/sie/es (he/she/it)" to forms.erSieEs,
        "wir (we)" to forms.wir,
        "ihr (you pl.)" to forms.ihr,
        "sie/Sie (they/You)" to forms.sieSie
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pairs.forEach { (pronoun, form) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pronoun,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = form,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
