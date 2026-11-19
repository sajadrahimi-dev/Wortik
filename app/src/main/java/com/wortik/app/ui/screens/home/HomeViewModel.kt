package com.wortik.app.ui.screens.home

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortik.app.data.local.UserPreferences
import com.wortik.app.data.remote.OnlineTranslationService
import com.wortik.app.data.remote.TranslationResult
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TranslationUiState {
    data object Idle : TranslationUiState
    data object Loading : TranslationUiState
    data class Success(val result: TranslationResult) : TranslationUiState
    data object Unavailable : TranslationUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DictionaryRepository,
    private val translationService: OnlineTranslationService,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _queryField = MutableStateFlow(TextFieldValue(""))
    val queryField: StateFlow<TextFieldValue> = _queryField.asStateFlow()

    private val _shouldFocusSearch = MutableStateFlow(false)
    val shouldFocusSearch: StateFlow<Boolean> = _shouldFocusSearch.asStateFlow()

    private val _wordOfTheDay = MutableStateFlow<WordEntry?>(null)
    val wordOfTheDay: StateFlow<WordEntry?> = _wordOfTheDay.asStateFlow()

    init {
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            val savedId = userPreferences.getSavedDailyWordId(today)
            val daily = if (savedId != null) {
                repository.getWordById(savedId) ?: repository.getWordOfTheDay()
            } else {
                repository.getWordOfTheDay()
            }
            _wordOfTheDay.value = daily
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: StateFlow<List<WordEntry>> = _queryField
        .map { it.text }
        .distinctUntilChanged()
        .debounce(50)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else repository.search(q)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val translationState: StateFlow<TranslationUiState> = combine(
        _queryField.map { it.text.trim() }.distinctUntilChanged(),
        results
    ) { query, offlineResults ->
        val isSentence = query.contains(' ') && query.length >= 4
        val needsFallback = query.length >= 2 && offlineResults.isEmpty()
        if (isSentence || needsFallback) query else ""
    }
        .distinctUntilChanged()
        .debounce(450)
        .flatMapLatest { queryToTranslate ->
            if (queryToTranslate.isEmpty()) {
                flowOf<TranslationUiState>(TranslationUiState.Idle)
            } else {
                flow<TranslationUiState> {
                    emit(TranslationUiState.Loading)
                    val res = translationService.translateAuto(queryToTranslate)
                    if (res != null) {
                        repository.saveTranslationHistory(
                            sourceText = res.sourceText,
                            translatedText = res.translatedText,
                            sourceLang = res.sourceLang,
                            targetLang = res.targetLang
                        )
                        emit(TranslationUiState.Success(res))
                    } else {
                        emit(TranslationUiState.Unavailable)
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TranslationUiState.Idle
        )

    fun rollRandomWord() {
        viewModelScope.launch {
            repository.getRandomWord()?.let { word ->
                _wordOfTheDay.value = word
                userPreferences.saveDailyWordId(LocalDate.now().toEpochDay(), word.id)
            }
        }
    }

    fun onQueryFieldChange(newValue: TextFieldValue) {
        _queryField.value = newValue
    }

    fun setQueryText(text: String) {
        val trimmed = text.trim()
        _queryField.value = TextFieldValue(
            text = trimmed,
            selection = TextRange(trimmed.length)
        )
    }

    fun clearQuery() {
        _queryField.value = TextFieldValue("")
    }

    fun appendCharacter(char: String) {
        val current = _queryField.value
        val start = current.selection.start.coerceIn(0, current.text.length)
        val end = current.selection.end.coerceIn(0, current.text.length)
        val min = minOf(start, end)
        val max = maxOf(start, end)
        val updated = current.text.substring(0, min) + char + current.text.substring(max)
        _queryField.value = TextFieldValue(
            text = updated,
            selection = TextRange(min + char.length)
        )
    }

    fun moveCursorToEnd() {
        val current = _queryField.value
        if (current.selection != TextRange(current.text.length)) {
            _queryField.value = current.copy(selection = TextRange(current.text.length))
        }
    }

    fun onSearchActiveChanged(active: Boolean) {
        _shouldFocusSearch.value = active
        if (!active) {
            _queryField.value = TextFieldValue("")
        }
    }

    fun consumeFocusRequest() {
        _shouldFocusSearch.value = false
    }
}
