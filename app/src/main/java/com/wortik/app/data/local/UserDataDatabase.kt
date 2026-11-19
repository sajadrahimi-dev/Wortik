package com.wortik.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_word_state")
data class UserWordStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "word_id")
    val wordId: Long,
    @ColumnInfo(name = "is_saved")
    val isSaved: Boolean = false,
    @ColumnInfo(name = "last_viewed_at")
    val lastViewedAt: Long? = null
)

@Entity(tableName = "translation_history")
data class TranslationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "source_text")
    val sourceText: String,
    @ColumnInfo(name = "translated_text")
    val translatedText: String,
    @ColumnInfo(name = "source_lang")
    val sourceLang: String,
    @ColumnInfo(name = "target_lang")
    val targetLang: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

@Dao
interface UserDataDao {

    @Query("SELECT * FROM user_word_state WHERE word_id = :wordId")
    fun observeState(wordId: Long): Flow<UserWordStateEntity?>

    @Query("SELECT * FROM user_word_state WHERE word_id = :wordId")
    suspend fun getState(wordId: Long): UserWordStateEntity?

    @Query("SELECT word_id FROM user_word_state WHERE is_saved = 1")
    fun observeSavedWordIds(): Flow<List<Long>>

    @Query("SELECT word_id FROM user_word_state WHERE is_saved = 1")
    suspend fun getSavedWordIds(): List<Long>

    @Query("SELECT * FROM user_word_state WHERE last_viewed_at IS NOT NULL ORDER BY last_viewed_at DESC LIMIT 100")
    fun observeHistoryStates(): Flow<List<UserWordStateEntity>>

    @Query("SELECT COUNT(*) FROM user_word_state")
    suspend fun getStateCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: UserWordStateEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(states: List<UserWordStateEntity>)

    @Query("UPDATE user_word_state SET last_viewed_at = NULL WHERE word_id = :wordId")
    suspend fun clearHistoryItem(wordId: Long)

    @Query("UPDATE user_word_state SET last_viewed_at = NULL WHERE last_viewed_at IS NOT NULL")
    suspend fun clearHistory()

    @Query("SELECT * FROM translation_history ORDER BY created_at DESC LIMIT 100")
    fun observeTranslationHistory(): Flow<List<TranslationHistoryEntity>>

    @Query("DELETE FROM translation_history WHERE LOWER(source_text) = LOWER(:sourceText)")
    suspend fun deleteTranslationBySource(sourceText: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(entry: TranslationHistoryEntity)

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteTranslationById(id: Long)

    @Query("DELETE FROM translation_history")
    suspend fun clearTranslationHistory()
}

@Database(
    entities = [UserWordStateEntity::class, TranslationHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class UserDataDatabase : RoomDatabase() {
    abstract fun userDataDao(): UserDataDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `translation_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `source_text` TEXT NOT NULL,
                        `translated_text` TEXT NOT NULL,
                        `source_lang` TEXT NOT NULL,
                        `target_lang` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
