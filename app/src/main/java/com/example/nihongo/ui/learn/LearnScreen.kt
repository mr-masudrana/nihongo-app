package com.example.nihongo.ui.learn

import androidx.compose.foundation.background
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.AppIcon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.nihongo.data.ContentRepository
import com.example.nihongo.ui.components.bounceClick
import com.example.nihongo.ui.kana.KanaChartScreen
import com.example.nihongo.ui.kana.KanaViewModel
import com.example.nihongo.ui.quiz.KanaQuizViewModel
import com.example.nihongo.ui.quiz.VocabQuizViewModel

const val ROUTE_HUB = "hub"
const val ROUTE_HIRAGANA = "hiragana"
const val ROUTE_KATAKANA = "katakana"
const val ROUTE_NUMBER = "number"

// Starter A1 Routes
const val ROUTE_A1_VOCAB = "starter_a1/vocab"
const val ROUTE_A1_KANJI = "starter_a1/kanji"
const val ROUTE_A1_GRAMMAR = "starter_a1/grammar"
const val ROUTE_A1_ACTIVITIES = "starter_a1/activities"
const val ROUTE_A1_CANDO = "starter_a1/cando"
const val ROUTE_A1_TIPS = "starter_a1/tips"

// Elementary 1 (A2) Routes
const val ROUTE_A2_1_VOCAB = "elementary1_a2/vocab"
const val ROUTE_A2_1_KANJI = "elementary1_a2/kanji"
const val ROUTE_A2_1_GRAMMAR = "elementary1_a2/grammar"
const val ROUTE_A2_1_ACTIVITIES = "elementary1_a2/activities"
const val ROUTE_A2_1_CANDO = "elementary1_a2/cando"
const val ROUTE_A2_1_TIPS = "elementary1_a2/tips"

// Elementary 2 (A2) Routes
const val ROUTE_A2_2_VOCAB = "elementary2_a2/vocab"
const val ROUTE_A2_2_KANJI = "elementary2_a2/kanji"
const val ROUTE_A2_2_GRAMMAR = "elementary2_a2/grammar"
const val ROUTE_A2_2_ACTIVITIES = "elementary2_a2/activities"
const val ROUTE_A2_2_CANDO = "elementary2_a2/cando"
const val ROUTE_A2_2_TIPS = "elementary2_a2/tips"

private fun typeName(t: String) = when (t) {
    "vocab" -> "Vocabulary"
    "kanji" -> "Kanji"
    "grammar" -> "Grammar"
    "activities", "convo" -> "Activities"
    "cando" -> "Can-do"
    "tips" -> "Tips of life in Japan"
    else -> t.replaceFirstChar { it.uppercase() }
}

private fun crumb(route: String): Pair<String, String> {
    val base = route.substringBefore(':')
    val no = route.substringAfter(':', "").toIntOrNull()
    return when (base) {
        ROUTE_HIRAGANA -> "Basic" to "Hiragana"
        ROUTE_KATAKANA -> "Basic" to "Katakana"
        ROUTE_NUMBER -> "Basic" to "Number"
        else -> {
            val parts = base.split("/")
            val level = ContentRepository.levelDisplayName(parts[0])
            val type = typeName(parts.getOrElse(1) { "" })
            when {
                no == null -> level to type
                parts.getOrNull(1) == "kanji" -> "$level · $type" to "${no + 1}"
                else -> "$level · $type" to "Lesson $no"
            }
        }
    }
}

/** ব্যাক চাপলে এক ধাপ উপরে: লেসন → লেসন তালিকা → Home সূচি */
fun parentRoute(route: String): String = when {
    ':' in route -> route.substringBefore(':')
    else -> ROUTE_HUB
}

/**
 * হোমের কোনো টাইল চাপলে এই স্ক্রিন খোলে (ROUTE_HUB-এর জন্য নয় — সেটা HomeScreen দেখায়)।
 */
@Composable
fun LearnScreen(
    route: String,
    onRoute: (String) -> Unit,
    kanaVm: KanaViewModel,
    kanaQuizVm: KanaQuizViewModel,
    vocabQuizVm: VocabQuizViewModel
) {
    val (level, name) = crumb(route)
    Column(Modifier.fillMaxSize()) {
        SectionBar(level, name, onBack = { onRoute(parentRoute(route)) })
        val base = route.substringBefore(':')
        val no = route.substringAfter(':', "").toIntOrNull()
        val parts = base.split("/")
        when {
            base == ROUTE_HIRAGANA -> KanaChartScreen(kanaVm, "hiragana")
            base == ROUTE_KATAKANA -> KanaChartScreen(kanaVm, "katakana")
            base == ROUTE_NUMBER -> NumberScreen()
            parts.getOrNull(1) == "kanji" && no != null ->
                KanjiDetailScreen(parts[0], no, onNavigate = { onRoute("$base:$it") })
            parts.getOrNull(1) == "kanji" ->
                KanjiGridScreen(parts[0], onOpen = { onRoute("$base:$it") })
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
            AppIcon(AppIcons.Back, size = 22.dp, tint = cs.onSurface)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(level, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            Text(name, style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
        }
    }
}
