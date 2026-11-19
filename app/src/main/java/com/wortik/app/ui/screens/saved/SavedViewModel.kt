package com.wortik.app.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortik.app.domain.model.Gender
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class SavedFilter(val label: String) {
    ALL("All"),
    DER("der"),
    DIE("die"),
    DAS("das")
}

data class GenderBreakdown(
    val der: Int = 0,
    val die: Int = 0,
    val das: Int = 0
) {
    val total: Int get() = der + die + das
}

@HiltViewModel
class SavedViewModel @Inject constructor(
    repository: DictionaryRepository
) : ViewModel() {

    private val allSavedFlow = repository.observeSavedWords()

    private val _selectedFilter = MutableStateFlow(SavedFilter.ALL)
    val selectedFilter: StateFlow<SavedFilter> = _selectedFilter.asStateFlow()

    val genderBreakdown: StateFlow<GenderBreakdown> = allSavedFlow
        .map { words ->
            GenderBreakdown(
                der = words.count { it.gender == Gender.DER },
                die = words.count { it.gender == Gender.DIE },
                das = words.count { it.gender == Gender.DAS }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GenderBreakdown()
        )

    val savedWords: StateFlow<List<WordEntry>> = combine(
        allSavedFlow,
        _selectedFilter
    ) { words, filter ->
        when (filter) {
            SavedFilter.ALL -> words
            SavedFilter.DER -> words.filter { it.gender == Gender.DER }
            SavedFilter.DIE -> words.filter { it.gender == Gender.DIE }
            SavedFilter.DAS -> words.filter { it.gender == Gender.DAS }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun selectFilter(filter: SavedFilter) {
        _selectedFilter.value = filter
    }
}
