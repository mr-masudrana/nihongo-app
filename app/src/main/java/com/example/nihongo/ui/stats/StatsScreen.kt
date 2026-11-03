package com.example.nihongo.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.Gamification

/** কুইজ স্ক্রিনের উপরের ছোট স্ট্যাটাস বার */
@Composable
fun StatsBar(vm: StatsViewModel) {
    val s by vm.stats.collectAsState()
    val today = remember(s) { Gamification.dayOf() }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("🔥 ${Gamification.currentStreak(s, today)}", fontSize = 14.sp)
        Text("⭐ ${s.xp} XP • Lv ${Gamification.level(s.xp)}", fontSize = 14.sp)
        Text("🎯 ${Gamification.xpToday(s, today)}/${Gamification.DAILY_GOAL}", fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: StatsViewModel) {
    val s by vm.stats.collectAsState()
    val learnedKana by vm.learnedKana.collectAsState()
    val learnedVocab by vm.learnedVocab.collectAsState()
    val today = remember(s) { Gamification.dayOf() }

    val streak = Gamification.currentStreak(s, today)
    val xpToday = Gamification.xpToday(s, today)
    val goalDone = xpToday >= Gamification.DAILY_GOAL

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("প্রগ্রেস") }, windowInsets = WindowInsets(0))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // স্ট্রিক
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("🔥 $streak দিনের স্ট্রিক", fontSize = 24.sp)
                    Text("সর্বোচ্চ স্ট্রিক: ${s.bestStreak} দিন", fontSize = 14.sp)
                    if (streak == 0) {
                        Spacer(Modifier.height(4.dp))
                        Text("আজ কিছু শিখে স্ট্রিক শুরু করুন!", fontSize = 14.sp)
                    }
                }
            }

            // লেভেল
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("⭐ Level ${Gamification.level(s.xp)}", fontSize = 24.sp)
                    Text("মোট ${s.xp} XP", fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { Gamification.levelProgress(s.xp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "পরের লেভেল পর্যন্ত আরও ${Gamification.XP_PER_LEVEL - s.xp % Gamification.XP_PER_LEVEL} XP",
                        fontSize = 12.sp
                    )
                }
            }

            // আজকের লক্ষ্য
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("🎯 আজকের লক্ষ্য: $xpToday / ${Gamification.DAILY_GOAL} XP", fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (xpToday.toFloat() / Gamification.DAILY_GOAL).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (goalDone) {
                        Spacer(Modifier.height(4.dp))
                        Text("✅ আজকের লক্ষ্য পূর্ণ!", fontSize = 14.sp)
                    }
                }
            }

            // সংখ্যা
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("শেখা কানা (লেভেল ৩+): $learnedKana", fontSize = 16.sp)
                    Text("শেখা শব্দ (লেভেল ৩+): $learnedVocab", fontSize = 16.sp)
                    Text("মোট কুইজ: ${s.quizzes}", fontSize = 16.sp)
                }
            }

            Text(
                "XP নিয়ম: কুইজে সঠিক উত্তর +${Gamification.XP_CORRECT}; পারফেক্ট কুইজে +${Gamification.XP_PERFECT_BONUS} বোনাস; " +
                    "ফ্ল্যাশকার্ডে \"জানি\" +${Gamification.XP_FLASH_KNOWN}, \"জানি না\" +${Gamification.XP_FLASH_AGAIN}। " +
                    "দিনে যেকোনো একটি অ্যাক্টিভিটি করলেই স্ট্রিক বাড়ে।",
                fontSize = 12.sp
            )
        }
    }
}
