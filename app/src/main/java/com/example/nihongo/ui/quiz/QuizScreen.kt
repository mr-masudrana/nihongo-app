package com.example.nihongo.ui.quiz

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import com.example.nihongo.data.StatsRepository
import androidx.compose.ui.unit.dp
import com.example.nihongo.domain.Gamification
import com.example.nihongo.domain.KanaDir
import com.example.nihongo.domain.VocabDir
import com.example.nihongo.ui.components.ActionButton
import com.example.nihongo.ui.components.AppProgressBar
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
    val ctx = LocalContext.current
    val stats by StatsRepository.get(ctx).stats.collectAsState()
    val high = stats.highScore(kind)
    val xp = stats.xp
    LaunchedEffect(kind) { if (kind != "kana") vocabVm.setLevel(kind) }
    DisposableEffect(kind) {
        onDispose { if (kind == "kana") kanaVm.back() else vocabVm.back() }
    }
    Column(Modifier.fillMaxSize()) {
        if (kind == "kana") KanaQuiz(kanaVm, high, xp) else VocabQuiz(vocabVm, high, xp)
    }
}

// ---------------- কানা কুইজ ----------------

@Composable
private fun KanaQuiz(vm: KanaQuizViewModel, high: Int, xp: Int) {
    val ui by vm.ui.collectAsState()
    val cfg by vm.config.collectAsState()

    when (ui.phase) {
        QuizPhase.SETUP -> SetupPanel(
            mode = cfg.mode, onMode = vm::setMode, highScore = high, xp = xp,
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

        QuizPhase.PLAYING -> QuizPlay(ui, onSelect = vm::select, onTimeout = vm::timeout, onNext = vm::next)

        QuizPhase.FINISHED -> QuizResult(ui, onRetry = vm::start, onBack = vm::back)
    }
}

// ---------------- শব্দ কুইজ ----------------

@Composable
private fun VocabQuiz(vm: VocabQuizViewModel, high: Int, xp: Int) {
    val ui by vm.ui.collectAsState()
    val cfg by vm.config.collectAsState()
    val cats by vm.categories.collectAsState()

    when (ui.phase) {
        QuizPhase.SETUP -> SetupPanel(
            mode = cfg.mode, onMode = vm::setMode, highScore = high, xp = xp,
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

        QuizPhase.PLAYING -> QuizPlay(ui, onSelect = vm::select, onTimeout = vm::timeout, onNext = vm::next)

        QuizPhase.FINISHED -> QuizResult(ui, onRetry = vm::start, onBack = vm::back)
    }
}

/** কুইজ শুরুর আগের সেটিংস প্যানেল — দুই কুইজেই একই চেহারা */
@Composable
private fun SetupPanel(
    mode: QuizMode,
    onMode: (QuizMode) -> Unit,
    highScore: Int,
    xp: Int,
    onStart: () -> Unit,
    options: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 8.dp)
    ) {
        SegmentedTabs(
            options = listOf("আনলিমিটেড", "চ্যালেঞ্জ"),
            selectedIndex = mode.ordinal,
            onSelect = { onMode(QuizMode.values()[it]) }
        )
        Spacer(Modifier.height(12.dp))
        if (mode == QuizMode.UNLIMITED) {
            SoftCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                color = cs.tertiaryContainer,
                borderColor = cs.tertiaryContainer
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(AppIcons.Trophy, size = 28.dp, tint = cs.onTertiaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("হাই স্কোর", style = MaterialTheme.typography.labelLarge, color = cs.onTertiaryContainer)
                            Text("$highScore", style = MaterialTheme.typography.headlineSmall, color = cs.onTertiaryContainer)
                        }
                    }
                    Text(
                        "প্রশ্ন অসীম। একটি ভুল হলে বা মাঝপথে বের হয়ে গেলে রান শেষ — আবার নতুন করে শুরু। টানা সঠিক উত্তরই আপনার স্কোর। এই মোডে XP নেই, শুধু হাই স্কোর।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onTertiaryContainer
                    )
                }
            }
        } else {
            SoftCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                color = cs.secondaryContainer,
                borderColor = cs.secondaryContainer
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(AppIcons.Target, size = 18.dp, tint = cs.onSecondaryContainer)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "লেভেল ${Gamification.level(xp)} · $xp XP",
                            style = MaterialTheme.typography.titleSmall,
                            color = cs.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${xp % Gamification.XP_PER_LEVEL} / ${Gamification.XP_PER_LEVEL}",
                            style = MaterialTheme.typography.labelMedium,
                            color = cs.onSecondaryContainer
                        )
                    }
                    AppProgressBar(progress = Gamification.levelProgress(xp), height = 6.dp)
                    Text(
                        "${Gamification.CHALLENGE_QUESTIONS}টি প্রশ্ন, প্রতিটিতে ${Gamification.CHALLENGE_SECONDS} সেকেন্ড। সঠিক উত্তরে +${Gamification.XP_CORRECT} XP; প্রথম ৫ সেকেন্ডে দিলে +${Gamification.XP_SPEED_BONUS} বোনাস; সব ঠিক হলে +${Gamification.XP_PERFECT_BONUS}। সময় শেষ হলে উত্তর ভুল ধরা হয়, তবে কুইজ চলতে থাকে।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSecondaryContainer
                    )
                }
            }
        }
        options()
        Spacer(Modifier.height(20.dp))
        ActionButton(
            text = if (mode == QuizMode.UNLIMITED) "আনলিমিটেড শুরু করুন" else "চ্যালেঞ্জ শুরু করুন",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(24.dp))
    }
}
