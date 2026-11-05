package com.example.nihongo.ui.home

import com.example.nihongo.ui.components.tr
import androidx.compose.foundation.background
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.AppIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.components.AppProgressBar
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ProgressRing
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.entrance
import com.example.nihongo.ui.kana.KanaViewModel
import com.example.nihongo.ui.learn.LearnScreen
import com.example.nihongo.ui.learn.ROUTE_NUMBER
import com.example.nihongo.ui.learn.ROUTE_SENTENCES
import com.example.nihongo.ui.learn.ROUTE_N4_CONVO
import com.example.nihongo.ui.learn.ROUTE_N5_CONVO
import com.example.nihongo.ui.learn.ROUTE_HIRAGANA
import com.example.nihongo.ui.learn.ROUTE_HUB
import com.example.nihongo.ui.learn.ROUTE_KATAKANA
import com.example.nihongo.ui.learn.ROUTE_N4_GRAMMAR
import com.example.nihongo.ui.learn.ROUTE_N4_KANJI
import com.example.nihongo.ui.learn.ROUTE_N4_VOCAB
import com.example.nihongo.ui.learn.ROUTE_N5_GRAMMAR
import com.example.nihongo.ui.learn.ROUTE_N5_KANJI
import com.example.nihongo.ui.learn.ROUTE_N5_VOCAB
import com.example.nihongo.ui.learn.ROUTE_TEST_KANA
import com.example.nihongo.ui.learn.ROUTE_TEST_N4
import com.example.nihongo.ui.learn.ROUTE_TEST_N5
import com.example.nihongo.ui.quiz.KanaQuizViewModel
import com.example.nihongo.ui.quiz.VocabQuizViewModel
import com.example.nihongo.ui.stats.StatsViewModel
import java.util.Calendar

/**
 * Home ট্যাব: route == ROUTE_HUB হলে নতুন হোম (তিন লেভেলের সেকশন);
 * নইলে নির্বাচিত সেকশন/পাতা (LearnScreen)।
 */
@Composable
fun HomeScreen(
    route: String,
    onRoute: (String) -> Unit,
    statsVm: StatsViewModel,
    kanaVm: KanaViewModel,
    kanaQuizVm: KanaQuizViewModel,
    vocabQuizVm: VocabQuizViewModel
) {
    if (route == ROUTE_HUB) {
        HomeHub(statsVm, onRoute)
    } else {
        LearnScreen(route, onRoute, kanaVm, kanaQuizVm, vocabQuizVm)
    }
}

private data class Tile(val glyph: String, val title: String, val route: String)

private val basicTiles = listOf(
    Tile("あ", "Hiragana", ROUTE_HIRAGANA),
    Tile("ア", "Katakana", ROUTE_KATAKANA),
    Tile("数", "Number", ROUTE_NUMBER),
    Tile("言", "Sentences", ROUTE_SENTENCES),
    Tile("問", "Test", ROUTE_TEST_KANA)
)
private val n5Tiles = listOf(
    Tile("語", "Vocabulary", ROUTE_N5_VOCAB),
    Tile("字", "Kanji", ROUTE_N5_KANJI),
    Tile("文", "Grammar", ROUTE_N5_GRAMMAR),
    Tile("話", "Dialogue", ROUTE_N5_CONVO),
    Tile("問", "Test", ROUTE_TEST_N5)
)
private val n4Tiles = listOf(
    Tile("語", "Vocabulary", ROUTE_N4_VOCAB),
    Tile("字", "Kanji", ROUTE_N4_KANJI),
    Tile("文", "Grammar", ROUTE_N4_GRAMMAR),
    Tile("話", "Dialogue", ROUTE_N4_CONVO),
    Tile("問", "Test", ROUTE_TEST_N4)
)

@Composable
private fun greeting(): Triple<String, String, String> {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 11 -> Triple("おはよう", "Ohayou", tr("সুপ্রভাত", "Good morning"))
        h < 18 -> Triple("こんにちは", "Konnichiwa", tr("শুভ দুপুর", "Good afternoon"))
        else -> Triple("こんばんは", "Konbanwa", tr("শুভ সন্ধ্যা", "Good evening"))
    }
}

@Composable
private fun HomeHub(statsVm: StatsViewModel, onRoute: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val s by statsVm.stats.collectAsState()
    val today = remember(s) { Gamification.dayOf() }
    val streak = Gamification.currentStreak(s, today)
    val greet = greeting()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        HomeHeader(greet, streak)

        LevelSection(
            "BASIC", basicTiles, onRoute,
            accent = cs.primary, onAccent = cs.onPrimary
        )
        LevelSection(
            "N5", n5Tiles, onRoute,
            accent = cs.secondary, onAccent = cs.onSecondary
        )
        LevelSection(
            "N4", n4Tiles, onRoute,
            accent = cs.tertiary, onAccent = cs.onTertiary
        )
    }
}

@Composable
private fun HomeHeader(greet: Triple<String, String, String>, streak: Int) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 20.dp, top = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${greet.second} · ${greet.third}",
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurfaceVariant
            )
            BigText(greet.first, 32.sp, color = cs.onBackground, weight = FontWeight.Bold)
        }
        Row(
            Modifier
                .clip(CircleShape)
                .background(cs.surfaceContainerHigh)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(AppIcons.Fire, size = 18.dp, tint = Color(0xFFFF7043))
            Spacer(Modifier.width(6.dp))
            Text("$streak", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
        }
    }
}

@Composable
private fun LevelSection(
    label: String,
    tiles: List<Tile>,
    onRoute: (String) -> Unit,
    accent: Color,
    onAccent: Color
) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.padding(horizontal = 20.dp).entrance(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(width = 4.dp, height = 24.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
        }
        tiles.chunked(3).forEach { rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowTiles.forEach { t ->
                    TileCard(
                        t, accent = accent, onAccent = onAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onRoute(t.route) }
                    )
                }
                // শেষ সারিতে কার্ড কম হলে ফাঁকা জায়গা রেখে প্রতিটি কার্ডের মাপ সমান রাখি
                repeat(3 - rowTiles.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TileCard(
    t: Tile,
    accent: Color,
    onAccent: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    SoftCard(
        modifier.height(88.dp),
        color = cs.surfaceContainerLow,
        borderColor = cs.outlineVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Column(
            Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                BigText(t.glyph, 20.sp, color = accent, weight = FontWeight.Bold)
            }
            Text(
                t.title,
                style = MaterialTheme.typography.titleSmall,
                color = cs.onSurface,
                maxLines = 1
            )
        }
    }
}
