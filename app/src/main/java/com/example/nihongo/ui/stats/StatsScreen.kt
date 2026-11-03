package com.example.nihongo.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ProgressRing
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.StatPill
import com.example.nihongo.ui.components.bounceClick
import com.example.nihongo.ui.components.entrance

/** কুইজ স্ক্রিনের উপরের ছোট স্ট্যাটাস পিল */
@Composable
fun StatsBar(vm: StatsViewModel) {
    val s by vm.stats.collectAsState()
    val goal by vm.dailyGoal.collectAsState()
    val today = remember(s) { Gamification.dayOf() }
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatPill(
            "🔥 ${Gamification.currentStreak(s, today)}",
            cs.secondaryContainer, cs.onSecondaryContainer, Modifier.weight(1f)
        )
        StatPill(
            "⭐ Lv ${Gamification.level(s.xp)} • ${s.xp}",
            cs.primaryContainer, cs.onPrimaryContainer, Modifier.weight(1.4f)
        )
        StatPill(
            "🎯 ${Gamification.xpToday(s, today)}/$goal",
            cs.tertiaryContainer, cs.onTertiaryContainer, Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsScreen(vm: StatsViewModel, onOpenSettings: () -> Unit = {}) {
    val s by vm.stats.collectAsState()
    val goal by vm.dailyGoal.collectAsState()
    val today = remember(s) { Gamification.dayOf() }
    val cs = MaterialTheme.colorScheme

    val streak = Gamification.currentStreak(s, today)
    val xpToday = Gamification.xpToday(s, today)
    val goalDone = xpToday >= goal

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth()) {
            ScreenHeader("Me", "আপনার শেখার যাত্রা")
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 20.dp, top = 18.dp)
                    .size(44.dp)
                    .bounceClick(onClick = onOpenSettings)
                    .clip(CircleShape)
                    .background(cs.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text("⚙️", fontSize = 20.sp)
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // স্ট্রিক — হিরো কার্ড
            Box(
                Modifier
                    .fillMaxWidth()
                    .entrance()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFF8A3D), Color(0xFFE5484D))))
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 56.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        BigText("$streak", 44.sp, color = Color.White, weight = FontWeight.Bold)
                        Text("দিনের স্ট্রিক", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            if (streak == 0) "আজ কিছু শিখে স্ট্রিক শুরু করুন!"
                            else "সর্বোচ্চ স্ট্রিক: ${s.bestStreak} দিন",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // লেভেল ও দৈনিক লক্ষ্য — পাশাপাশি রিং
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SoftCard(Modifier.weight(1f)) {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProgressRing(
                            progress = Gamification.levelProgress(s.xp),
                            diameter = 104.dp, strokeWidth = 11.dp,
                            color = cs.primary
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Lv", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                                BigText("${Gamification.level(s.xp)}", 30.sp, color = cs.onSurface, weight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("মোট ${s.xp} XP", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text(
                            "পরের লেভেলে আরও ${Gamification.XP_PER_LEVEL - s.xp % Gamification.XP_PER_LEVEL} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                SoftCard(Modifier.weight(1f)) {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProgressRing(
                            progress = (xpToday.toFloat() / goal).coerceIn(0f, 1f),
                            diameter = 104.dp, strokeWidth = 11.dp,
                            color = if (goalDone) cs.tertiary else cs.secondary
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BigText("$xpToday", 28.sp, color = cs.onSurface, weight = FontWeight.Bold)
                                Text(
                                    "/ $goal",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = cs.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("আজকের লক্ষ্য", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text(
                            if (goalDone) "✅ লক্ষ্য পূর্ণ!" else "XP অর্জন করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // সংখ্যা
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberTile("連", "${s.bestStreak}", "সর্বোচ্চ স্ট্রিক", Modifier.weight(1f))
                NumberTile("問", "${s.quizzes}", "মোট কুইজ", Modifier.weight(1f))
                NumberTile("経", "${s.xp}", "মোট XP", Modifier.weight(1f))
            }

            // XP নিয়ম
            SoftCard(Modifier.fillMaxWidth(), color = cs.primaryContainer, borderColor = cs.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("💡 XP নিয়ম", style = MaterialTheme.typography.titleSmall, color = cs.onPrimaryContainer)
                    Text(
                        "কুইজে সঠিক উত্তর +${Gamification.XP_CORRECT}; পারফেক্ট কুইজে +${Gamification.XP_PERFECT_BONUS} বোনাস।",
                        style = MaterialTheme.typography.bodyMedium, color = cs.onPrimaryContainer
                    )
                    Text(
                        "দিনে অন্তত একটি কুইজ দিলেই স্ট্রিক বাড়ে।",
                        style = MaterialTheme.typography.bodyMedium, color = cs.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberTile(glyph: String, value: String, label: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    SoftCard(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BigText(glyph, 20.sp, color = cs.primary, weight = FontWeight.Medium)
            BigText(value, 28.sp, color = cs.onSurface, weight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        }
    }
}
