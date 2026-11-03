package com.example.nihongo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.data.SettingsRepository
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.kana.KanaViewModel
import com.example.nihongo.ui.kana.TtsHelper
import com.example.nihongo.ui.quiz.KanaQuizViewModel
import com.example.nihongo.ui.quiz.VocabQuizViewModel
import com.example.nihongo.ui.settings.SettingsScreen
import com.example.nihongo.ui.settings.SettingsViewModel
import com.example.nihongo.ui.stats.StatsViewModel
import com.example.nihongo.ui.theme.NihongoTheme
import com.example.nihongo.ui.home.HomeScreen
import com.example.nihongo.ui.learn.ROUTE_HUB
import com.example.nihongo.ui.learn.parentRoute
import com.example.nihongo.ui.search.SearchScreen
import com.example.nihongo.ui.search.SearchViewModel

class MainActivity : ComponentActivity() {
    private val kanaVm: KanaViewModel by viewModels()
    private val searchVm: SearchViewModel by viewModels()
    private val kanaQuizVm: KanaQuizViewModel by viewModels()
    private val vocabQuizVm: VocabQuizViewModel by viewModels()
    private val statsVm: StatsViewModel by viewModels()
    private val settingsVm: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsVm.settings.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val dark = when (settings.themeMode) {
                1 -> false
                2 -> true
                else -> systemDark
            }
            NihongoTheme(darkTheme = dark) {
                // একটাই TextToSpeech — সব স্ক্রিন এটাই ব্যবহার করে
                val context = LocalContext.current
                val tts = remember { TtsHelper(context) }
                DisposableEffect(Unit) { onDispose { tts.shutdown() } }
                LaunchedEffect(settings.ttsRate) { tts.setRate(settings.ttsRate) }

                CompositionLocalProvider(LocalTts provides tts) {
                    NihongoApp()
                }
            }
        }
    }

    @Composable
    private fun NihongoApp() {
        val cs = MaterialTheme.colorScheme
        var tab by rememberSaveable { mutableStateOf(0) }
        // Home ট্যাবের ভেতরের পাতা (লেভেলের সেকশন, লেসন, টেস্ট…)
        var homeRoute by rememberSaveable { mutableStateOf(ROUTE_HUB) }

        // অন্য ট্যাবে থাকলে ব্যাক চাপলে Home-এ ফেরে
        BackHandler(enabled = tab != 0) { tab = 0 }
        // পরে রেজিস্টার হওয়া হ্যান্ডলার আগে চলে — তাই Home-এর ভেতরে থাকলে ব্যাক আগে এক ধাপ উপরে যায়
        BackHandler(enabled = tab == 0 && homeRoute != ROUTE_HUB) { homeRoute = parentRoute(homeRoute) }

        Scaffold(
            containerColor = cs.background,
            bottomBar = {
                NavigationBar(containerColor = cs.surfaceContainer, tonalElevation = 0.dp) {
                    val items = listOf(
                        "家" to "Home",
                        "探" to "Search",
                        "設" to "Settings"
                    )
                    items.forEachIndexed { i, (glyph, label) ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = {
                                // Home আবার চাপলে হোমের মূল পাতায় ফেরে
                                if (i == 0 && tab == 0) homeRoute = ROUTE_HUB
                                tab = i
                            },
                            icon = { Text(glyph, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = cs.onPrimaryContainer,
                                selectedTextColor = cs.onSurface,
                                indicatorColor = cs.primaryContainer,
                                unselectedIconColor = cs.onSurfaceVariant,
                                unselectedTextColor = cs.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad)) {
                Crossfade(targetState = tab, animationSpec = tween(220), label = "tabs") { t ->
                    when (t) {
                        0 -> HomeScreen(
                            route = homeRoute,
                            onRoute = { homeRoute = it },
                            statsVm = statsVm,
                            kanaVm = kanaVm,
                            kanaQuizVm = kanaQuizVm,
                            vocabQuizVm = vocabQuizVm
                        )
                        1 -> SearchScreen(searchVm)
                        else -> SettingsScreen(settingsVm, statsVm)
                    }
                }
            }
        }
    }
}
