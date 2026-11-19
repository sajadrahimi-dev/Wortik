package com.wortik.app.data.repository

import com.wortik.app.data.local.GermanNormalizer
import com.wortik.app.domain.model.Gender
import com.wortik.app.domain.model.PartOfSpeech
import com.wortik.app.domain.model.WordEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class WordSearchRankerTest {

    private fun word(
        id: Long,
        word: String,
        meanings: List<String> = emptyList()
    ): WordEntry = WordEntry(
        id = id,
        word = word,
        normalizedWord = "${GermanNormalizer.normalize(word)} ${GermanNormalizer.normalizeSimple(word)}",
        gender = Gender.DER,
        partOfSpeech = PartOfSpeech.NOUN,
        plural = null,
        ipa = null,
        meanings = meanings,
        examples = emptyList()
    )

    @Test
    fun exactWordMatch_ranksAbovePrefixAndMeaning() {
        val exact = word(1, "Haus", listOf("house"))
        val prefix = word(2, "Haustür", listOf("front door"))
        val meaningExact = word(3, "Gebäude", listOf("haus"))
        val fallback = word(4, "Bauwerk", listOf("structure"))

        val sorted = WordSearchRanker.sortAndLimit(
            entries = listOf(fallback, prefix, meaningExact, exact),
            rawLower = "haus",
            norm = GermanNormalizer.normalize("haus")
        )

        assertEquals(listOf(1L, 3L, 2L, 4L), sorted.map { it.id })
    }

    @Test
    fun normalizedUmlautMatch_getsTopRank() {
        val loesung = word(1, "Lösung", listOf("solution"))
        val loesungsweg = word(2, "Lösungsweg", listOf("approach"))

        val sorted = WordSearchRanker.sortAndLimit(
            entries = listOf(loesungsweg, loesung),
            rawLower = "loesung",
            norm = GermanNormalizer.normalize("loesung")
        )

        assertEquals(1L, sorted.first().id)
        assertEquals(0, WordSearchRanker.rank(loesung, "loesung", "loesung"))
    }

    @Test
    fun meaningSegmentMatch_ranksBeforePrefixWord() {
        val segmentMatch = word(1, "Hund", listOf("canine, dog; hound"))
        val prefixMeaning = word(2, "Welpe", listOf("doggy"))

        val sorted = WordSearchRanker.sortAndLimit(
            entries = listOf(prefixMeaning, segmentMatch),
            rawLower = "dog",
            norm = "dog"
        )

        assertEquals(listOf(1L, 2L), sorted.map { it.id })
    }
}
