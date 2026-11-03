package com.example.nihongo.ui.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.kana.TtsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceChips(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(options) { (key, label) ->
            FilterChip(selected = selected == key, onClick = { onSelect(key) }, label = { Text(label) })
        }
    }
}

@Composable
fun QuizPlay(ui: QuizUi, onSelect: (Int) -> Unit, onNext: () -> Unit) {
    val q = ui.current ?: return
    val context = LocalContext.current
    val tts = remember { TtsHelper(context) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    // উত্তর দেওয়ার পর সঠিক উচ্চারণ বাজাও
    LaunchedEffect(ui.index, ui.selected) {
        if (ui.selected != null) q.speak?.let { tts.speak(it) }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("প্রশ্ন ${ui.index + 1} / ${ui.questions.size}", fontSize = 14.sp)
            Text("সঠিক: ${ui.score}", fontSize = 14.sp)
        }
        LinearProgressIndicator(
            progress = { (ui.index + if (ui.answered) 1 else 0).toFloat() / ui.questions.size },
            modifier = Modifier.fillMaxWidth()
        )

        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(q.prompt, fontSize = q.promptSp.sp, textAlign = TextAlign.Center)
                if (q.hint != null) Text(q.hint, fontSize = 15.sp, textAlign = TextAlign.Center)
                if (q.speak != null) {
                    TextButton(onClick = { tts.speak(q.speak) }) { Text("🔊 শুনুন") }
                }
            }
        }

        q.options.forEachIndexed { i, text ->
            OptionCard(
                text = text,
                sp = q.optionSp,
                answered = ui.answered,
                isCorrect = i == q.correctIndex,
                isSelected = i == ui.selected,
                onClick = { onSelect(i) }
            )
        }

        if (ui.answered) {
            val correct = ui.selected == q.correctIndex
            Text(
                if (correct) "✅ সঠিক! +${Gamification.XP_CORRECT} XP"
                else "❌ ভুল। সঠিক উত্তর: ${q.options[q.correctIndex]}",
                fontSize = 16.sp
            )
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(if (ui.isLast) "ফলাফল দেখুন" else "পরবর্তী")
            }
        }
    }
}

@Composable
private fun OptionCard(
    text: String, sp: Int, answered: Boolean, isCorrect: Boolean, isSelected: Boolean, onClick: () -> Unit
) {
    val green = Color(0xFFC8E6C9)
    val red = Color(0xFFFFCDD2)
    val container = when {
        answered && isCorrect -> green
        answered && isSelected -> red
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (answered && (isCorrect || isSelected)) Color.Black
    else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        modifier = Modifier.fillMaxWidth().clickable(enabled = !answered, onClick = onClick)
    ) {
        Text(
            text,
            fontSize = sp.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
    }
}

@Composable
fun QuizResult(ui: QuizUi, onRetry: () -> Unit, onBack: () -> Unit) {
    val total = ui.questions.size
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (total == 0) {
            Text("প্রশ্ন তৈরি করা যায়নি।", fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) { Text("ফিরে যান") }
            return@Column
        }
        val pct = ui.score * 100 / total
        val emoji = when {
            pct == 100 -> "🏆"
            pct >= 70 -> "🎉"
            pct >= 40 -> "👍"
            else -> "💪"
        }
        Text(emoji, fontSize = 64.sp)
        Spacer(Modifier.height(8.dp))
        Text("স্কোর: ${ui.score} / $total", fontSize = 28.sp)
        Spacer(Modifier.height(4.dp))
        Text("এই কুইজে অর্জন: +${ui.xpEarned} XP", fontSize = 18.sp)
        if (pct == 100) Text("পারফেক্ট! বোনাস +${Gamification.XP_PERFECT_BONUS} XP যোগ হয়েছে", fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("আবার খেলুন") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("সেটিংসে ফিরুন") }
    }
}
