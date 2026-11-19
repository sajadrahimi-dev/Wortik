package com.wortik.app.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wortik.app.data.remote.WordImage
import com.wortik.app.data.remote.WordImageService
import com.wortik.app.domain.model.PartOfSpeech
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.domain.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DictionaryRepository,
    private val imageService: WordImageService
) : ViewModel() {

    private val wordId: Long = checkNotNull(savedStateHandle["wordId"])

    val word: StateFlow<WordEntry?> = repository.observeWord(wordId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val wordImage: StateFlow<WordImage?> = word
        .distinctUntilChangedBy { it?.id }
        .mapLatest { entry ->
            if (entry != null && entry.partOfSpeech == PartOfSpeech.NOUN) {
                imageService.getPhotoForNoun(entry.word, entry.meanings)
            } else {
                null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val relatedWords: StateFlow<List<WordEntry>> = word
        .distinctUntilChangedBy { it?.id }
        .mapLatest { entry ->
            if (entry != null) {
                repository.getRelatedCompounds(entry.word, entry.id)
            } else {
                emptyList()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.markViewed(wordId)
        }
    }

    fun toggleSaved() {
        viewModelScope.launch {
            repository.toggleSaved(wordId)
        }
    }
}
