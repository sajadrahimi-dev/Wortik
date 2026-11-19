package com.wortik.app.domain.model

import kotlinx.serialization.Serializable

enum class Gender(val article: String) {
    DER("der"),
    DIE("die"),
    DAS("das"),
    NONE("");

    companion object {
        fun fromString(value: String?): Gender = when (value?.lowercase()) {
            "der", "m", "masculine" -> DER
            "die", "f", "feminine" -> DIE
            "das", "n", "neuter" -> DAS
            else -> NONE
        }
    }
}

enum class PartOfSpeech(val label: String, val shortLabel: String) {
    NOUN("Noun", "n."),
    VERB("Verb", "v."),
    ADJECTIVE("Adjective", "adj."),
    ADVERB("Adverb", "adv."),
    PREPOSITION("Preposition", "prep."),
    OTHER("Other", "");

    companion object {
        fun fromString(value: String?): PartOfSpeech = when (value?.lowercase()) {
            "noun", "nomen" -> NOUN
            "verb" -> VERB
            "adjective", "adjektiv" -> ADJECTIVE
            "adverb" -> ADVERB
            "preposition", "präposition" -> PREPOSITION
            else -> OTHER
        }
    }
}

@Serializable
data class NounDeclension(
    val nominativSg: String,
    val genitivSg: String,
    val dativSg: String,
    val akkusativSg: String,
    val nominativPl: String,
    val genitivPl: String,
    val dativPl: String,
    val akkusativPl: String
)

@Serializable
data class VerbForms(
    val ich: String,
    val du: String,
    val erSieEs: String,
    val wir: String,
    val ihr: String,
    val sieSie: String
)

@Serializable
data class VerbConjugation(
    val auxiliary: String,
    val partizipII: String,
    val praesens: VerbForms,
    val praeteritum: VerbForms
)

@Serializable
data class ExampleSentence(
    val german: String,
    val english: String
)

data class WordEntry(
    val id: Long,
    val word: String,
    val normalizedWord: String,
    val gender: Gender,
    val partOfSpeech: PartOfSpeech,
    val plural: String?,
    val ipa: String?,
    val audioUrl: String? = null,
    val meanings: List<String>,
    val examples: List<ExampleSentence>,
    val nounDeclension: NounDeclension? = null,
    val verbConjugation: VerbConjugation? = null,
    val isSaved: Boolean = false,
    val lastViewedAt: Long? = null
)

data class TranslationHistoryEntry(
    val id: Long,
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String,
    val createdAt: Long
) {
    val germanText: String
        get() = if (sourceLang == "de") sourceText else translatedText
}
