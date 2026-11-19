package com.wortik.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class GermanNormalizerTest {

    @Test
    fun normalize_replacesUmlautsAndEszett() {
        assertEquals("loesung", GermanNormalizer.normalize("Lösung"))
        assertEquals("strasse", GermanNormalizer.normalize("Straße"))
        assertEquals("haeuser", GermanNormalizer.normalize("Häuser"))
        assertEquals("uebersetzen", GermanNormalizer.normalize("Übersetzen"))
    }

    @Test
    fun normalizeSimple_stripsUmlautDots() {
        assertEquals("losung", GermanNormalizer.normalizeSimple("Lösung"))
        assertEquals("hauser", GermanNormalizer.normalizeSimple("Häuser"))
    }
}
