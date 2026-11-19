package com.wortik.app.data.remote

import android.content.Context
import com.wortik.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class WordImage(
    val url: String,
    val attribution: String
)

@Singleton
class WordImageService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private data class CacheEntry(
        val image: WordImage?,
        val timestampMs: Long
    )

    private val memoryCache = ConcurrentHashMap<String, CacheEntry>()
    private val prefs by lazy {
        context.getSharedPreferences("wortik_pixabay_cache", Context.MODE_PRIVATE)
    }

    private companion object {
        const val CACHE_TTL_MS = 24L * 60L * 60L * 1000L
    }

    suspend fun getPhotoForNoun(germanWord: String, englishMeanings: List<String>): WordImage? {
        val apiKey = BuildConfig.PIXABAY_API_KEY
        if (apiKey.isBlank()) return null

        val enTerm = extractCleanSearchTerm(englishMeanings)
        val deTerm = germanWord.trim()
        val cacheKey = "${deTerm.lowercase()}|${enTerm.orEmpty()}"
        val now = System.currentTimeMillis()

        memoryCache[cacheKey]?.let { cached ->
            if (now - cached.timestampMs < CACHE_TTL_MS) {
                return cached.image
            }
        }

        readDiskCache(cacheKey, now)?.let { diskCached ->
            memoryCache[cacheKey] = diskCached
            return diskCached.image
        }

        val fetched = withContext(Dispatchers.IO) {
            val fromEnglish = enTerm?.let { fetchFromPixabay(apiKey = apiKey, query = it, lang = "en") }
            fromEnglish ?: if (deTerm.length >= 3) {
                fetchFromPixabay(apiKey = apiKey, query = deTerm, lang = "de")
            } else {
                null
            }
        }

        val entry = CacheEntry(fetched, now)
        memoryCache[cacheKey] = entry
        writeDiskCache(cacheKey, entry)
        return fetched
    }

    private fun readDiskCache(key: String, now: Long): CacheEntry? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching {
            val obj = JSONObject(raw)
            val ts = obj.optLong("ts", 0L)
            if (now - ts >= CACHE_TTL_MS) {
                prefs.edit().remove(key).apply()
                return null
            }
            val url = obj.optString("url")
            val attr = obj.optString("attr")
            val image = if (url.isNotBlank()) WordImage(url, attr) else null
            CacheEntry(image, ts)
        }.getOrNull()
    }

    private fun writeDiskCache(key: String, entry: CacheEntry) {
        runCatching {
            val obj = JSONObject().apply {
                put("ts", entry.timestampMs)
                put("url", entry.image?.url.orEmpty())
                put("attr", entry.image?.attribution.orEmpty())
            }
            prefs.edit().putString(key, obj.toString()).apply()
        }
    }

    private fun extractCleanSearchTerm(meanings: List<String>): String? {
        for (raw in meanings) {
            val cleaned = raw
                .replace(Regex("\\([^)]*\\)"), "")
                .substringBefore(';')
                .substringBefore(',')
                .substringBefore(':')
                .trim()
                .removePrefix("a ")
                .removePrefix("an ")
                .removePrefix("the ")
                .trim()
            val wordCount = cleaned.split(Regex("\\s+")).count { it.isNotBlank() }
            if (cleaned.length in 3..28 && wordCount in 1..3 &&
                cleaned.all { it.isLetter() || it == ' ' || it == '-' }
            ) {
                return cleaned.lowercase()
            }
        }
        return null
    }

    private fun fetchFromPixabay(apiKey: String, query: String, lang: String): WordImage? {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val endpoint = "https://pixabay.com/api/" +
                "?key=$apiKey" +
                "&q=$encoded" +
                "&lang=$lang" +
                "&image_type=photo" +
                "&orientation=horizontal" +
                "&safesearch=true" +
                "&order=popular" +
                "&per_page=5"
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "WortikAndroid/0.1")
                setRequestProperty("Accept", "application/json")
            }
            try {
                if (conn.responseCode != HttpURLConnection.HTTP_OK) return null
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val hits = JSONObject(body).optJSONArray("hits") ?: return null
                for (i in 0 until hits.length()) {
                    val item = hits.optJSONObject(i) ?: continue
                    val url = item.optString("webformatURL").takeIf { it.startsWith("http") }
                        ?: continue
                    val user = item.optString("user").trim()
                    val attribution = if (user.isNotBlank()) {
                        "Photo by $user · Pixabay"
                    } else {
                        "Photo · Pixabay"
                    }
                    return WordImage(url = url, attribution = attribution)
                }
                null
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }
}
