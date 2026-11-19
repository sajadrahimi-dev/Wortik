package com.wortik.app.data.repository

import com.wortik.app.data.local.GermanNormalizer
import com.wortik.app.data.local.TranslationHistoryEntity
import com.wortik.app.data.local.UserDataDao
import com.wortik.app.data.local.UserWordStateEntity
import com.wortik.app.data.local.WordDao
import com.wortik.app.data.local.WordEntity
import com.wortik.app.domain.model.ExampleSentence
import com.wortik.app.domain.model.Gender
import com.wortik.app.domain.model.NounDeclension
import com.wortik.app.domain.model.PartOfSpeech
import com.wortik.app.domain.model.TranslationHistoryEntry
import com.wortik.app.domain.model.VerbConjugation
import com.wortik.app.domain.model.WordEntry
import com.wortik.app.domain.repository.DictionaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val userDataDao: UserDataDao
) : DictionaryRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val migrationMutex = Mutex()
    @Volatile
    private var migratedLegacyState = false

    private suspend fun ensureLegacyUserDataMigrated() {
        if (migratedLegacyState) return
        migrationMutex.withLock {
            if (migratedLegacyState) return
            withContext(Dispatchers.IO) {
                if (userDataDao.getStateCount() == 0) {
                    val legacy = wordDao.getLegacyUserStateWords()
                    if (legacy.isNotEmpty()) {
                        userDataDao.insertAllIgnore(
                            legacy.map {
                                UserWordStateEntity(
                                    wordId = it.id,
                                    isSaved = it.isSaved,
                                    lastViewedAt = it.lastViewedAt
                                )
                            }
                        )
                    }
                }
            }
            migratedLegacyState = true
        }
    }

    override fun search(query: String): Flow<List<WordEntry>> = flow {
        ensureLegacyUserDataMigrated()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val rawLower = trimmed.lowercase()
        val norm = GermanNormalizer.normalize(trimmed)
        val normSimple = GermanNormalizer.normalizeSimple(trimmed)
        val ftsTokens = trimmed
            .replace("\"", "")
            .replace("*", "")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { "$it*" }

        val results = coroutineScope {
            val savedIdsDeferred = async { userDataDao.getSavedWordIds().toHashSet() }
            val prefixDeferred = async {
                if (norm.isNotEmpty()) {
                    wordDao.searchPrefix(norm, upperBound(norm))
                } else {
                    emptyList()
                }
            }
            val simplePrefixDeferred = async {
                if (normSimple.isNotEmpty() && normSimple != norm) {
                    wordDao.searchPrefix(normSimple, upperBound(normSimple))
                } else {
                    emptyList()
                }
            }
            val ftsDeferred = async {
                if (ftsTokens.isNotBlank() && trimmed.length >= 2) {
                    runCatching { wordDao.searchFtsFast(ftsTokens) }.getOrDefault(emptyList())
                } else {
                    emptyList()
                }
            }
            val exactMeaningFtsDeferred = async {
                val cleanToken = trimmed.replace("\"", "").replace("*", "").trim()
                if (cleanToken.length >= 2 && !cleanToken.contains(' ')) {
                    runCatching {
                        wordDao.searchFtsFast("meanings_json:$cleanToken")
                    }.getOrDefault(emptyList())
                } else {
                    emptyList()
                }
            }

            val savedIds = savedIdsDeferred.await()
            val merged = LinkedHashMap<Long, WordEntity>()
            prefixDeferred.await().forEach { merged[it.id] = it }
            simplePrefixDeferred.await().forEach { merged.putIfAbsent(it.id, it) }
            exactMeaningFtsDeferred.await().forEach { merged.putIfAbsent(it.id, it) }
            ftsDeferred.await().forEach { merged.putIfAbsent(it.id, it) }

            val domainEntries = merged.values.map { entity ->
                entity.toDomain(isSavedOverride = entity.id in savedIds)
            }
            WordSearchRanker.sortAndLimit(domainEntries, rawLower, norm, limit = 40)
        }

        emit(results)
    }.flowOn(Dispatchers.IO)

    private fun upperBound(prefix: String): String {
        val last = prefix.last()
        return prefix.dropLast(1) + (last + 1)
    }

    override fun observeWord(id: Long): Flow<WordEntry?> {
        return combine(
            wordDao.observeWord(id),
            userDataDao.observeState(id)
        ) { entity, state ->
            ensureLegacyUserDataMigrated()
            entity?.toDomain(
                isSavedOverride = state?.isSaved ?: false,
                lastViewedOverride = state?.lastViewedAt
            )
        }.flowOn(Dispatchers.IO)
    }

    override fun observeSavedWords(): Flow<List<WordEntry>> {
        return userDataDao.observeSavedWordIds()
            .map { ids ->
                ensureLegacyUserDataMigrated()
                if (ids.isEmpty()) {
                    emptyList()
                } else {
                    wordDao.getWordsByIds(ids)
                        .map { it.toDomain(isSavedOverride = true) }
                        .sortedBy { it.word.lowercase() }
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun observeHistory(): Flow<List<WordEntry>> {
        return userDataDao.observeHistoryStates()
            .map { states ->
                ensureLegacyUserDataMigrated()
                if (states.isEmpty()) {
                    emptyList()
                } else {
                    val entities = wordDao.getWordsByIds(states.map { it.wordId })
                        .associateBy { it.id }
                    states.mapNotNull { state ->
                        entities[state.wordId]?.toDomain(
                            isSavedOverride = state.isSaved,
                            lastViewedOverride = state.lastViewedAt
                        )
                    }
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun observeTranslationHistory(): Flow<List<TranslationHistoryEntry>> {
        return userDataDao.observeTranslationHistory().map { list ->
            list.map {
                TranslationHistoryEntry(
                    id = it.id,
                    sourceText = it.sourceText,
                    translatedText = it.translatedText,
                    sourceLang = it.sourceLang,
                    targetLang = it.targetLang,
                    createdAt = it.createdAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun observeWordCount(): Flow<Int> = wordDao.observeWordCount()

    override suspend fun getWordById(id: Long): WordEntry? = withContext(Dispatchers.IO) {
        ensureLegacyUserDataMigrated()
        val entity = wordDao.getWordById(id) ?: return@withContext null
        val state = userDataDao.getState(entity.id)
        entity.toDomain(
            isSavedOverride = state?.isSaved ?: false,
            lastViewedOverride = state?.lastViewedAt
        )
    }

    override suspend fun getWordOfTheDay(): WordEntry? = withContext(Dispatchers.IO) {
        ensureLegacyUserDataMigrated()
        val count = wordDao.countWordOfTheDayCandidates()
        if (count <= 0) return@withContext null
        val epochDay = LocalDate.now().toEpochDay()
        val mixed = ((epochDay * 2654435761L) and Long.MAX_VALUE) % count
        val entity = wordDao.getWordOfTheDayAt(mixed.toInt()) ?: return@withContext null
        val state = userDataDao.getState(entity.id)
        entity.toDomain(
            isSavedOverride = state?.isSaved ?: false,
            lastViewedOverride = state?.lastViewedAt
        )
    }

    override suspend fun getRandomWord(): WordEntry? = withContext(Dispatchers.IO) {
        ensureLegacyUserDataMigrated()
        val count = wordDao.countWordOfTheDayCandidates()
        if (count <= 0) return@withContext null
        val offset = kotlin.random.Random.nextInt(count)
        val entity = wordDao.getWordOfTheDayAt(offset) ?: return@withContext null
        val state = userDataDao.getState(entity.id)
        entity.toDomain(
            isSavedOverride = state?.isSaved ?: false,
            lastViewedOverride = state?.lastViewedAt
        )
    }

    override suspend fun getWordsByExactNames(words: List<String>): List<WordEntry> =
        withContext(Dispatchers.IO) {
            ensureLegacyUserDataMigrated()
            if (words.isEmpty()) return@withContext emptyList()
            val savedIds = userDataDao.getSavedWordIds().toHashSet()
            val entities = wordDao.getWordsByExactNames(words)
            val orderMap = words.withIndex().associate { it.value to it.index }

            entities
                .groupBy { it.word }
                .values
                .map { group -> group.maxByOrNull { it.meaningsJson.length } ?: group.first() }
                .sortedBy { orderMap[it.word] ?: Int.MAX_VALUE }
                .map { it.toDomain(isSavedOverride = it.id in savedIds) }
        }

    override suspend fun getWordsByLetter(
        letter: String,
        limit: Int,
        offset: Int
    ): List<WordEntry> = withContext(Dispatchers.IO) {
        ensureLegacyUserDataMigrated()
        val clean = letter.trim()
        if (clean.isEmpty()) return@withContext emptyList()
        val savedIds = userDataDao.getSavedWordIds().toHashSet()
        val entities = when (clean.uppercase()) {
            "Ä", "Ö", "Ü" -> wordDao.getWordsByUmlautPaged(
                upperPrefix = "${clean.uppercase()}%",
                lowerPrefix = "${clean.lowercase()}%",
                limit = limit,
                offset = offset
            )
            else -> {
                val lower = clean.lowercase()
                wordDao.getWordsByNormalizedPrefixPaged(
                    prefix = lower,
                    prefixUpper = upperBound(lower),
                    limit = limit,
                    offset = offset
                )
            }
        }
        entities.map { it.toDomain(isSavedOverride = it.id in savedIds) }
    }

    override suspend fun getRelatedCompounds(word: String, excludeId: Long): List<WordEntry> =
        withContext(Dispatchers.IO) {
            val clean = word.trim()
            if (clean.length < 3 || clean.contains(' ')) return@withContext emptyList()
            val norm = GermanNormalizer.normalize(clean)
            if (norm.length < 3) return@withContext emptyList()
            val candidates = wordDao.searchPrefix(norm, upperBound(norm))
            candidates
                .asSequence()
                .filter { it.id != excludeId && !it.word.equals(clean, ignoreCase = true) }
                .filter { it.word.length in (clean.length + 2)..(clean.length + 16) }
                .distinctBy { it.word.lowercase() }
                .take(10)
                .map { it.toDomain() }
                .toList()
        }

    override suspend fun toggleSaved(id: Long) = withContext(Dispatchers.IO) {
        ensureLegacyUserDataMigrated()
        val current = userDataDao.getState(id)
        val updated = if (current != null) {
            current.copy(isSaved = !current.isSaved)
        } else {
            UserWordStateEntity(wordId = id, isSaved = true, lastViewedAt = null)
        }
        userDataDao.upsert(updated)
    }

    override suspend fun markViewed(id: Long) = withContext(Dispatchers.IO) {
        ensureLegacyUserDataMigrated()
        val now = System.currentTimeMillis()
        val current = userDataDao.getState(id)
        val updated = if (current != null) {
            current.copy(lastViewedAt = now)
        } else {
            UserWordStateEntity(wordId = id, isSaved = false, lastViewedAt = now)
        }
        userDataDao.upsert(updated)
    }

    override suspend fun saveTranslationHistory(
        sourceText: String,
        translatedText: String,
        sourceLang: String,
        targetLang: String
    ) = withContext(Dispatchers.IO) {
        val cleanSource = sourceText.trim()
        val cleanTarget = translatedText.trim()
        if (cleanSource.isEmpty() || cleanTarget.isEmpty()) return@withContext
        userDataDao.deleteTranslationBySource(cleanSource)
        userDataDao.insertTranslation(
            TranslationHistoryEntity(
                sourceText = cleanSource,
                translatedText = cleanTarget,
                sourceLang = sourceLang,
                targetLang = targetLang,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeHistoryItem(id: Long) = withContext(Dispatchers.IO) {
        ensureLegacyUserDataMigrated()
        userDataDao.clearHistoryItem(id)
    }

    override suspend fun removeTranslationHistoryItem(id: Long) = withContext(Dispatchers.IO) {
        userDataDao.deleteTranslationById(id)
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        ensureLegacyUserDataMigrated()
        userDataDao.clearHistory()
        userDataDao.clearTranslationHistory()
    }

    private fun WordEntity.toDomain(
        isSavedOverride: Boolean = isSaved,
        lastViewedOverride: Long? = lastViewedAt
    ): WordEntry {
        val meanings = runCatching { json.decodeFromString<List<String>>(meaningsJson) }
            .getOrDefault(emptyList())
        val examples = runCatching { json.decodeFromString<List<ExampleSentence>>(examplesJson) }
            .getOrDefault(emptyList())
        val declension = nounDeclensionJson?.let {
            runCatching { json.decodeFromString<NounDeclension>(it) }.getOrNull()
        }
        val conjugation = verbConjugationJson?.let {
            runCatching { json.decodeFromString<VerbConjugation>(it) }.getOrNull()
        }

        return WordEntry(
            id = id,
            word = word,
            normalizedWord = normalizedWord,
            gender = Gender.fromString(gender),
            partOfSpeech = PartOfSpeech.fromString(partOfSpeech),
            plural = plural,
            ipa = ipa,
            audioUrl = audioUrl,
            meanings = meanings,
            examples = examples,
            nounDeclension = declension,
            verbConjugation = conjugation,
            isSaved = isSavedOverride,
            lastViewedAt = lastViewedOverride
        )
    }
}
