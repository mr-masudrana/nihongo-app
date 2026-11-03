package com.example.nihongo.ui.flashcard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.EmptyState
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.components.SegmentedTabs
import com.example.nihongo.ui.components.SessionDone
import com.example.nihongo.ui.components.SpeakButton
import com.example.nihongo.ui.components.StudyDeck

@Composable
fun FlashcardScreen(vm: FlashcardViewModel) {
    val tts = LocalTts.current
    val ui by vm.ui.collectAsState()
    val cs = MaterialTheme.colorScheme
    val card = ui.current

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("ফ্ল্যাশকার্ড", "কার্ড উল্টে মনে করার চেষ্টা করুন")
        SegmentedTabs(
            options = listOf("হিরাগানা", "কাতাকানা"),
            selectedIndex = if (ui.type == "katakana") 1 else 0,
            onSelect = { vm.setType(if (it == 1) "katakana" else "hiragana") }
        )
        Spacer(Modifier.height(16.dp))

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                ui.loading -> CircularProgressIndicator()

                ui.empty -> EmptyState(
                    emoji = "🎉",
                    title = "আজকের জন্য সব শেষ!",
                    message = "কোনো কার্ড বাকি নেই। পরে আবার আসুন।"
                )

                ui.finished -> SessionDone(known = ui.known, again = ui.again, onRestart = vm::restart)

                card != null -> StudyDeck(
                    index = ui.index,
                    total = ui.queue.size,
                    flipped = ui.flipped,
                    onFlip = {
                        vm.flip()
                        tts.speak(card.symbol)
                    },
                    onAnswer = vm::answer,
                    front = {
                        BigText(card.symbol, 112.sp, color = cs.onPrimaryContainer, weight = FontWeight.Medium)
                    },
                    back = {
                        BigText(card.symbol, 72.sp, color = cs.onSurface, weight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        BigText(card.romaji, 36.sp, color = cs.onSurface, weight = FontWeight.Bold)
                        BigText(card.bn, 26.sp, color = cs.secondary, weight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        SpeakButton(onClick = { tts.speak(card.symbol) }, diameter = 52.dp)
                    }
                )
            }
        }
    }
}
