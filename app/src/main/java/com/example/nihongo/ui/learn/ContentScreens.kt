package com.example.nihongo.ui.learn

import com.example.nihongo.ui.components.LocalEnglish
import com.example.nihongo.ui.components.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.AppIcon
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
import androidx.compose.runtime.collectAsState
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
import com.example.nihongo.data.SettingsRepository
import com.example.nihongo.data.WordField
import com.example.nihongo.data.ConvoLesson
import com.example.nihongo.data.Dialogue
import com.example.nihongo.data.SentenceItem
import com.example.nihongo.data.SentenceSection
import com.example.nihongo.data.GrammarItem
import com.example.nihongo.data.GrammarLesson
import com.example.nihongo.data.LessonInfo
import com.example.nihongo.data.NumberSection
import com.example.nihongo.data.NumberItem
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
        tr("লোড হচ্ছে…", "Loading…"),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(20.dp)
    )
}

// ---------------------------------------------------------------- লেসন তালিকা

/** type: "vocab" | "grammar" | "convo"; ডেটা না থাকা লেসন ধূসর দেখায় ও চাপা যায় না */
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
            val unit = when (type) {
                "vocab" -> tr("টি শব্দ", " words")
                "convo" -> tr("টি কথোপকথন", " dialogues")
                else -> tr("টি পাঠ", " points")
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
                            if (info.title.isNotEmpty()) tr(info.title, info.titleEn) else tr("লেসন ${info.no}", "Lesson ${info.no}"),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (available) cs.onSurface else cs.outline
                        )
                        Text(
                            if (available) "$count$unit" else tr("ডেটা যোগ হয়নি", "No data yet"),
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    if (available) AppIcon(AppIcons.ChevronRight, size = 24.dp, tint = cs.outline)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- লেসনের ভেতরের পাতা

@Composable
fun LessonContent(level: String, type: String, no: Int) {
    when (type) {
        "vocab" -> VocabLessonContent(level, no)
        "convo" -> ConvoLessonContent(level, no)
        else -> GrammarLessonContent(level, no)
    }
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
        LessonTitle(tr(lesson.title, lesson.titleEn))
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
        LessonTitle(tr(lesson.title, lesson.titleEn))
        GrammarList(lesson.points)
    }
}

@Composable
private fun EmptyLesson() {
    EmptyState(AppIcons.Inbox, tr("এই লেসনে এখনো ডেটা নেই", "No data in this lesson yet"), tr("JSON ফাইলে যোগ করলেই এখানে দেখাবে।", "It will appear here once you add it to the JSON file."))
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
                    tr(g.title, g.titleEn),
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(if (open) "▴" else "▾", color = cs.outline)
            }
            Text(tr(g.pattern, g.patternEn), style = MaterialTheme.typography.bodyMedium, color = cs.primary)
            if (open) {
                Text(tr(g.explain, g.explainEn), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                if (g.note.isNotBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        AppIcon(AppIcons.Info, size = 16.dp, tint = cs.tertiary, modifier = Modifier.padding(top = 2.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            tr(g.note, g.noteEn),
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.tertiary
                        )
                    }
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
                            Text(tr(e.bn, e.en), style = MaterialTheme.typography.bodySmall, color = cs.tertiary)
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
    val ctx = LocalContext.current
    val settings by SettingsRepository.get(ctx).settings.collectAsState()
    val tts = LocalTts.current
    val cs = MaterialTheme.colorScheme
    val english = LocalEnglish.current
    val showWord = settings.shows(WordField.WORD)
    val showRomaji = settings.shows(WordField.ROMAJI)
    val showBn = settings.shows(WordField.BN)
    val showEn = settings.shows(WordField.EN)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(list) { w ->
            // মূল অর্থ = অ্যাপের ভাষা অনুযায়ী (ইংরেজি না থাকলে বাংলা)
            val mainMeaning = if (english && w.en.isNotBlank()) w.en else w.bn
            val otherMeaning = when {
                english && showBn -> w.bn
                !english && showEn -> w.en
                else -> ""
            }
            SoftCard(Modifier.fillMaxWidth(), onClick = { tts.speak(w.reading) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        // কানা পড়া — মূল অংশ, সবসময় বড় করে
                        BigText(w.reading, 26.sp, color = cs.onSurface, weight = FontWeight.Medium)
                        if (showWord && w.word != w.reading) {
                            Text(w.word, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                        }
                        if (showRomaji) {
                            Text(w.romaji, style = MaterialTheme.typography.labelMedium, color = cs.outline)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            mainMeaning,
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.primary,
                            textAlign = TextAlign.End
                        )
                        if (otherMeaning.isNotBlank() && otherMeaning != mainMeaning) {
                            Text(
                                otherMeaning,
                                style = MaterialTheme.typography.labelMedium,
                                color = cs.outline,
                                textAlign = TextAlign.End
                            )
                        }
                        if (showBn && w.note.isNotBlank()) {
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

@Composable
fun NumberScreen() {
    val context = LocalContext.current
    val loaded by produceState<List<NumberSection>?>(null) {
        value = withContext(Dispatchers.IO) { ContentRepository.get(context).numbers() }
    }
    val tts = LocalTts.current
    val sections = loaded ?: return
    if (sections.isEmpty()) {
        EmptyState(AppIcons.Inbox, tr("সংখ্যা পাওয়া যায়নি", "Numbers not found"), tr("assets/number.json ফাইলটি দেখুন।", "Check the assets/number.json file."))
        return
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sections.forEach { sec ->
            item(key = "h_${sec.id}") { NumHeader(tr(sec.titleBn, sec.titleEn)) }
            items(sec.items, key = { "${sec.id}_${it.number}" }) { n ->
                NumRow(n) { tts.speak(n.reading.substringBefore("／")) }
            }
        }
        item {
            Text(
                tr("৪, ৭ ও ৯-এর দুটি করে পড়া আছে। সাধারণত ৪ = よん, ৭ = なな, ৯ = きゅう বেশি চলে।", "4, 7 and 9 each have two readings. 4 = よん, 7 = なな and 9 = きゅう are used more often."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun NumRow(n: NumberItem, onSpeak: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SoftCard(Modifier.fillMaxWidth(), onClick = onSpeak) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(n.number, style = MaterialTheme.typography.titleLarge, color = cs.primary)
                BigText(n.kanji, 20.sp, color = cs.onSurface, weight = FontWeight.Medium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1.4f), horizontalAlignment = Alignment.End) {
                Text(n.reading, style = MaterialTheme.typography.titleMedium, color = cs.onSurface, textAlign = TextAlign.End)
                Text(n.romaji, style = MaterialTheme.typography.labelMedium, color = cs.outline, textAlign = TextAlign.End)
                if (n.note.isNotBlank()) {
                    Text(n.note, style = MaterialTheme.typography.labelSmall, color = cs.tertiary, textAlign = TextAlign.End)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- কথোপকথন

@Composable
private fun ConvoLessonContent(level: String, no: Int) {
    val ctx = LocalContext.current
    val loaded by produceState<ConvoLesson?>(null, level, no) {
        value = withContext(Dispatchers.IO) { ContentRepository.get(ctx).convoLesson(level, no) }
    }
    val lesson = loaded
    if (lesson == null) { Loading(); return }
    Column(Modifier.fillMaxSize()) {
        LessonTitle(tr(lesson.title, lesson.titleEn))
        if (lesson.dialogues.isEmpty()) {
            EmptyLesson()
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = listPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lesson.dialogues) { d -> DialogueCard(d) }
            }
        }
    }
}

@Composable
private fun DialogueCard(d: Dialogue) {
    val cs = MaterialTheme.colorScheme
    val tts = LocalTts.current
    SoftCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    tr(d.title, d.titleEn),
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface,
                    modifier = Modifier.weight(1f)
                )
                SpeakButton(
                    onClick = { tts.speak(d.lines.joinToString("\n") { it.jp }) },
                    diameter = 40.dp
                )
            }
            d.lines.forEach { l ->
                val isA = l.speaker == "A"
                val chip = if (isA) cs.primary else cs.tertiary
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(chip.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(l.speaker, style = MaterialTheme.typography.labelLarge, color = chip)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f).clickable { tts.speak(l.jp) }) {
                        Text(l.jp, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text(l.romaji, style = MaterialTheme.typography.labelMedium, color = cs.outline)
                        Text(tr(l.bn, l.en), style = MaterialTheme.typography.bodySmall, color = cs.tertiary)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- দৈনন্দিন বাক্য

@Composable
fun SentenceScreen() {
    val context = LocalContext.current
    val loaded by produceState<List<SentenceSection>?>(null) {
        value = withContext(Dispatchers.IO) { ContentRepository.get(context).sentences() }
    }
    val tts = LocalTts.current
    val sections = loaded ?: return
    if (sections.isEmpty()) {
        EmptyState(AppIcons.Inbox, tr("বাক্য পাওয়া যায়নি", "No sentences found"), tr("assets/sentences.json ফাইলটি দেখুন।", "Check the assets/sentences.json file."))
        return
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sections.forEach { sec ->
            item(key = "h_${sec.id}") { NumHeader(tr(sec.titleBn, sec.titleEn)) }
            items(sec.items, key = { "${sec.id}_${it.jp}" }) { s ->
                SentenceRow(s) { tts.speak(s.jp) }
            }
        }
    }
}

@Composable
private fun SentenceRow(s: SentenceItem, onSpeak: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SoftCard(Modifier.fillMaxWidth(), onClick = onSpeak) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(s.jp, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Text(s.romaji, style = MaterialTheme.typography.labelMedium, color = cs.outline)
                Text(tr(s.bn, s.en), style = MaterialTheme.typography.bodyMedium, color = cs.primary)
            }
            SpeakButton(onClick = onSpeak, diameter = 40.dp)
        }
    }
}
