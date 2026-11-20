package com.wortik.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WordEntryModelTest {

    @Test
    fun genderFromString_parsesAllVariants() {
        assertEquals(Gender.DER, Gender.fromString("der"))
        assertEquals(Gender.DER, Gender.fromString("M"))
        assertEquals(Gender.DER, Gender.fromString("masculine"))
        assertEquals(Gender.DIE, Gender.fromString("die"))
        assertEquals(Gender.DIE, Gender.fromString("f"))
        assertEquals(Gender.DIE, Gender.fromString("Feminine"))
        assertEquals(Gender.DAS, Gender.fromString("das"))
        assertEquals(Gender.DAS, Gender.fromString("n"))
        assertEquals(Gender.DAS, Gender.fromString("neuter"))
        assertEquals(Gender.NONE, Gender.fromString("none"))
        assertEquals(Gender.NONE, Gender.fromString(null))
    }

    @Test
    fun partOfSpeechFromString_parsesGermanAndEnglishLabels() {
        assertEquals(PartOfSpeech.NOUN, PartOfSpeech.fromString("noun"))
        assertEquals(PartOfSpeech.NOUN, PartOfSpeech.fromString("Nomen"))
        assertEquals(PartOfSpeech.VERB, PartOfSpeech.fromString("verb"))
        assertEquals(PartOfSpeech.ADJECTIVE, PartOfSpeech.fromString("Adjektiv"))
        assertEquals(PartOfSpeech.ADVERB, PartOfSpeech.fromString("adverb"))
        assertEquals(PartOfSpeech.PREPOSITION, PartOfSpeech.fromString("Präposition"))
        assertEquals(PartOfSpeech.OTHER, PartOfSpeech.fromString("unknown"))
        assertEquals(PartOfSpeech.OTHER, PartOfSpeech.fromString(null))
    }
}
