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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.entrance
import com.example.nihongo.ui.kana.KanaViewModel
import com.example.nihongo.ui.learn.*
import com.example.nihongo.ui.quiz.KanaQuizViewModel
import com.example.nihongo.ui.quiz.VocabQuizViewModel
import com.example.nihongo.ui.stats.StatsViewModel
import java.util.Calendar

/**
 * Home ট্যাব: route == ROUTE_HUB হলে হোম সেকশনসমূহ;
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
    Tile("数", "Number", ROUTE_NUMBER)
)

private val starterA1Tiles = listOf(
    Tile("語", "Vocabulary", ROUTE_A1_VOCAB),
    Tile("字", "Kanji", ROUTE_A1_KANJI),
    Tile("文", "Grammar", ROUTE_A1_GRAMMAR),
    Tile("話", "Activities", ROUTE_A1_ACTIVITIES),
    Tile("力", "Can-do", ROUTE_A1_CANDO),
    Tile("暮", "Tips of life in Japan", ROUTE_A1_TIPS)
)

private val elementary1A2Tiles = listOf(
    Tile("語", "Vocabulary", ROUTE_A2_1_VOCAB),
    Tile("字", "Kanji", ROUTE_A2_1_KANJI),
    Tile("文", "Grammar", ROUTE_A2_1_GRAMMAR),
    Tile("話", "Activities", ROUTE_A2_1_ACTIVITIES),
    Tile("力", "Can-do", ROUTE_A2_1_CANDO),
    Tile("暮", "Tips of life in Japan", ROUTE_A2_1_TIPS)
)

private val elementary2A2Tiles = listOf(
    Tile("語", "Vocabulary", ROUTE_A2_2_VOCAB),
    Tile("字", "Kanji", ROUTE_A2_2_KANJI),
    Tile("文", "Grammar", ROUTE_A2_2_GRAMMAR),
    Tile("話", "Activities", ROUTE_A2_2_ACTIVITIES),
    Tile("力", "Can-do", ROUTE_A2_2_CANDO),
    Tile("暮", "Tips of life in Japan", ROUTE_A2_2_TIPS)
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HomeHeader(greet, streak)

        LevelSection(
            "BASIC", basicTiles, onRoute,
            accent = cs.primary, onAccent = cs.onPrimary
        )
        LevelSection(
            "IRODORI · STARTER A1", starterA1Tiles, onRoute,
            accent = cs.secondary, onAccent = cs.onSecondary
        )
        LevelSection(
            "IRODORI · ELEMENTARY 1 (A2)", elementary1A2Tiles, onRoute,
            accent = cs.tertiary, onAccent = cs.onTertiary
        )
        LevelSection(
            "IRODORI · ELEMENTARY 2 (A2)", elementary2A2Tiles, onRoute,
            accent = cs.primary, onAccent = cs.onPrimary
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
            Text(
                tr("JFT-Basic · Irodori সিলেবাস (A1 - A2)", "JFT-Basic · Irodori Syllabus (A1 - A2)"),
                style = MaterialTheme.typography.labelMedium,
                color = cs.primary
            )
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
            Text(label, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
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
        modifier.height(94.dp),
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
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                BigText(t.glyph, 18.sp, color = accent, weight = FontWeight.Bold)
            }
            Text(
                t.title,
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}
