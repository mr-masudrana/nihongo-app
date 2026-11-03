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
import com.example.nihongo.data.GrammarLesson
import com.example.nihongo.data.LessonInfo
import com.example.nihongo.data.VocabLesson
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

/** type: "vocab" | "grammar"; ডেটা না থাকা লেসন ধূসর দেখায় ও চাপা যায় না */
@Composable
fun LessonListScreen(
    level: String,
    type: String,
    onOpen: (Int) -> Unit,
    top: (@Composable () -> Unit)? = null
) {
    val ctx = LocalContext.current
    val loaded by produceState<List<LessonInfo>?>(null, level, type) {
        value = withContext(Dispatchers.IO) { ContentRepository.get(ctx).lessonInfos(level, type) }
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
            val count = info.count
            val unit = if (type == "vocab") "টি শব্দ" else "টি পাঠ"
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
    if (type == "vocab") VocabLessonContent(level, no) else GrammarLessonContent(level, no)
}

@Composable
private fun LessonTitle(title: String) {
    if (title.isNotEmpty()) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun VocabLessonContent(level: String, no: Int) {
    val ctx = LocalContext.current
    val loaded by produceState<VocabLesson?>(null, level, no) {
        value = withContext(Dispatchers.IO) { ContentRepository.get(ctx).vocabLesson(level, no) }
    }
    val lesson = loaded
    if (lesson == null) { Loading(); return }
    Column(Modifier.fillMaxSize()) {
        LessonTitle(lesson.title)
        WordList(lesson.words)
    }
}

@Composable
private fun GrammarLessonContent(level: String, no: Int) {
    val ctx = LocalContext.current
    val loaded by produceState<GrammarLesson?>(null, level, no) {
        value = withContext(Dispatchers.IO) { ContentRepository.get(ctx).grammarLesson(level, no) }
    }
    val lesson = loaded
    if (lesson == null) { Loading(); return }
    Column(Modifier.fillMaxSize()) {
        LessonTitle(lesson.title)
        GrammarList(lesson.points)
    }
}

@Composable
private fun EmptyLesson() {
    EmptyState("📭", "এই লেসনে এখনো ডেটা নেই", "JSON ফাইলে যোগ করলেই এখানে দেখাবে।")
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
                if (g.note.isNotBlank()) {
                    Text(
                        "📝 ${g.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.tertiary
                    )
                }
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
                        if (w.note.isNotBlank()) {
                            Text(
                                w.note,
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.tertiary,
                                textAlign = TextAlign.End
                            )
                        }
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

private val tsuCounters = listOf(
    Num("1つ", "一つ", "ひとつ", "hitotsu"),
    Num("2つ", "二つ", "ふたつ", "futatsu"),
    Num("3つ", "三つ", "みっつ", "mittsu"),
    Num("4つ", "四つ", "よっつ", "yottsu"),
    Num("5つ", "五つ", "いつつ", "itsutsu"),
    Num("6つ", "六つ", "むっつ", "muttsu"),
    Num("7つ", "七つ", "ななつ", "nanatsu"),
    Num("8つ", "八つ", "やっつ", "yattsu"),
    Num("9つ", "九つ", "ここのつ", "kokonotsu"),
    Num("10", "十", "とお", "too")
)

private val personCounters = listOf(
    Num("1人", "一人", "ひとり", "hitori"),
    Num("2人", "二人", "ふたり", "futari"),
    Num("3人", "三人", "さんにん", "sannin"),
    Num("4人", "四人", "よにん", "yonin"),
    Num("5人", "五人", "ごにん", "gonin"),
    Num("6人", "六人", "ろくにん", "rokunin"),
    Num("7人", "七人", "ななにん／しちにん", "nananin / shichinin"),
    Num("8人", "八人", "はちにん", "hachinin"),
    Num("9人", "九人", "きゅうにん", "kyuunin"),
    Num("10人", "十人", "じゅうにん", "juunin")
)

private val pieceCounters = listOf(
    Num("1個", "一個", "いっこ", "ikko"),
    Num("2個", "二個", "にこ", "niko"),
    Num("3個", "三個", "さんこ", "sanko"),
    Num("4個", "四個", "よんこ", "yonko"),
    Num("5個", "五個", "ごこ", "goko"),
    Num("6個", "六個", "ろっこ", "rokko"),
    Num("7個", "七個", "ななこ", "nanako"),
    Num("8個", "八個", "はっこ", "hakko"),
    Num("9個", "九個", "きゅうこ", "kyuuko"),
    Num("10個", "十個", "じゅっこ", "jukko")
)

private val irregularNumbers = listOf(
    Num("300", "三百", "さんびゃく", "san-byaku"),
    Num("600", "六百", "ろっぴゃく", "rop-pyaku"),
    Num("800", "八百", "はっぴゃく", "hap-pyaku"),
    Num("3,000", "三千", "さんぜん", "san-zen"),
    Num("8,000", "八千", "はっせん", "has-sen")
)

@Composable
fun CountingScreen() {
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
        item { NumHeader("গোনার ধরন: ～つ (সাধারণ জিনিস, ১০ পর্যন্ত)") }
        items(tsuCounters) { n -> NumRow(n) { tts.speak(n.reading) } }
        item { NumHeader("গোনার ধরন: ～人 (মানুষ)") }
        items(personCounters) { n -> NumRow(n) { tts.speak(n.reading.substringBefore("／")) } }
        item { NumHeader("গোনার ধরন: ～個 (ছোট জিনিস)") }
        items(pieceCounters) { n -> NumRow(n) { tts.speak(n.reading) } }
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
