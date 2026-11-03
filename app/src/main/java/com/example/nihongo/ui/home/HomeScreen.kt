package com.example.nihongo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ProgressRing
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.entrance
import com.example.nihongo.ui.kana.KanaViewModel
import com.example.nihongo.ui.learn.LearnScreen
import com.example.nihongo.ui.learn.ROUTE_COUNTING
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

private data class Tile(val glyph: String, val title: String, val sub: String, val route: String, val isTest: Boolean = false)

private val basicTiles = listOf(
    Tile("あ", "Hiragana", "চার্ট ও উচ্চারণ", ROUTE_HIRAGANA),
    Tile("ア", "Katakana", "চার্ট ও উচ্চারণ", ROUTE_KATAKANA),
    Tile("数", "Counting", "সংখ্যা ও গোনা", ROUTE_COUNTING),
    Tile("問", "Test", "কানা কুইজ", ROUTE_TEST_KANA, isTest = true)
)
private val n5Tiles = listOf(
    Tile("語", "Vocabulary", "লেসন ১–২৫", ROUTE_N5_VOCAB),
    Tile("字", "Kanji", "১২০টি কাঞ্জি", ROUTE_N5_KANJI),
    Tile("文", "Grammar", "লেসন ১–২৫", ROUTE_N5_GRAMMAR),
    Tile("問", "Test", "শব্দ কুইজ", ROUTE_TEST_N5, isTest = true)
)
private val n4Tiles = listOf(
    Tile("語", "Vocabulary", "লেসন ২৬–৫০", ROUTE_N4_VOCAB),
    Tile("字", "Kanji", "কাঞ্জি তালিকা", ROUTE_N4_KANJI),
    Tile("文", "Grammar", "লেসন ২৬–৫০", ROUTE_N4_GRAMMAR),
    Tile("問", "Test", "শব্দ কুইজ", ROUTE_TEST_N4, isTest = true)
)

private fun greeting(): Triple<String, String, String> {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 11 -> Triple("おはよう", "Ohayou", "সুপ্রভাত")
        h < 18 -> Triple("こんにちは", "Konnichiwa", "শুভ দুপুর")
        else -> Triple("こんばんは", "Konbanwa", "শুভ সন্ধ্যা")
    }
}

@Composable
private fun HomeHub(statsVm: StatsViewModel, onRoute: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val s by statsVm.stats.collectAsState()
    val goal by statsVm.dailyGoal.collectAsState()
    val today = remember(s) { Gamification.dayOf() }
    val xpToday = Gamification.xpToday(s, today)
    val streak = Gamification.currentStreak(s, today)
    val greet = remember { greeting() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // শুভেচ্ছা
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
            Text(
                "${greet.second} · ${greet.third}",
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurfaceVariant
            )
            BigText(greet.first, 34.sp, color = cs.onBackground, weight = FontWeight.Bold)
        }

        HeroCard(xpToday, goal, Gamification.level(s.xp), streak)

        LevelSection(
            "BASIC", "শুরুর ভিত্তি", basicTiles, onRoute,
            accent = cs.primary, onAccent = cs.onPrimary,
            tint = cs.primaryContainer, onTint = cs.onPrimaryContainer
        )
        LevelSection(
            "N5", "প্রাথমিক স্তর", n5Tiles, onRoute,
            accent = cs.secondary, onAccent = cs.onSecondary,
            tint = cs.secondaryContainer, onTint = cs.onSecondaryContainer
        )
        LevelSection(
            "N4", "মাধ্যমিক স্তর", n4Tiles, onRoute,
            accent = cs.tertiary, onAccent = cs.onTertiary,
            tint = cs.tertiaryContainer, onTint = cs.onTertiaryContainer
        )
    }
}

@Composable
private fun HeroCard(xpToday: Int, goal: Int, level: Int, streak: Int) {
    val done = xpToday >= goal
    val brand = Brush.linearGradient(listOf(Color(0xFF3F51B5), Color(0xFF7E57C2), Color(0xFFD86C9A)))
    Box(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .entrance()
            .clip(RoundedCornerShape(28.dp))
            .background(brand)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = if (goal > 0) xpToday.toFloat() / goal else 0f,
                diameter = 92.dp, strokeWidth = 10.dp,
                color = Color.White, trackColor = Color.White.copy(alpha = 0.25f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BigText("$xpToday", 24.sp, color = Color.White, weight = FontWeight.Bold)
                    Text("/ $goal", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("আজকের লক্ষ্য", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    if (done) "✅ লক্ষ্য পূর্ণ! চমৎকার।" else "আর ${goal - xpToday} XP বাকি — কুইজ দিয়ে জিতুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroPill("Lv $level")
                    HeroPill("🔥 $streak দিন")
                }
            }
        }
    }
}

@Composable
private fun HeroPill(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
private fun LevelSection(
    label: String,
    subtitle: String,
    tiles: List<Tile>,
    onRoute: (String) -> Unit,
    accent: Color,
    onAccent: Color,
    tint: Color,
    onTint: Color
) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.padding(horizontal = 20.dp).entrance(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = onAccent)
            }
            Spacer(Modifier.width(12.dp))
            Text(subtitle, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
        }
        tiles.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { t ->
                    TileCard(
                        t,
                        container = if (t.isTest) accent else tint,
                        contentColor = if (t.isTest) onAccent else onTint,
                        glyphColor = if (t.isTest) onAccent else accent,
                        modifier = Modifier.weight(1f),
                        onClick = { onRoute(t.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TileCard(
    t: Tile,
    container: Color,
    contentColor: Color,
    glyphColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    SoftCard(
        modifier.height(118.dp),
        color = container,
        borderColor = container,
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BigText(t.glyph, 32.sp, color = glyphColor, weight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("›", style = MaterialTheme.typography.titleLarge, color = contentColor.copy(alpha = 0.7f))
            }
            Column {
                Text(t.title, style = MaterialTheme.typography.titleMedium, color = contentColor)
                Text(t.sub, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
            }
        }
    }
}
