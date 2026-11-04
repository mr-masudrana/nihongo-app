package com.example.nihongo.ui.learn

import androidx.compose.foundation.background
import com.example.nihongo.ui.components.AppIcons
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.data.ContentRepository
import com.example.nihongo.data.KanjiItem
import com.example.nihongo.ui.components.ActionButton
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.EmptyState
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.SpeakButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
private fun rememberKanji(level: String): List<KanjiItem>? {
    val ctx = LocalContext.current
    val loaded by produceState<List<KanjiItem>?>(null, level) {
        value = withContext(Dispatchers.IO) { ContentRepository.get(ctx).kanji(level) }
    }
    return loaded
}

// ---------------------------------------------------------------- গ্রিড

/** কাঞ্জির তালিকা — লেসন ছাড়া, ৩ কলামের টাইল; ট্যাপ করলে বিস্তারিত পাতা */
@Composable
fun KanjiGridScreen(level: String, onOpen: (Int) -> Unit) {
    val list = rememberKanji(level)
    if (list == null) {
        Text(
            "লোড হচ্ছে…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(20.dp)
        )
        return
    }
    if (list.isEmpty()) {
        EmptyState(AppIcons.Inbox, "এখনো কাঞ্জি নেই", "assets/${level}/kanji_${level}.json ফাইলে কাঞ্জি যোগ করুন।")
        return
    }
    val cs = MaterialTheme.colorScheme
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(cs.primaryContainer)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${list.size} টি কাঞ্জি",
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onPrimaryContainer
                    )
                }
            }
        }
        itemsIndexed(list) { i, k -> KanjiTile(k) { onOpen(i) } }
    }
}

@Composable
private fun KanjiTile(k: KanjiItem, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SoftCard(
        Modifier.fillMaxWidth().aspectRatio(0.95f),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        Column(
            Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BigText(k.kanji, 42.sp, color = cs.onSurface, weight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                k.bn,
                style = MaterialTheme.typography.labelMedium,
                color = cs.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------- বিস্তারিত

@Composable
fun KanjiDetailScreen(level: String, index: Int, onNavigate: (Int) -> Unit) {
    val list = rememberKanji(level)
    val k = list?.getOrNull(index)
    if (list == null || k == null) {
        EmptyState(AppIcons.Inbox, "কাঞ্জি পাওয়া যায়নি", "তালিকায় ফিরে গিয়ে আবার চেষ্টা করুন।")
        return
    }
    val cs = MaterialTheme.colorScheme
    val tts = LocalTts.current

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // কাঞ্জি ও অর্থ
            SoftCard(
                Modifier.fillMaxWidth(),
                onClick = { k.words.firstOrNull()?.let { tts.speak(it.reading) } }
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(116.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(cs.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        BigText(k.kanji, 76.sp, color = cs.onPrimaryContainer, weight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(k.bn, style = MaterialTheme.typography.titleLarge, color = cs.onSurface)
                        Text(k.en, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                        if (k.strokes > 0) {
                            Text(
                                "${k.strokes}টি স্ট্রোক",
                                style = MaterialTheme.typography.labelLarge,
                                color = cs.tertiary
                            )
                        }
                    }
                }
            }

            // পড়া
            SoftCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReadingRow("訓 কুন", k.kunyomi)
                    ReadingRow("音 অন", k.onyomi)
                }
            }

            // শব্দের উদাহরণ
            if (k.words.isNotEmpty()) {
                Text("শব্দের উদাহরণ", style = MaterialTheme.typography.titleSmall, color = cs.onSurfaceVariant)
                k.words.forEach { w ->
                    SoftCard(Modifier.fillMaxWidth(), onClick = { tts.speak(w.reading) }) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                BigText(w.word, 24.sp, color = cs.onSurface, weight = FontWeight.Medium)
                                Text(w.reading, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                            }
                            Text(
                                w.bn,
                                style = MaterialTheme.typography.titleSmall,
                                color = cs.primary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            SpeakButton(onClick = { tts.speak(w.reading) }, diameter = 40.dp)
                        }
                    }
                }
            }

            // উদাহরণ বাক্য
            if (k.sentenceJp.isNotBlank()) {
                SoftCard(
                    Modifier.fillMaxWidth(),
                    color = cs.secondaryContainer,
                    borderColor = cs.secondaryContainer,
                    onClick = { tts.speak(k.sentenceJp) }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("উদাহরণ বাক্য", style = MaterialTheme.typography.labelMedium, color = cs.onSecondaryContainer)
                        Text(k.sentenceJp, style = MaterialTheme.typography.titleMedium, color = cs.onSecondaryContainer)
                        Text(k.sentenceBn, style = MaterialTheme.typography.bodyMedium, color = cs.onSecondaryContainer)
                    }
                }
            }
        }

        // আগের / পরের কাঞ্জি
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "‹ আগের",
                onClick = { onNavigate(index - 1) },
                modifier = Modifier.weight(1f),
                container = cs.surfaceContainerHigh,
                content = cs.onSurface,
                enabled = index > 0
            )
            ActionButton(
                text = "পরের ›",
                onClick = { onNavigate(index + 1) },
                modifier = Modifier.weight(1f),
                enabled = index < list.lastIndex
            )
        }
    }
}

@Composable
private fun ReadingRow(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = cs.onSurfaceVariant,
            modifier = Modifier.width(72.dp)
        )
        Text(
            if (value.isBlank()) "—" else value,
            style = MaterialTheme.typography.titleMedium,
            color = cs.onSurface
        )
    }
}
