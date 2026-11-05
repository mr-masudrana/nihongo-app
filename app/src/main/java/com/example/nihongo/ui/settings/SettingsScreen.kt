package com.example.nihongo.ui.settings

import com.example.nihongo.data.AppLang
import com.example.nihongo.ui.components.tr
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
import androidx.compose.material3.Switch
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
import com.example.nihongo.data.WordField
import com.example.nihongo.ui.components.AppIcon
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ChipRow
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.SegmentedTabs
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.bounceClick
import kotlin.math.abs

private val RATES = listOf(0.7f, 1.0f, 1.3f)

/** প্রোফাইল পেজের উপরের ডানের সেটিংস আইকন থেকে খোলে; onBack চাপলে প্রোফাইলে ফেরে */
@Composable
fun SettingsScreen(vm: SettingsViewModel, onBack: () -> Unit) {
    val s by vm.settings.collectAsState()
    val tts = LocalTts.current
    val cs = MaterialTheme.colorScheme

    val rateIndex = RATES.indices.minByOrNull { abs(RATES[it] - s.ttsRate) } ?: 1
    val rateName = listOf(tr("ধীর", "Slow"), tr("স্বাভাবিক", "Normal"), tr("দ্রুত", "Fast"))[rateIndex]
    val langName = when (s.lang) {
        AppLang.SYSTEM -> tr("সিস্টেম", "System")
        AppLang.BN -> "বাংলা"
        AppLang.EN -> "English"
    }
    val themeName = listOf(tr("অটো", "Auto"), tr("লাইট", "Light"), tr("ডার্ক", "Dark"))[s.themeMode.coerceIn(0, 2)]

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
                Text(tr("সেটিংস", "Settings"), style = MaterialTheme.typography.headlineMedium, color = cs.onBackground)
                Text(tr("অ্যাপ নিজের মতো সাজান", "Customize the app"), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsGroup(
                AppIcons.Search, cs.primary, tr("শব্দ কার্ডে কী দেখাবেন", "What to show on word cards"),
                tr("${WordField.values().count { s.shows(it) }}টি অংশ দেখানো হচ্ছে", "${WordField.values().count { s.shows(it) }} parts shown")
            ) {
                WordField.values().forEach { f ->
                    val locked = s.locked(f)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tr(f.label, f.labelEn), style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                            Text(
                                if (locked && f != WordField.READING) tr("অ্যাপের ভাষা অনুযায়ী মূল অর্থ — সবসময় চালু", "Main meaning for the app language — always on")
                                else tr(f.hint, f.hintEn),
                                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = s.shows(f),
                            onCheckedChange = { vm.setWordField(f, it) },
                            enabled = !locked
                        )
                    }
                }
                Text(
                    tr("কানা পড়া ও অ্যাপের ভাষার অর্থ সবসময় থাকবে; বাকিগুলো ইচ্ছেমতো চালু/বন্ধ করুন। এটি শব্দ তালিকার কার্ডে প্রযোজ্য।",
                        "The kana reading and the meaning in your app language are always shown; toggle the rest as you like. This applies to cards in the word list."),
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.outline
                )
            }

            SettingsGroup(AppIcons.Info, cs.primary, tr("ভাষা", "Language"), tr("বর্তমান: $langName", "Current: $langName")) {
                SegmentedTabs(
                    options = listOf(tr("সিস্টেম", "System"), "বাংলা", "English"),
                    selectedIndex = s.lang.ordinal,
                    onSelect = { vm.setLang(AppLang.values()[it]) },
                    horizontalPadding = 0.dp
                )
                Text(
                    tr("সিস্টেম মানে ফোনের ভাষা অনুসরণ: ফোন বাংলা হলে বাংলা, নইলে ইংরেজি।",
                        "System follows your phone's language: Bengali if it is Bengali, otherwise English."),
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.outline
                )
            }

            SettingsGroup(AppIcons.Theme, cs.secondary, tr("থিম", "Theme"), tr("এখন: $themeName", "Now: $themeName")) {
                SegmentedTabs(
                    options = listOf(tr("অটো", "Auto"), tr("লাইট", "Light"), tr("ডার্ক", "Dark")),
                    selectedIndex = s.themeMode,
                    onSelect = { vm.setThemeMode(it) },
                    horizontalPadding = 0.dp
                )
            }

            SettingsGroup(
                AppIcons.Speaker, cs.tertiary, tr("অডিও ও উচ্চারণ", "Audio & pronunciation"),
                if (s.audioOn) tr("চালু · গতি: $rateName", "On · speed: $rateName") else tr("বন্ধ — কোনো শব্দ বাজবে না", "Off — nothing will be played")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("উচ্চারণ শোনা", "Play pronunciation"), style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text(
                            tr("বন্ধ করলে স্পিকার বাটন ও কুইজের স্বয়ংক্রিয় উচ্চারণ নীরব থাকবে।", "When off, speaker buttons and the quiz's automatic pronunciation stay silent."),
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                        )
                    }
                    Switch(checked = s.audioOn, onCheckedChange = { vm.setAudioOn(it) })
                }
                if (s.audioOn) {
                SegmentedTabs(
                    options = listOf(tr("ধীর", "Slow"), tr("স্বাভাবিক", "Normal"), tr("দ্রুত", "Fast")),
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
                        Text(tr("নমুনা শুনুন", "Hear a sample"), style = MaterialTheme.typography.labelLarge, color = cs.onPrimaryContainer)
                    }
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
                    tr("সংস্করণ $version · N5 ও N4", "Version $version · N5 & N4"),
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                )
            }
        }
    }
}
