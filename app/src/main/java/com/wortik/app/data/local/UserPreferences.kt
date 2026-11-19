package com.wortik.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wortik_prefs")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppPalette(val displayName: String) {
    BERLIN_BLUE("Berlin"),
    SCHWARZWALD("Forest"),
    BAVARIA_GOLD("Amber"),
    BERRY_ROSE("Berry"),
    LAVENDER("Iris"),
    MONOCHROME("Ink")
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val palette: AppPalette = AppPalette.BERLIN_BLUE,
    val amoledBlack: Boolean = false
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val PALETTE = stringPreferencesKey("app_palette")
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
        val WOTD_DAY = longPreferencesKey("wotd_epoch_day")
        val WOTD_WORD_ID = longPreferencesKey("wotd_word_id")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val themeMode = prefs[Keys.THEME_MODE]?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.SYSTEM
        val dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true
        val palette = prefs[Keys.PALETTE]?.let {
            runCatching { AppPalette.valueOf(it) }.getOrNull()
        } ?: AppPalette.BERLIN_BLUE
        val amoledBlack = prefs[Keys.AMOLED_BLACK] ?: false
        UserSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            palette = palette,
            amoledBlack = amoledBlack
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setPalette(palette: AppPalette) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PALETTE] = palette.name
            prefs[Keys.DYNAMIC_COLOR] = false
        }
    }

    suspend fun setAmoledBlack(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AMOLED_BLACK] = enabled
        }
    }

    suspend fun getSavedDailyWordId(epochDay: Long): Long? {
        val prefs = context.dataStore.data.first()
        val savedDay = prefs[Keys.WOTD_DAY] ?: return null
        if (savedDay != epochDay) return null
        return prefs[Keys.WOTD_WORD_ID]
    }

    suspend fun saveDailyWordId(epochDay: Long, wordId: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WOTD_DAY] = epochDay
            prefs[Keys.WOTD_WORD_ID] = wordId
        }
    }
}
