package com.wortik.app.data.repository

import com.wortik.app.domain.model.WordEntry

object WordSearchRanker {

    fun rank(entry: WordEntry, rawLower: String, norm: String): Int {
        val wLower = entry.word.lowercase()
        val normFirst = entry.normalizedWord.substringBefore(' ')
        if (wLower == rawLower || normFirst == norm) return 0
        if (entry.meanings.any { it.lowercase() == rawLower }) return 1
        if (entry.meanings.any { m ->
                m.lowercase().split(',', ';', ':').any { part -> part.trim() == rawLower }
            }
        ) return 2
        if (wLower.startsWith(rawLower) || normFirst.startsWith(norm)) return 3
        if (entry.meanings.any { it.lowercase().startsWith(rawLower) }) return 4
        return 5
    }

    fun sortAndLimit(
        entries: Collection<WordEntry>,
        rawLower: String,
        norm: String,
        limit: Int = 40
    ): List<WordEntry> {
        return entries
            .sortedWith(
                compareBy<WordEntry>(
                    { rank(it, rawLower, norm) },
                    { it.word.length },
                    { it.word.lowercase() }
                )
            )
            .take(limit)
    }
}
