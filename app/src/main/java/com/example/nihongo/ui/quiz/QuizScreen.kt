package com.example.nihongo.ui.quiz

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.AppIcon
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nihongo.domain.KanaDir
import com.example.nihongo.domain.VocabDir
import com.example.nihongo.ui.components.ActionButton
import com.example.nihongo.ui.components.ChipRow
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.components.SectionLabel
import com.example.nihongo.ui.components.SegmentedTabs
import com.example.nihongo.ui.components.SoftCard

/**
 * হোমের "Test" টাইলের স্ক্রিন। kind: "kana" = কানা কুইজ; "n5"/"n4" = ওই লেভেলের লেসনের শব্দ কুইজ।
 * স্ক্রিন ছেড়ে গেলে কুইজ সেটআপে ফিরে যায়।
 */
@Composable
fun TestScreen(kind: String, kanaVm: KanaQuizViewModel, vocabVm: VocabQuizViewModel) {
    LaunchedEffect(kind) { if (kind != "kana") vocabVm.setLevel(kind) }
    DisposableEffect(kind) {
        onDispose { if (kind == "kana") kanaVm.back() else vocabVm.back() }
    }
    Column(Modifier.fillMaxSize()) {
        if (kind == "kana") KanaQuiz(kanaVm) else VocabQuiz(vocabVm)
    }
}

// ---------------- কানা কুইজ ----------------

@Composable
private fun KanaQuiz(vm: KanaQuizViewModel) {
    val ui by vm.ui.collectAsState()
    val cfg by vm.config.collectAsState()

    when (ui.phase) {
        QuizPhase.SETUP -> SetupPanel(
            startLabel = "কুইজ শুরু করুন (১০টি প্রশ্ন)",
            onStart = { vm.start() }
        ) {
            SectionLabel("কোন কানা?")
            ChipRow(
                listOf("hiragana" to "হিরাগানা", "katakana" to "কাতাকানা", "both" to "দুটোই"),
                cfg.scripts, vm::setScripts
            )
            SectionLabel("প্রশ্নের ধরন")
            ChipRow(
                listOf(
                    KanaDir.KANA_TO_ROMAJI.name to "কানা → রোমাজি",
                    KanaDir.ROMAJI_TO_KANA.name to "রোমাজি → কানা"
                ),
                cfg.dir.name, { vm.setDir(KanaDir.valueOf(it)) }
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
    val cats by vm.categories.collectAsState()

    when (ui.phase) {
        QuizPhase.SETUP -> SetupPanel(
            startLabel = "কুইজ শুরু করুন (সর্বোচ্চ ১০টি প্রশ্ন)",
            onStart = { vm.start() }
        ) {
            SectionLabel("লেসন")
            ChipRow(cats, cfg.category, vm::setCategory)
            if (cats.size <= 1) {
                Text(
                    "শব্দ কুইজের জন্য আগে লেসনের JSON-এ শব্দ যোগ করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            SectionLabel("প্রশ্নের ধরন")
            ChipRow(
                listOf(
                    VocabDir.WORD_TO_MEANING.name to "জাপানি → বাংলা",
                    VocabDir.MEANING_TO_WORD.name to "বাংলা → জাপানি"
                ),
                cfg.dir.name, { vm.setDir(VocabDir.valueOf(it)) }
            )
        }

        QuizPhase.PLAYING -> QuizPlay(ui, onSelect = vm::select, onNext = vm::next)

        QuizPhase.FINISHED -> QuizResult(ui, onRetry = vm::start, onBack = vm::back)
    }
}

/** কুইজ শুরুর আগের সেটিংস প্যানেল — দুই কুইজেই একই চেহারা */
@Composable
private fun SetupPanel(
    startLabel: String,
    onStart: () -> Unit,
    options: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 8.dp)
    ) {
        options()
        Spacer(Modifier.height(20.dp))
        SoftCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            color = cs.secondaryContainer,
            borderColor = cs.secondaryContainer
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(AppIcons.Target, size = 18.dp, tint = cs.onSecondaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text("কীভাবে XP পাবেন", style = MaterialTheme.typography.titleSmall, color = cs.onSecondaryContainer)
                }
                Text(
                    "সঠিক উত্তরে +১০ XP; সব ঠিক হলে +২০ বোনাস।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSecondaryContainer
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        ActionButton(
            text = startLabel,
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(24.dp))
    }
}
