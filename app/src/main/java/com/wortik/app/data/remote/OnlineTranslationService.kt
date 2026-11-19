package com.wortik.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class TranslationResult(
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String,
    val targetLang: String
) {
    val germanText: String
        get() = if (sourceLang == "de") sourceText else translatedText
}

@Singleton
class OnlineTranslationService @Inject constructor() {

    private val cache = ConcurrentHashMap<String, TranslationResult>()

    private fun containsPersianScript(text: String): Boolean {
        return text.any { ch -> ch in '\u0600'..'\u06FF' || ch in '\uFB50'..'\uFDFF' || ch in '\uFE70'..'\uFEFF' }
    }

    suspend fun translateAuto(text: String): TranslationResult? {
        val cleaned = text.trim()
        if (cleaned.length < 2) return null

        val key = cleaned.lowercase()
        cache[key]?.let { return it }

        val result = withContext(Dispatchers.IO) {
            if (containsPersianScript(cleaned)) {
                requestTranslation(cleaned, sourceLang = "fa", targetLang = "de")
            } else {

                val probe = requestTranslation(cleaned, sourceLang = "auto", targetLang = "en")
                when {
                    probe == null -> null
                    probe.sourceLang == "de" -> probe
                    probe.sourceLang == "en" -> requestTranslation(cleaned, sourceLang = "en", targetLang = "de")
                    else -> {

                        val deToEn = requestTranslation(cleaned, sourceLang = "de", targetLang = "en")
                        if (deToEn != null && !deToEn.translatedText.equals(cleaned, ignoreCase = true)) {
                            deToEn
                        } else {
                            requestTranslation(cleaned, sourceLang = "en", targetLang = "de")
                        }
                    }
                }
            }
        }

        if (result != null && !result.translatedText.equals(cleaned, ignoreCase = true)) {
            cache[key] = result
            return result
        }
        return null
    }

    private fun requestTranslation(
        text: String,
        sourceLang: String,
        targetLang: String
    ): TranslationResult? {
        return runCatching {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val endpoint = "https://translate.googleapis.com/translate_a/single" +
                "?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=$encoded"
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            }
            try {
                if (conn.responseCode != HttpURLConnection.HTTP_OK) return null
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONArray(body)
                val segments = root.optJSONArray(0) ?: return null
                val sb = StringBuilder()
                for (i in 0 until segments.length()) {
                    val seg = segments.optJSONArray(i) ?: continue
                    val part = seg.optString(0)
                    if (part.isNotBlank()) sb.append(part)
                }
                val translated = sb.toString().trim()
                if (translated.isEmpty()) return null
                val detected = if (sourceLang == "auto") {
                    root.optString(2, "de").lowercase().substringBefore('-')
                } else {
                    sourceLang
                }
                TranslationResult(
                    sourceText = text,
                    translatedText = translated,
                    sourceLang = detected,
                    targetLang = targetLang
                )
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }
}
