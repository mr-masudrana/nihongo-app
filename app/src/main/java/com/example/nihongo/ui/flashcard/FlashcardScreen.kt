package com.example.nihongo.ui.flashcard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.ui.kana.TtsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(vm: FlashcardViewModel) {
    val context = LocalContext.current
    val tts = remember { TtsHelper(context) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val ui by vm.ui.collectAsState()
    val types = listOf("hiragana" to "হিরাগানা", "katakana" to "কাতাকানা")

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("ফ্ল্যাশকার্ড") }, windowInsets = WindowInsets(0))

        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            types.forEach { (key, label) ->
                FilterChip(selected = ui.type == key, onClick = { vm.setType(key) }, label = { Text(label) })
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            when {
                ui.loading -> CircularProgressIndicator()

                ui.empty -> Message("আজকের জন্য কোনো কার্ড বাকি নেই 🎉\nপরে আবার আসুন।")

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
                                    tts.speak(card.symbol)
                                }
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(card.symbol, fontSize = 96.sp)
                                Spacer(Modifier.height(12.dp))
                                if (ui.flipped) {
                                    Text(card.romaji, fontSize = 28.sp)
                                    Text(card.bn, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(onClick = { tts.speak(card.symbol) }) { Text("🔊 আবার শুনুন") }
                                } else {
                                    Text("ট্যাপ করে উত্তর দেখুন", fontSize = 14.sp)
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

@Composable
private fun Message(text: String) {
    Text(text, fontSize = 18.sp, textAlign = TextAlign.Center)
}
