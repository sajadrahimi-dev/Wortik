package com.wortik.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    @ColumnInfo(name = "normalized_word", index = true)
    val normalizedWord: String,
    val gender: String,
    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String,
    val plural: String?,
    val ipa: String?,
    @ColumnInfo(name = "audio_url")
    val audioUrl: String? = null,
    @ColumnInfo(name = "meanings_json")
    val meaningsJson: String,
    @ColumnInfo(name = "examples_json")
    val examplesJson: String,
    @ColumnInfo(name = "noun_declension_json")
    val nounDeclensionJson: String?,
    @ColumnInfo(name = "verb_conjugation_json")
    val verbConjugationJson: String?,
    @ColumnInfo(name = "is_saved")
    val isSaved: Boolean = false,
    @ColumnInfo(name = "last_viewed_at")
    val lastViewedAt: Long? = null
)

@Fts4(contentEntity = WordEntity::class)
@Entity(tableName = "words_fts")
data class WordFtsEntity(
    val word: String,
    @ColumnInfo(name = "normalized_word")
    val normalizedWord: String,
    @ColumnInfo(name = "meanings_json")
    val meaningsJson: String
)
