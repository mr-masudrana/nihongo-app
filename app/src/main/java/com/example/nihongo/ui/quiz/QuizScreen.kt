package com.example.nihongo.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.KanaDir
import com.example.nihongo.domain.VocabDir
import com.example.nihongo.ui.stats.StatsBar
import com.example.nihongo.ui.stats.StatsViewModel
import com.example.nihongo.ui.vocab.vocabCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(kanaVm: KanaQuizViewModel, vocabVm: VocabQuizViewModel, statsVm: StatsViewModel) {
    var mode by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("কুইজ") }, windowInsets = WindowInsets(0))
        StatsBar(statsVm)
        TabRow(selectedTabIndex = mode) {
            Tab(selected = mode == 0, onClick = { mode = 0 }, text = { Text("কানা কুইজ") })
            Tab(selected = mode == 1, onClick = { mode = 1 }, text = { Text("শব্দ কুইজ") })
        }
        if (mode == 0) KanaQuiz(kanaVm) else VocabQuiz(vocabVm)
    }
}

// ---------------- কানা কুইজ ----------------

@Composable
private fun KanaQuiz(vm: KanaQuizViewModel) {
    val ui by vm.ui.collectAsState()
    val cfg by vm.config.collectAsState()

    when (ui.phase) {
        QuizPhase.SETUP -> Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text("কোন কানা?", fontSize = 16.sp)
            ChoiceChips(
                listOf("hiragana" to "হিরাগানা", "katakana" to "কাতাকানা", "both" to "দুটোই"),
                cfg.scripts, vm::setScripts
            )
            Spacer(Modifier.height(12.dp))
            Text("প্রশ্নের ধরন", fontSize = 16.sp)
            ChoiceChips(
                listOf(
                    KanaDir.KANA_TO_ROMAJI.name to "কানা → রোমাজি",
                    KanaDir.ROMAJI_TO_KANA.name to "রোমাজি → কানা"
                ),
                cfg.dir.name, { vm.setDir(KanaDir.valueOf(it)) }
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = { vm.start() }, modifier = Modifier.fillMaxWidth()) {
                Text("কুইজ শুরু করুন (১০টি প্রশ্ন)")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "সঠিক উত্তরে +১০ XP; সব ঠিক হলে +২০ বোনাস।",
                fontSize = 12.sp
            )
        }

        QuizPhase.PLAYING -> QuizPlay(ui, onSelect = vm::select, onNext = vm::next)

        QuizPhase.FINISHED -> QuizResult(ui, onRetry = vm::start, onBack = vm::back)
    }
}

// ---------------- শব্দ কুইজ ----------------

@Composable
private fun VocabQuiz(vm: VocabQuizViewModel) {
    val ui by vm.ui.collectAsState()
    val cfg by vm.config.collectAsState()

    when (ui.phase) {
        QuizPhase.SETUP -> Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text("বিভাগ", fontSize = 16.sp)
            ChoiceChips(vocabCategories, cfg.category, vm::setCategory)
            Spacer(Modifier.height(12.dp))
            Text("প্রশ্নের ধরন", fontSize = 16.sp)
            ChoiceChips(
                listOf(
                    VocabDir.WORD_TO_MEANING.name to "জাপানি → বাংলা",
                    VocabDir.MEANING_TO_WORD.name to "বাংলা → জাপানি"
                ),
                cfg.dir.name, { vm.setDir(VocabDir.valueOf(it)) }
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = { vm.start() }, modifier = Modifier.fillMaxWidth()) {
                Text("কুইজ শুরু করুন (সর্বোচ্চ ১০টি প্রশ্ন)")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "সঠিক উত্তরে +১০ XP; সব ঠিক হলে +২০ বোনাস।",
                fontSize = 12.sp
            )
        }

        QuizPhase.PLAYING -> QuizPlay(ui, onSelect = vm::select, onNext = vm::next)

        QuizPhase.FINISHED -> QuizResult(ui, onRetry = vm::start, onBack = vm::back)
    }
}
