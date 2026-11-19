package com.wortik.app.domain.repository

import com.wortik.app.domain.model.TranslationHistoryEntry
import com.wortik.app.domain.model.WordEntry
import kotlinx.coroutines.flow.Flow

interface DictionaryRepository {
    fun search(query: String): Flow<List<WordEntry>>
    fun observeWord(id: Long): Flow<WordEntry?>
    fun observeSavedWords(): Flow<List<WordEntry>>
    fun observeHistory(): Flow<List<WordEntry>>
    fun observeTranslationHistory(): Flow<List<TranslationHistoryEntry>>
    fun observeWordCount(): Flow<Int>
    suspend fun getWordById(id: Long): WordEntry?
    suspend fun getWordOfTheDay(): WordEntry?
    suspend fun getRandomWord(): WordEntry?
    suspend fun getWordsByExactNames(words: List<String>): List<WordEntry>
    suspend fun getWordsByLetter(letter: String, limit: Int, offset: Int): List<WordEntry>
    suspend fun getRelatedCompounds(word: String, excludeId: Long): List<WordEntry>
    suspend fun toggleSaved(id: Long)
    suspend fun markViewed(id: Long)
    suspend fun saveTranslationHistory(
        sourceText: String,
        translatedText: String,
        sourceLang: String,
        targetLang: String
    )
    suspend fun removeHistoryItem(id: Long)
    suspend fun removeTranslationHistoryItem(id: Long)
    suspend fun clearHistory()
}
