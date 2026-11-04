package com.example.nihongo.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.ui.components.AppIcon
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ChipRow
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.SegmentedTabs
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.bounceClick
import kotlin.math.abs

private val GOALS = listOf(20, 30, 50, 100, 150)
private val RATES = listOf(0.7f, 1.0f, 1.3f)

/** প্রোফাইল পেজের উপরের ডানের সেটিংস আইকন থেকে খোলে; onBack চাপলে প্রোফাইলে ফেরে */
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val s by vm.settings.collectAsState()
    val tts = LocalTts.current
    val cs = MaterialTheme.colorScheme

    val rateIndex = RATES.indices.minByOrNull { abs(RATES[it] - s.ttsRate) } ?: 1
    val rateName = listOf("ধীর", "স্বাভাবিক", "দ্রুত")[rateIndex]
    val themeName = listOf("অটো", "লাইট", "ডার্ক")[s.themeMode.coerceIn(0, 2)]

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .bounceClick(onClick = onBack)
                    .clip(CircleShape)
                    .background(cs.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) { AppIcon(AppIcons.Back, size = 22.dp, tint = cs.onSurface) }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("সেটিংস", style = MaterialTheme.typography.headlineMedium, color = cs.onBackground)
                Text("অ্যাপ নিজের মতো সাজান", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsGroup(AppIcons.Target, cs.primary, "দৈনিক XP লক্ষ্য", "প্রতিদিন ${s.dailyGoal} XP") {
                ChipRow(
                    options = GOALS.map { it.toString() to "$it XP" },
                    selected = s.dailyGoal.toString(),
                    onSelect = { vm.setDailyGoal(it.toInt()) },
                    contentPadding = PaddingValues(0.dp)
                )
            }

            SettingsGroup(AppIcons.Theme, cs.secondary, "থিম", "এখন: $themeName") {
                SegmentedTabs(
                    options = listOf("অটো", "লাইট", "ডার্ক"),
                    selectedIndex = s.themeMode,
                    onSelect = { vm.setThemeMode(it) },
                    horizontalPadding = 0.dp
                )
            }

            SettingsGroup(AppIcons.Speaker, cs.tertiary, "উচ্চারণের গতি", "এখন: $rateName") {
                SegmentedTabs(
                    options = listOf("ধীর", "স্বাভাবিক", "দ্রুত"),
                    selectedIndex = rateIndex,
                    onSelect = {
                        vm.setTtsRate(RATES[it])
                        tts.setRate(RATES[it])
                        tts.speak("こんにちは")
                    },
                    horizontalPadding = 0.dp
                )
                SoftCard(
                    Modifier.fillMaxWidth(),
                    color = cs.primaryContainer,
                    borderColor = cs.primaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    onClick = { tts.speak("こんにちは") }
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(AppIcons.Speaker, size = 20.dp, tint = cs.onPrimaryContainer)
                        Spacer(Modifier.width(10.dp))
                        BigText("こんにちは", 18.sp, color = cs.onPrimaryContainer, weight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        Text("নমুনা শুনুন", style = MaterialTheme.typography.labelLarge, color = cs.onPrimaryContainer)
                    }
                }
            }

            AboutCard()
        }
    }
}

/** আইকন + শিরোনাম + বর্ণনা সহ একটি সেটিংস গ্রুপ কার্ড */
@Composable
private fun SettingsGroup(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    SoftCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) { AppIcon(icon, size = 22.dp, tint = tint) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
            content()
        }
    }
}

@Composable
private fun AboutCard() {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val version = remember(ctx) {
        runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: "1.0"
    }
    SoftCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF3F51B5), Color(0xFFD86C9A)))),
                contentAlignment = Alignment.Center
            ) { BigText("日", 24.sp, color = Color.White, weight = FontWeight.Bold) }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Nihongo", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Text(
                    "সংস্করণ $version · N5 ও N4",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                )
            }
        }
    }
}
