package com.example.nihongo.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ProgressRing
import com.example.nihongo.ui.components.ChipRow
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.components.SectionLabel
import com.example.nihongo.ui.components.SegmentedTabs
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.stats.StatsViewModel
import kotlin.math.abs

private val GOALS = listOf(20, 30, 50, 100, 150)
private val RATES = listOf(0.7f, 1.0f, 1.3f)

@Composable
fun SettingsScreen(vm: SettingsViewModel, statsVm: StatsViewModel) {
    val s by vm.settings.collectAsState()
    val st by statsVm.stats.collectAsState()
    val tts = LocalTts.current
    val cs = MaterialTheme.colorScheme

    val rateIndex = RATES.indices.minByOrNull { abs(RATES[it] - s.ttsRate) } ?: 1

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("সেটিংস", "অ্যাপ নিজের মতো সাজান")
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SoftCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = Gamification.levelProgress(st.xp),
                        diameter = 84.dp, strokeWidth = 9.dp, color = cs.primary
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lv", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                            BigText("${Gamification.level(st.xp)}", 26.sp, color = cs.onSurface, weight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("মোট ${st.xp} XP", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                        Text("সর্বোচ্চ স্ট্রিক ${st.bestStreak} দিন", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                        Text("${st.quizzes}টি কুইজ সম্পন্ন", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            SectionLabel("🎯 দৈনিক XP লক্ষ্য")
            ChipRow(
                options = GOALS.map { it.toString() to "$it XP" },
                selected = s.dailyGoal.toString(),
                onSelect = { vm.setDailyGoal(it.toInt()) }
            )
            Spacer(Modifier.height(12.dp))

            SectionLabel("🌗 থিম")
            SegmentedTabs(
                options = listOf("অটো", "লাইট", "ডার্ক"),
                selectedIndex = s.themeMode,
                onSelect = { vm.setThemeMode(it) }
            )
            Spacer(Modifier.height(12.dp))

            SectionLabel("🔊 উচ্চারণের গতি")
            SegmentedTabs(
                options = listOf("ধীর", "স্বাভাবিক", "দ্রুত"),
                selectedIndex = rateIndex,
                onSelect = {
                    vm.setTtsRate(RATES[it])
                    tts.setRate(RATES[it])
                    tts.speak("こんにちは")
                }
            )
            Text(
                "ট্যাপ করলেই “こんにちは” বেজে নমুনা শোনাবে।",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}
