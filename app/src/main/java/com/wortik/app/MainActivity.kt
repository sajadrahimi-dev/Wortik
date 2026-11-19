package com.wortik.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wortik.app.data.local.ThemeMode
import com.wortik.app.data.local.UserPreferences
import com.wortik.app.data.local.UserSettings
import com.wortik.app.ui.components.BottomTab
import com.wortik.app.ui.components.FloatingBottomBar
import com.wortik.app.ui.components.VolumeMutedToast
import com.wortik.app.ui.screens.detail.WordDetailScreen
import com.wortik.app.ui.screens.explore.ExploreScreen
import com.wortik.app.ui.screens.history.HistoryScreen
import com.wortik.app.ui.screens.home.HomeScreen
import com.wortik.app.ui.screens.saved.SavedScreen
import com.wortik.app.ui.screens.settings.SettingsScreen
import com.wortik.app.ui.theme.WortikTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

private object Routes {
    const val HISTORY = "history"
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val SAVED = "saved"
    const val SETTINGS = "settings"
    const val WORD_DETAIL = "word/{wordId}"

    fun wordDetail(wordId: Long) = "word/$wordId"

    fun tabIndex(route: String?): Int? = when (route) {
        HISTORY -> 0
        HOME -> 1
        EXPLORE -> 2
        SAVED -> 3
        else -> null
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private val externalLookupQuery = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleIncomingIntent(intent)

        setContent {
            val settings by userPreferences.settings.collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )
            val pendingLookup by externalLookupQuery.collectAsStateWithLifecycle()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            LaunchedEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT
                    ) { darkTheme }
                )
            }

            WortikTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
                palette = settings.palette,
                amoledBlack = settings.amoledBlack
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    WortikApp(
                        externalLookupQuery = pendingLookup,
                        onExternalLookupConsumed = { externalLookupQuery.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_PROCESS_TEXT) {
            val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
                ?.toString()
                ?.trim()
                ?.take(80)
            if (!selectedText.isNullOrEmpty()) {
                externalLookupQuery.value = selectedText
            }
        }
    }
}

@Composable
private fun WortikApp(
    externalLookupQuery: String?,
    onExternalLookupConsumed: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var showVolumeToast by remember { mutableStateOf(false) }
    var internalLookupQuery by remember { mutableStateOf<String?>(null) }
    val activeExternalQuery = externalLookupQuery ?: internalLookupQuery

    LaunchedEffect(activeExternalQuery) {
        if (!activeExternalQuery.isNullOrBlank() && currentRoute != Routes.HOME) {
            navController.navigate(Routes.HOME) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val currentTab = when (currentRoute) {
        Routes.HISTORY -> BottomTab.HISTORY
        Routes.EXPLORE -> BottomTab.EXPLORE
        Routes.SAVED -> BottomTab.SAVED
        else -> BottomTab.HOME
    }

    val showBottomBar = currentRoute in setOf(
        Routes.HISTORY,
        Routes.HOME,
        Routes.EXPLORE,
        Routes.SAVED
    ) && !(currentRoute == Routes.HOME && isSearchActive)

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = {
                val from = Routes.tabIndex(initialState.destination.route)
                val to = Routes.tabIndex(targetState.destination.route)
                if (from != null && to != null && from != to) {
                    val dir = if (to > from) 1 else -1
                    slideInHorizontally(animationSpec = tween(220)) { (it * 0.18f * dir).toInt() } +
                        fadeIn(animationSpec = tween(200))
                } else {
                    fadeIn(animationSpec = tween(150))
                }
            },
            exitTransition = {
                val from = Routes.tabIndex(initialState.destination.route)
                val to = Routes.tabIndex(targetState.destination.route)
                if (from != null && to != null && from != to) {
                    val dir = if (to > from) -1 else 1
                    slideOutHorizontally(animationSpec = tween(200)) { (it * 0.14f * dir).toInt() } +
                        fadeOut(animationSpec = tween(160))
                } else {
                    fadeOut(animationSpec = tween(120))
                }
            },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = { fadeOut(animationSpec = tween(120)) },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            composable(Routes.HISTORY) {
                HistoryScreen(
                    viewModel = hiltViewModel(),
                    onWordClick = { id -> navController.navigate(Routes.wordDetail(id)) },
                    onTranslationClick = { query -> internalLookupQuery = query },
                    onVolumeMuted = { showVolumeToast = true },
                    onVolumeRestored = { showVolumeToast = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = hiltViewModel(),
                    isSearchActive = isSearchActive,
                    onSearchActiveChange = { isSearchActive = it },
                    externalQuery = activeExternalQuery,
                    onExternalQueryConsumed = {
                        internalLookupQuery = null
                        onExternalLookupConsumed()
                    },
                    onWordClick = { id -> navController.navigate(Routes.wordDetail(id)) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onVolumeMuted = { showVolumeToast = true },
                    onVolumeRestored = { showVolumeToast = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }

            composable(Routes.EXPLORE) {
                ExploreScreen(
                    viewModel = hiltViewModel(),
                    onWordClick = { id -> navController.navigate(Routes.wordDetail(id)) },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }

            composable(Routes.SAVED) {
                SavedScreen(
                    viewModel = hiltViewModel(),
                    onWordClick = { id -> navController.navigate(Routes.wordDetail(id)) },
                    onVolumeMuted = { showVolumeToast = true },
                    onVolumeRestored = { showVolumeToast = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }

            composable(
                route = Routes.WORD_DETAIL,
                arguments = listOf(navArgument("wordId") { type = NavType.LongType })
            ) {
                WordDetailScreen(
                    viewModel = hiltViewModel(),
                    onBackClick = { navController.popBackStack() },
                    onWordClick = { id -> navController.navigate(Routes.wordDetail(id)) },
                    onVolumeMuted = { showVolumeToast = true },
                    onVolumeRestored = { showVolumeToast = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = hiltViewModel(),
                    onBackClick = { navController.popBackStack() },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }
        }

        AnimatedVisibility(
            visible = showBottomBar,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingBottomBar(
                currentTab = currentTab,
                onSelectTab = { tab ->
                    val route = when (tab) {
                        BottomTab.HISTORY -> Routes.HISTORY
                        BottomTab.HOME -> Routes.HOME
                        BottomTab.EXPLORE -> Routes.EXPLORE
                        BottomTab.SAVED -> Routes.SAVED
                    }
                    isSearchActive = false
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        VolumeMutedToast(
            visible = showVolumeToast,
            onDismiss = { showVolumeToast = false },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
