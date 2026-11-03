package com.example.nihongo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.components.AppProgressBar
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.entrance
import com.example.nihongo.ui.learn.ROUTE_KANA
import com.example.nihongo.ui.learn.ROUTE_N5_VOCAB_ALL
import com.example.nihongo.ui.stats.StatsBar
import com.example.nihongo.ui.stats.StatsViewModel

/** Home: স্ট্রিক, আজকের লক্ষ্য ও দ্রুত শুরুর কার্ড। onLearn(0 = কানা, 1 = ফ্ল্যাশকার্ড, 2 = শব্দ প্র্যাকটিস) */
@Composable
fun HomeScreen(
    statsVm: StatsViewModel,
    onLearn: (route: String, kanaSub: Int) -> Unit,
    onQuiz: () -> Unit,
    onSearch: () -> Unit
) {
    val s by statsVm.stats.collectAsState()
    val goal by statsVm.dailyGoal.collectAsState()
    val today = remember(s) { Gamification.dayOf() }
    val xpToday = Gamification.xpToday(s, today)
    val cs = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Home", "আজ কী শিখবেন?")
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsBar(statsVm)

            SoftCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp).entrance()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🎯 আজকের লক্ষ্য", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    AppProgressBar(
                        progress = xpToday.toFloat() / goal,
                        color = if (xpToday >= goal) cs.tertiary else cs.secondary
                    )
                    Text(
                        if (xpToday >= goal) "✅ আজকের লক্ষ্য পূর্ণ! চাইলে আরও শিখুন।"
                        else "$xpToday / $goal XP — আর ${goal - xpToday} XP বাকি",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            HomeAction("札", "ফ্ল্যাশকার্ড রিভিউ", "আজকের SRS কার্ডগুলো দেখুন") { onLearn(ROUTE_KANA, 1) }
            HomeAction("あ", "কানা চার্ট", "হিরাগানা ও কাতাকানা") { onLearn(ROUTE_KANA, 0) }
            HomeAction("語", "শব্দ প্র্যাকটিস", "N5 শব্দ কার্ডে শিখুন") { onLearn(ROUTE_N5_VOCAB_ALL, 0) }
            HomeAction("問", "কুইজ", "১০টি প্রশ্নে নিজেকে যাচাই করুন", onQuiz)
            HomeAction("探", "শব্দ খুঁজুন", "জাপানি / romaji / বাংলা / English", onSearch)
        }
    }
}

@Composable
private fun HomeAction(glyph: String, title: String, sub: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SoftCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), onClick = onClick) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            BigText(glyph, 30.sp, color = cs.primary, weight = FontWeight.Bold)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = cs.outline)
        }
    }
}
