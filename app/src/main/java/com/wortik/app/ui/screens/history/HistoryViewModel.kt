package com.wortik.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortik.app.domain.model.TranslationHistoryEntry
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryItem {
    val timestamp: Long
    val stableKey: String

    data class Word(val entry: WordEntry) : HistoryItem {
        override val timestamp: Long get() = entry.lastViewedAt ?: 0L
        override val stableKey: String get() = "word_${entry.id}"
    }

    data class Translation(val entry: TranslationHistoryEntry) : HistoryItem {
        override val timestamp: Long get() = entry.createdAt
        override val stableKey: String get() = "trans_${entry.id}"
    }
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: DictionaryRepository
) : ViewModel() {

    val historyItems: StateFlow<List<HistoryItem>> = combine(
        repository.observeHistory(),
        repository.observeTranslationHistory()
    ) { words, translations ->
        val merged = buildList<HistoryItem>(words.size + translations.size) {
            words.forEach { add(HistoryItem.Word(it)) }
            translations.forEach { add(HistoryItem.Translation(it)) }
        }
        merged.sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun removeItem(item: HistoryItem) {
        viewModelScope.launch {
            when (item) {
                is HistoryItem.Word -> repository.removeHistoryItem(item.entry.id)
                is HistoryItem.Translation -> repository.removeTranslationHistoryItem(item.entry.id)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
