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

// Book routes (Book -> Lesson List with expandable dropdowns -> Lesson Detail)
const val ROUTE_BOOK_A1 = "starter_a1"
const val ROUTE_BOOK_A2_1 = "elementary1_a2"
const val ROUTE_BOOK_A2_2 = "elementary2_a2"

private fun typeName(t: String) = when (t) {
    "vocab" -> "Vocabulary"
    "kanji" -> "Kanji"
    "grammar" -> "Grammar"
    "activities", "convo" -> "Activities"
    "cando" -> "Can-do"
    "tips" -> "Life of live in Japan"
    else -> t.replaceFirstChar { it.uppercase() }
}

private fun crumb(route: String): Pair<String, String> {
    val base = route.substringBefore(':')
    val no = route.substringAfter(':', "").toIntOrNull()
    return when (base) {
        ROUTE_HIRAGANA -> "Basic" to "Hiragana"
        ROUTE_KATAKANA -> "Basic" to "Katakana"
        ROUTE_NUMBER -> "Basic" to "Number"
        ROUTE_BOOK_A1, ROUTE_BOOK_A2_1, ROUTE_BOOK_A2_2 ->
            "Irodori" to ContentRepository.levelDisplayName(base)
        else -> {
            val parts = base.split("/")
            val level = ContentRepository.levelDisplayName(parts[0])
            val type = typeName(parts.getOrElse(1) { "" })
            when {
                no == null -> level to type
                parts.getOrNull(1) == "kanji_detail" -> "$level · Kanji" to "${no + 1}"
                else -> "$level · Lesson $no" to type
            }
        }
    }
}

/** ব্যাক চাপলে এক ধাপ উপরে: লেসন কনটেন্ট → বইয়ের লেসন তালিকা → Home বইয়ের তালিকা */
fun parentRoute(route: String): String = when {
    ':' in route -> route.substringBefore('/').let { book ->
        if (book in listOf(ROUTE_BOOK_A1, ROUTE_BOOK_A2_1, ROUTE_BOOK_A2_2)) book else ROUTE_HUB
    }
    '/' in route -> route.substringBefore('/')
    else -> ROUTE_HUB
}

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
            base in listOf(ROUTE_BOOK_A1, ROUTE_BOOK_A2_1, ROUTE_BOOK_A2_2) ->
                BookLessonsScreen(
                    level = base,
                    onOpenSection = { lessonNo, type -> onRoute("$base/$type:$lessonNo") },
                    onOpenKanjiAll = { onRoute("$base/kanji") }
                )
            parts.getOrNull(1) == "kanji_detail" && no != null ->
                KanjiDetailScreen(parts[0], no, onNavigate = { onRoute("$base:$it") })
            parts.getOrNull(1) == "kanji" && no != null ->
                LessonKanjiScreen(parts[0], no, onOpenDetail = { idx -> onRoute("${parts[0]}/kanji_detail:$idx") })
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
