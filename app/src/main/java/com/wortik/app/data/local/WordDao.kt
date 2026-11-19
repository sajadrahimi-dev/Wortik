package com.wortik.app.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query(
        """
        SELECT * FROM words
        WHERE normalized_word >= :prefix AND normalized_word < :prefixUpper
        ORDER BY LENGTH(word) ASC
        LIMIT 30
        """
    )
    suspend fun searchPrefix(prefix: String, prefixUpper: String): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE id IN (
            SELECT rowid FROM words_fts
            WHERE words_fts MATCH :ftsQuery
            LIMIT 300
        )
        """
    )
    suspend fun searchFtsFast(ftsQuery: String): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    fun observeWord(id: Long): Flow<WordEntity?>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWordById(id: Long): WordEntity?

    @Query("SELECT * FROM words WHERE id IN (:ids)")
    suspend fun getWordsByIds(ids: List<Long>): List<WordEntity>

    @Query("SELECT * FROM words WHERE word IN (:words)")
    suspend fun getWordsByExactNames(words: List<String>): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE normalized_word >= :prefix AND normalized_word < :prefixUpper
        ORDER BY normalized_word ASC, LENGTH(word) ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getWordsByNormalizedPrefixPaged(
        prefix: String,
        prefixUpper: String,
        limit: Int,
        offset: Int
    ): List<WordEntity>

    @Query(
        """
        SELECT * FROM words
        WHERE word LIKE :upperPrefix OR word LIKE :lowerPrefix
        ORDER BY word COLLATE NOCASE ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getWordsByUmlautPaged(
        upperPrefix: String,
        lowerPrefix: String,
        limit: Int,
        offset: Int
    ): List<WordEntity>

    @Query("SELECT * FROM words WHERE is_saved = 1 OR last_viewed_at IS NOT NULL")
    suspend fun getLegacyUserStateWords(): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words")
    fun observeWordCount(): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM words
        WHERE gender IN ('der', 'die', 'das')
          AND meanings_json != '[]'
          AND examples_json != '[]'
        """
    )
    suspend fun countWordOfTheDayCandidates(): Int

    @Query(
        """
        SELECT * FROM words
        WHERE gender IN ('der', 'die', 'das')
          AND meanings_json != '[]'
          AND examples_json != '[]'
        LIMIT 1 OFFSET :offset
        """
    )
    suspend fun getWordOfTheDayAt(offset: Int): WordEntity?
}
