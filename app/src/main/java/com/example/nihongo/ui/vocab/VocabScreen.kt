package com.example.nihongo.ui.vocab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.data.VocabEntity
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ChipRow
import com.example.nihongo.ui.components.EmptyState
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.components.SegmentedTabs
import com.example.nihongo.ui.components.SessionDone
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.SpeakButton
import com.example.nihongo.ui.components.StudyDeck

@Composable
fun VocabScreen(listVm: VocabListViewModel, studyVm: VocabStudyViewModel) {
    var mode by rememberSaveable { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("N5 শব্দভান্ডার", "প্রয়োজনীয় ২৪০টি শব্দ")
        SegmentedTabs(listOf("তালিকা", "প্র্যাকটিস"), mode, onSelect = { mode = it })
        Spacer(Modifier.height(12.dp))
        if (mode == 0) VocabList(listVm) else VocabStudy(studyVm)
    }
}

// ---------------- তালিকা ----------------

@Composable
fun VocabList(vm: VocabListViewModel) {
    val tts = LocalTts.current
    val words by vm.words.collectAsState()
    val category by vm.category.collectAsState()
    val query by vm.query.collectAsState()
    val learned by vm.learned.collectAsState()
    val cs = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        TextField(
            value = query,
            onValueChange = vm::setQuery,
            singleLine = true,
            placeholder = { Text("খুঁজুন — জাপানি / romaji / বাংলা / English") },
            leadingIcon = { Text("🔍") },
            trailingIcon = { if (query.isNotEmpty()) ClearButton { vm.setQuery("") } },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cs.surfaceContainerHigh,
                unfocusedContainerColor = cs.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))
        ChipRow(vocabCategories, category, vm::selectCategory)
        Text(
            "${words.size}টি শব্দ  •  শেখা হয়েছে: $learned",
            style = MaterialTheme.typography.labelMedium,
            color = cs.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )

        if (words.isEmpty()) {
            EmptyState("🔎", "কোনো শব্দ পাওয়া যায়নি", "অন্য কিছু লিখে বা অন্য বিভাগে খুঁজে দেখুন।")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(words, key = { it.id }) { v -> VocabRow(v) { tts.speak(v.reading) } }
            }
        }
    }
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    Text(
        "✕",
        modifier = Modifier.clickable(onClick = onClick).padding(12.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun VocabRow(v: VocabEntity, onSpeak: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SoftCard(Modifier.fillMaxWidth(), onClick = onSpeak) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                BigText(v.word, 26.sp, color = cs.onSurface, weight = FontWeight.Medium)
                if (v.reading != v.word) {
                    Text(v.reading, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                }
                Text(v.romaji, style = MaterialTheme.typography.labelMedium, color = cs.outline)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    v.meaningBn,
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.primary,
                    textAlign = TextAlign.End
                )
                Text(
                    v.meaningEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
            Spacer(Modifier.width(12.dp))
            SpeakButton(onClick = onSpeak, diameter = 40.dp)
        }
    }
}

// ---------------- প্র্যাকটিস (SRS ফ্ল্যাশকার্ড) ----------------

@Composable
fun VocabStudy(vm: VocabStudyViewModel) {
    val tts = LocalTts.current
    val ui by vm.ui.collectAsState()
    val cs = MaterialTheme.colorScheme
    val card = ui.current

    Column(Modifier.fillMaxSize()) {
        ChipRow(vocabCategories, ui.category, vm::setCategory)
        Spacer(Modifier.height(16.dp))

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                ui.loading -> CircularProgressIndicator()

                ui.empty -> EmptyState(
                    emoji = "🎉",
                    title = "আজকের জন্য সব শেষ!",
                    message = "এই বিভাগে কোনো কার্ড বাকি নেই। পরে আবার আসুন বা অন্য বিভাগ বেছে নিন।"
                )

                ui.finished -> SessionDone(known = ui.known, again = ui.again, onRestart = vm::restart)

                card != null -> StudyDeck(
                    index = ui.index,
                    total = ui.queue.size,
                    flipped = ui.flipped,
                    onFlip = {
                        vm.flip()
                        tts.speak(card.reading)
                    },
                    onAnswer = vm::answer,
                    front = {
                        BigText(
                            card.word, 54.sp,
                            color = cs.onPrimaryContainer, weight = FontWeight.Medium, textAlign = TextAlign.Center
                        )
                    },
                    back = {
                        BigText(card.word, 38.sp, color = cs.onSurface, weight = FontWeight.Medium, textAlign = TextAlign.Center)
                        if (card.reading != card.word) {
                            BigText(card.reading, 24.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                        Text(card.romaji, style = MaterialTheme.typography.bodyMedium, color = cs.outline)
                        Spacer(Modifier.height(14.dp))
                        BigText(
                            card.meaningBn, 28.sp,
                            color = cs.secondary, weight = FontWeight.SemiBold, textAlign = TextAlign.Center
                        )
                        Text(card.meaningEn, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                        Spacer(Modifier.height(14.dp))
                        SpeakButton(onClick = { tts.speak(card.reading) }, diameter = 52.dp)
                    }
                )
            }
        }
    }
}
