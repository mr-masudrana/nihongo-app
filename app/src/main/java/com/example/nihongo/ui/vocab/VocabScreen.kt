package com.example.nihongo.ui.vocab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.data.VocabEntity
import com.example.nihongo.ui.kana.TtsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabScreen(listVm: VocabListViewModel, studyVm: VocabStudyViewModel) {
    var mode by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("N5 শব্দভান্ডার") }, windowInsets = WindowInsets(0))
        TabRow(selectedTabIndex = mode) {
            Tab(selected = mode == 0, onClick = { mode = 0 }, text = { Text("তালিকা") })
            Tab(selected = mode == 1, onClick = { mode = 1 }, text = { Text("প্র্যাকটিস") })
        }
        if (mode == 0) VocabList(listVm) else VocabStudy(studyVm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChips(selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(vocabCategories) { (key, label) ->
            FilterChip(selected = selected == key, onClick = { onSelect(key) }, label = { Text(label) })
        }
    }
}

// ---------------- তালিকা ----------------

@Composable
private fun VocabList(vm: VocabListViewModel) {
    val context = LocalContext.current
    val tts = remember { TtsHelper(context) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val words by vm.words.collectAsState()
    val category by vm.category.collectAsState()
    val query by vm.query.collectAsState()
    val learned by vm.learned.collectAsState()

    Column {
        OutlinedTextField(
            value = query,
            onValueChange = vm::setQuery,
            singleLine = true,
            placeholder = { Text("খুঁজুন (জাপানি / romaji / বাংলা / English)") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
        )
        CategoryChips(category, vm::selectCategory)
        Text(
            "দেখানো হচ্ছে ${words.size}টি  •  শেখা শব্দ: $learned",
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        if (words.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("কোনো শব্দ পাওয়া যায়নি") }
        } else {
            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(words, key = { it.id }) { v -> VocabRow(v) { tts.speak(v.reading) } }
            }
        }
    }
}

@Composable
private fun VocabRow(v: VocabEntity, onSpeak: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onSpeak)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(v.word, fontSize = 24.sp)
                if (v.reading != v.word) Text(v.reading, fontSize = 14.sp)
                Text(v.romaji, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(4.dp))
                Text(v.meaningBn, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Text(v.meaningEn, fontSize = 12.sp)
            }
            Text("🔊", fontSize = 22.sp)
        }
    }
}

// ---------------- প্র্যাকটিস (SRS ফ্ল্যাশকার্ড) ----------------

@Composable
private fun VocabStudy(vm: VocabStudyViewModel) {
    val context = LocalContext.current
    val tts = remember { TtsHelper(context) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val ui by vm.ui.collectAsState()

    Column(Modifier.fillMaxSize()) {
        CategoryChips(ui.category, vm::setCategory)

        Box(Modifier.weight(1f).fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            when {
                ui.loading -> CircularProgressIndicator()

                ui.empty -> Text(
                    "এই বিভাগে আজকের জন্য কোনো কার্ড বাকি নেই 🎉\nপরে আবার আসুন বা অন্য বিভাগ বেছে নিন।",
                    fontSize = 16.sp, textAlign = TextAlign.Center
                )

                ui.finished -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("সেশন শেষ! 🎉", fontSize = 24.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("জানি: ${ui.known}   |   জানি না: ${ui.again}", fontSize = 16.sp)
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { vm.restart() }) { Text("আবার শুরু করুন") }
                }

                else -> {
                    val card = ui.current!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${ui.index + 1} / ${ui.queue.size}", fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { ui.index.toFloat() / ui.queue.size },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(20.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clickable(enabled = !ui.flipped) {
                                    vm.flip()
                                    tts.speak(card.reading)
                                }
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(card.word, fontSize = 48.sp, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(12.dp))
                                if (ui.flipped) {
                                    if (card.reading != card.word) Text(card.reading, fontSize = 22.sp)
                                    Text(card.romaji, fontSize = 16.sp, color = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        card.meaningBn, fontSize = 22.sp, textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(card.meaningEn, fontSize = 14.sp)
                                    TextButton(onClick = { tts.speak(card.reading) }) { Text("🔊 আবার শুনুন") }
                                } else {
                                    Text("ট্যাপ করে অর্থ দেখুন", fontSize = 14.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        if (ui.flipped) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { vm.answer(false) }, modifier = Modifier.weight(1f)) {
                                    Text("জানি না")
                                }
                                Button(onClick = { vm.answer(true) }, modifier = Modifier.weight(1f)) {
                                    Text("জানি")
                                }
                            }
                        } else {
                            Spacer(Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }
}
