package com.wortik.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class WordCategory(
    val id: String,
    val groupId: String,
    val title: String,
    val germanTitle: String,
    val words: List<String>
)

@Serializable
data class WordCategoryGroup(
    val id: String,
    val title: String,
    val germanTitle: String,
    val categories: List<WordCategory>
)

@Serializable
private data class CategoriesCatalog(
    val groups: List<WordCategoryGroup>
)

@Singleton
class WordCategoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    val groups: List<WordCategoryGroup> by lazy {
        runCatching {
            val raw = context.assets.open("categories.json").bufferedReader().use { it.readText() }
            json.decodeFromString<CategoriesCatalog>(raw).groups
        }.getOrDefault(emptyList())
    }

    val allCategories: List<WordCategory> by lazy {
        groups.flatMap { it.categories }
    }
}
