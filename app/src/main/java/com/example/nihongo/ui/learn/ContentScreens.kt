package com.example.nihongo.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.data.ContentRepository
import com.example.nihongo.data.GrammarItem
import com.example.nihongo.data.Lesson
import com.example.nihongo.data.LessonInfo
import com.example.nihongo.data.KanjiItem
import com.example.nihongo.data.WordItem
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.EmptyState
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.SpeakButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val listPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)

@Composable
private fun Loading() {
    Text(
        "লোড হচ্ছে…",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(20.dp)
    )
}

// ---------------------------------------------------------------- লেসন তালিকা

/** type: "vocab" | "grammar" | "kanji"; ডেটা না থাকা লেসন ধূসর দেখায় ও চাপা যায় না */
@Composable
fun LessonListScreen(
    level: String,
    type: String,
    onOpen: (Int) -> Unit,
    top: (@Composable () -> Unit)? = null
) {
    val ctx = LocalContext.current
    val loaded by produceState<List<LessonInfo>?>(null, level) {
        value = withContext(Dispatchers.IO) { ContentRepository.get(ctx).lessonInfos(level) }
    }
    val list = loaded
    if (list == null) { Loading(); return }
    val cs = MaterialTheme.colorScheme
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (top != null) item { top() }
        items(list) { info ->
            val count = when (type) {
                "vocab" -> info.vocab
                "grammar" -> info.grammar
                else -> info.kanji
            }
            val unit = when (type) {
                "vocab" -> "টি শব্দ"
                "grammar" -> "টি পাঠ"
                else -> "টি কাঞ্জি"
            }
            val available = info.exists && count > 0
            SoftCard(
                Modifier.fillMaxWidth(),
                onClick = if (available) ({ onOpen(info.no) }) else null
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "L${info.no}",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (available) cs.primary else cs.outline,
                        modifier = Modifier.width(64.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (info.title.isNotEmpty()) info.title else "লেসন ${info.no}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (available) cs.onSurface else cs.outline
                        )
                        Text(
                            if (available) "$count$unit" else "ডেটা যোগ হয়নি",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    if (available) Text("›", style = MaterialTheme.typography.titleLarge, color = cs.outline)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- লেসনের ভেতরের পাতা

@Composable
fun LessonContent(level: String, type: String, no: Int) {
    val ctx = LocalContext.current
    val loaded by produceState<Lesson?>(null, level, no) {
        value = withContext(Dispatchers.IO) { ContentRepository.get(ctx).lesson(level, no) }
    }
    val lesson = loaded
    if (lesson == null) { Loading(); return }
    Column(Modifier.fillMaxSize()) {
        if (lesson.title.isNotEmpty()) {
            Text(
                lesson.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
        when (type) {
            "vocab" -> WordList(lesson.vocab)
            "grammar" -> GrammarList(lesson.grammar)
            else -> KanjiList(lesson.kanji)
        }
    }
}

@Composable
private fun EmptyLesson() {
    EmptyState("📭", "এই লেসনে এখনো ডেটা নেই", "JSON ফাইলে যোগ করলেই এখানে দেখাবে।")
}

// ---------------------------------------------------------------- কাঞ্জি

@Composable
fun KanjiList(list: List<KanjiItem>) {
    if (list.isEmpty()) { EmptyLesson(); return }
    val tts = LocalTts.current
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(list) { k -> KanjiRow(k) { tts.speak(k.exampleReading) } }
    }
}

@Composable
private fun KanjiRow(k: KanjiItem, onSpeak: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SoftCard(Modifier.fillMaxWidth(), onClick = onSpeak) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            BigText(k.kanji, 48.sp, color = cs.primary, weight = FontWeight.Medium)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(k.bn, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Text(k.en, style = MaterialTheme.typography.labelMedium, color = cs.outline)
                Text("音  ${k.onyomi}", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                Text("訓  ${k.kunyomi}", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                Text(
                    "${k.example}（${k.exampleReading}） — ${k.exampleBn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.tertiary
                )
            }
        }
    }
}

// ---------------------------------------------------------------- ব্যাকরণ

@Composable
fun GrammarList(list: List<GrammarItem>) {
    if (list.isEmpty()) { EmptyLesson(); return }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(list) { g -> GrammarCard(g) }
    }
}

@Composable
private fun GrammarCard(g: GrammarItem) {
    val cs = MaterialTheme.colorScheme
    val tts = LocalTts.current
    var open by rememberSaveable(g.title) { mutableStateOf(false) }
    SoftCard(Modifier.fillMaxWidth(), onClick = { open = !open }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    g.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(if (open) "▴" else "▾", color = cs.outline)
            }
            Text(g.pattern, style = MaterialTheme.typography.bodyMedium, color = cs.primary)
            if (open) {
                Text(g.explain, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                g.examples.forEach { e ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(e.jp, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                            Text(e.romaji, style = MaterialTheme.typography.labelMedium, color = cs.outline)
                            Text(e.bn, style = MaterialTheme.typography.bodySmall, color = cs.tertiary)
                        }
                        SpeakButton(onClick = { tts.speak(e.jp) }, diameter = 40.dp)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- শব্দ

@Composable
fun WordList(list: List<WordItem>) {
    if (list.isEmpty()) { EmptyLesson(); return }
    val tts = LocalTts.current
    val cs = MaterialTheme.colorScheme
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(list) { w ->
            SoftCard(Modifier.fillMaxWidth(), onClick = { tts.speak(w.reading) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        BigText(w.word, 26.sp, color = cs.onSurface, weight = FontWeight.Medium)
                        if (w.reading != w.word) {
                            Text(w.reading, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                        }
                        Text(w.romaji, style = MaterialTheme.typography.labelMedium, color = cs.outline)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            w.bn,
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.primary,
                            textAlign = TextAlign.End
                        )
                        Text(
                            w.en,
                            style = MaterialTheme.typography.labelMedium,
                            color = cs.outline,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- সংখ্যা

private data class Num(val digit: String, val kanji: String, val reading: String, val romaji: String)

private val basicNumbers = listOf(
    Num("0", "〇／零", "ゼロ／れい", "zero / rei"),
    Num("1", "一", "いち", "ichi"),
    Num("2", "二", "に", "ni"),
    Num("3", "三", "さん", "san"),
    Num("4", "四", "よん／し", "yon / shi"),
    Num("5", "五", "ご", "go"),
    Num("6", "六", "ろく", "roku"),
    Num("7", "七", "なな／しち", "nana / shichi"),
    Num("8", "八", "はち", "hachi"),
    Num("9", "九", "きゅう／く", "kyuu / ku"),
    Num("10", "十", "じゅう", "juu")
)

private val tensAndMore = listOf(
    Num("11", "十一", "じゅういち", "juu-ichi"),
    Num("20", "二十", "にじゅう", "ni-juu"),
    Num("30", "三十", "さんじゅう", "san-juu"),
    Num("40", "四十", "よんじゅう", "yon-juu"),
    Num("50", "五十", "ごじゅう", "go-juu"),
    Num("60", "六十", "ろくじゅう", "roku-juu"),
    Num("70", "七十", "ななじゅう", "nana-juu"),
    Num("80", "八十", "はちじゅう", "hachi-juu"),
    Num("90", "九十", "きゅうじゅう", "kyuu-juu"),
    Num("100", "百", "ひゃく", "hyaku"),
    Num("1,000", "千", "せん", "sen"),
    Num("10,000", "一万", "いちまん", "ichi-man")
)

private val irregularNumbers = listOf(
    Num("300", "三百", "さんびゃく", "san-byaku"),
    Num("600", "六百", "ろっぴゃく", "rop-pyaku"),
    Num("800", "八百", "はっぴゃく", "hap-pyaku"),
    Num("3,000", "三千", "さんぜん", "san-zen"),
    Num("8,000", "八千", "はっせん", "has-sen")
)

@Composable
fun NumbersScreen() {
    val tts = LocalTts.current
    val cs = MaterialTheme.colorScheme
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { NumHeader("১ থেকে ১০") }
        items(basicNumbers) { n -> NumRow(n) { tts.speak(n.reading.substringBefore("／")) } }
        item { NumHeader("দশক, শত, হাজার") }
        items(tensAndMore) { n -> NumRow(n) { tts.speak(n.reading) } }
        item { NumHeader("বিশেষ উচ্চারণ (শব্দ বদলে যায়)") }
        items(irregularNumbers) { n -> NumRow(n) { tts.speak(n.reading) } }
        item {
            Text(
                "৪, ৭ ও ৯-এর দুটি করে পড়া আছে। সাধারণত ৪ = よん, ৭ = なな, ৯ = きゅう বেশি চলে।",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun NumHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun NumRow(n: Num, onSpeak: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SoftCard(Modifier.fillMaxWidth(), onClick = onSpeak) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                n.digit,
                style = MaterialTheme.typography.titleLarge,
                color = cs.primary,
                modifier = Modifier.width(72.dp)
            )
            BigText(n.kanji, 24.sp, color = cs.onSurface, weight = FontWeight.Medium)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(n.reading, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Text(n.romaji, style = MaterialTheme.typography.labelMedium, color = cs.outline)
            }
        }
    }
}
