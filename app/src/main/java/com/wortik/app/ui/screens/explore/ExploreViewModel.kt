package com.wortik.app.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortik.app.data.local.WordCategory
import com.wortik.app.data.local.WordCategoryGroup
import com.wortik.app.data.local.WordCategoryRepository
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExploreMode {
    CATEGORIES,
    ALPHABET
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: DictionaryRepository,
    categoryRepository: WordCategoryRepository
) : ViewModel() {

    val groups: List<WordCategoryGroup> = categoryRepository.groups
    private val allCategories: List<WordCategory> = categoryRepository.allCategories

    val letters: List<String> = ('A'..'Z').map { it.toString() } + listOf("Ä", "Ö", "Ü")

    private val _mode = MutableStateFlow(ExploreMode.CATEGORIES)
    val mode: StateFlow<ExploreMode> = _mode.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    private val _categorySearchQuery = MutableStateFlow("")
    val categorySearchQuery: StateFlow<String> = _categorySearchQuery.asStateFlow()

    val filteredCategories: StateFlow<List<WordCategory>> = combine(
        _selectedGroupId,
        _categorySearchQuery
    ) { groupId, query ->
        val trimmed = query.trim()
        allCategories.filter { cat ->
            val matchesGroup = groupId == null || cat.groupId == groupId
            val matchesQuery = trimmed.isEmpty() ||
                cat.title.contains(trimmed, ignoreCase = true) ||
                cat.germanTitle.contains(trimmed, ignoreCase = true)
            matchesGroup && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = allCategories
    )

    private val _selectedCategory = MutableStateFlow<WordCategory?>(null)
    val selectedCategory: StateFlow<WordCategory?> = _selectedCategory.asStateFlow()

    private val _categoryWords = MutableStateFlow<List<WordEntry>>(emptyList())
    val categoryWords: StateFlow<List<WordEntry>> = _categoryWords.asStateFlow()

    private val _isLoadingCategory = MutableStateFlow(false)
    val isLoadingCategory: StateFlow<Boolean> = _isLoadingCategory.asStateFlow()

    private val _selectedLetter = MutableStateFlow("A")
    val selectedLetter: StateFlow<String> = _selectedLetter.asStateFlow()

    private val _letterWords = MutableStateFlow<List<WordEntry>>(emptyList())
    val letterWords: StateFlow<List<WordEntry>> = _letterWords.asStateFlow()

    private val _isLoadingLetter = MutableStateFlow(false)
    val isLoadingLetter: StateFlow<Boolean> = _isLoadingLetter.asStateFlow()

    private var hasMoreForLetter = true
    private var letterLoadJob: Job? = null
    private var categoryLoadJob: Job? = null

    init {
        loadLetterPage("A", reset = true)
    }

    fun selectMode(newMode: ExploreMode) {
        _mode.value = newMode
    }

    fun selectGroup(groupId: String?) {
        _selectedGroupId.value = groupId
    }

    fun onCategorySearchChange(query: String) {
        _categorySearchQuery.value = query
    }

    fun openCategory(category: WordCategory) {
        _selectedCategory.value = category
        categoryLoadJob?.cancel()
        categoryLoadJob = viewModelScope.launch {
            _isLoadingCategory.value = true
            _categoryWords.value = repository.getWordsByExactNames(category.words)
            _isLoadingCategory.value = false
        }
    }

    fun closeCategory() {
        _selectedCategory.value = null
    }

    fun selectLetter(letter: String) {
        if (_selectedLetter.value == letter && _letterWords.value.isNotEmpty()) return
        _selectedLetter.value = letter
        loadLetterPage(letter, reset = true)
    }

    fun loadMoreForSelectedLetter() {
        if (_isLoadingLetter.value || !hasMoreForLetter) return
        loadLetterPage(_selectedLetter.value, reset = false)
    }

    private fun loadLetterPage(letter: String, reset: Boolean) {
        letterLoadJob?.cancel()
        letterLoadJob = viewModelScope.launch {
            _isLoadingLetter.value = true
            val offset = if (reset) 0 else _letterWords.value.size
            val pageSize = 60
            val page = repository.getWordsByLetter(letter, limit = pageSize, offset = offset)
            hasMoreForLetter = page.size == pageSize
            _letterWords.value = if (reset) page else _letterWords.value + page
            _isLoadingLetter.value = false
        }
    }
}
