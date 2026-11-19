package com.wortik.app.data.local

object GermanNormalizer {
    fun normalize(input: String): String {
        return input.trim()
            .lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
    }

    fun normalizeSimple(input: String): String {
        return input.trim()
            .lowercase()
            .replace("ä", "a")
            .replace("ö", "o")
            .replace("ü", "u")
            .replace("ß", "ss")
    }
}
