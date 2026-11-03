package com.example.nihongo.ui.learn

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.bounceClick
import com.example.nihongo.ui.kana.KanaChartScreen
import com.example.nihongo.ui.kana.KanaViewModel

const val ROUTE_HUB = "hub"
const val ROUTE_HIRAGANA = "hiragana"
const val ROUTE_KATAKANA = "katakana"
const val ROUTE_NUMBERS = "numbers"
const val ROUTE_N5_VOCAB = "n5_vocab"
const val ROUTE_N5_GRAMMAR = "n5_grammar"
const val ROUTE_N5_KANJI = "n5_kanji"
const val ROUTE_N4_VOCAB = "n4_vocab"
const val ROUTE_N4_GRAMMAR = "n4_grammar"
const val ROUTE_N4_KANJI = "n4_kanji"

private data class LearnItem(val glyph: String, val title: String, val sub: String, val route: String)

private val basicItems = listOf(
    LearnItem("あ", "Hiragana", "৪৬ মূল অক্ষর, দাকুতেন ও যৌগিক — চার্ট ও উচ্চারণ", ROUTE_HIRAGANA),
    LearnItem("ア", "Katakana", "বিদেশি শব্দের জন্য কানা, চার্ট ও উচ্চারণ", ROUTE_KATAKANA),
    LearnItem("数", "Number", "১ থেকে ১০,০০০ ও বিশেষ উচ্চারণ", ROUTE_NUMBERS)
)
private val n5Items = listOf(
    LearnItem("語", "Vocabulary", "লেসন অনুযায়ী শব্দ (লেসন ১–২৫)", ROUTE_N5_VOCAB),
    LearnItem("文", "Grammar", "লেসন অনুযায়ী ব্যাকরণ (লেসন ১–২৫)", ROUTE_N5_GRAMMAR),
    LearnItem("字", "Kanji", "লেসন অনুযায়ী কাঞ্জি (লেসন ১–২৫)", ROUTE_N5_KANJI)
)
private val n4Items = listOf(
    LearnItem("語", "Vocabulary", "লেসন অনুযায়ী শব্দ (লেসন ২৬–৫০)", ROUTE_N4_VOCAB),
    LearnItem("文", "Grammar", "লেসন অনুযায়ী ব্যাকরণ (লেসন ২৬–৫০)", ROUTE_N4_GRAMMAR),
    LearnItem("字", "Kanji", "লেসন অনুযায়ী কাঞ্জি (লেসন ২৬–৫০)", ROUTE_N4_KANJI)
)

private fun typeName(t: String) = when (t) {
    "vocab" -> "Vocabulary"
    "grammar" -> "Grammar"
    else -> "Kanji"
}

private fun crumb(route: String): Pair<String, String> {
    val base = route.substringBefore(':')
    val no = route.substringAfter(':', "").toIntOrNull()
    return when (base) {
        ROUTE_HIRAGANA -> "Basic" to "Hiragana"
        ROUTE_KATAKANA -> "Basic" to "Katakana"
        ROUTE_NUMBERS -> "Basic" to "Number"
        else -> {
            val parts = base.split("_")
            val level = parts[0].uppercase()
            val type = typeName(parts.getOrElse(1) { "" })
            if (no == null) level to type else "$level · $type" to "Lesson $no"
        }
    }
}

/** ব্যাক চাপলে এক ধাপ উপরে: লেসন → লেসন তালিকা → Learn সূচি */
fun parentRoute(route: String): String = when {
    ':' in route -> route.substringBefore(':')
    else -> ROUTE_HUB
}

/**
 * route: ROUTE_HUB হলে লেভেল-ভিত্তিক সূচি; নইলে নির্বাচিত সেকশন।
 */
@Composable
fun LearnScreen(
    route: String,
    onRoute: (String) -> Unit,
    kanaVm: KanaViewModel
) {
    if (route == ROUTE_HUB) {
        LearnHub(onRoute)
        return
    }
    val (level, name) = crumb(route)
    Column(Modifier.fillMaxSize()) {
        SectionBar(level, name, onBack = { onRoute(parentRoute(route)) })
        val base = route.substringBefore(':')
        val no = route.substringAfter(':', "").toIntOrNull()
        val parts = base.split("_")
        when {
            base == ROUTE_HIRAGANA -> KanaChartScreen(kanaVm, "hiragana")
            base == ROUTE_KATAKANA -> KanaChartScreen(kanaVm, "katakana")
            base == ROUTE_NUMBERS -> NumbersScreen()
            no != null -> LessonContent(parts[0], parts.getOrElse(1) { "vocab" }, no)
            else -> LessonListScreen(
                level = parts[0],
                type = parts.getOrElse(1) { "vocab" },
                onOpen = { onRoute("$base:$it") }
            )
        }
    }
}

@Composable
private fun SectionBar(level: String, name: String, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .bounceClick(onClick = onBack)
                .clip(CircleShape)
                .background(cs.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Text("‹", fontSize = 24.sp, color = cs.onSurface)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(level, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            Text(name, style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
        }
    }
}

@Composable
private fun LearnHub(onRoute: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Learn", "ধাপে ধাপে জাপানি শিখুন")
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LevelBlock("Basic", "শুরুর ভিত্তি", basicItems, onRoute)
            LevelBlock("N5", "প্রাথমিক স্তর", n5Items, onRoute)
            LevelBlock("N4", "মাধ্যমিক স্তর", n4Items, onRoute)
        }
    }
}

@Composable
private fun LevelBlock(level: String, subtitle: String, items: List<LearnItem>, onRoute: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(cs.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(level, style = MaterialTheme.typography.titleSmall, color = cs.onPrimaryContainer)
            }
            Spacer(Modifier.width(10.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        }
        items.forEach { item ->
            SoftCard(Modifier.fillMaxWidth(), onClick = { onRoute(item.route) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    BigText(item.glyph, 28.sp, color = cs.primary, weight = FontWeight.Bold)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                        Text(item.sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    Text("›", style = MaterialTheme.typography.titleLarge, color = cs.outline)
                }
            }
        }
    }
}
