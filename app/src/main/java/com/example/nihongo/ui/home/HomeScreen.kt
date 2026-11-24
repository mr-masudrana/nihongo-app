package com.example.nihongo.ui.home

import com.example.nihongo.ui.components.tr
import androidx.compose.foundation.background
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.AppIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.entrance
import com.example.nihongo.ui.kana.KanaViewModel
import com.example.nihongo.ui.learn.*
import com.example.nihongo.ui.quiz.KanaQuizViewModel
import com.example.nihongo.ui.quiz.VocabQuizViewModel
import com.example.nihongo.ui.stats.StatsViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    route: String,
    onRoute: (String) -> Unit,
    statsVm: StatsViewModel,
    kanaVm: KanaViewModel,
    kanaQuizVm: KanaQuizViewModel,
    vocabQuizVm: VocabQuizViewModel
) {
    if (route == ROUTE_HUB) {
        HomeHub(statsVm, onRoute)
    } else {
        LearnScreen(route, onRoute, kanaVm, kanaQuizVm, vocabQuizVm)
    }
}

private data class BookItem(
    val badge: String,
    val jpTitle: String,
    val title: String,
    val subtitleBn: String,
    val subtitleEn: String,
    val lessonsCount: String,
    val route: String,
    val accentIndex: Int
)

private val irodoriBooks = listOf(
    BookItem(
        badge = "A1",
        jpTitle = "いろどり 入門",
        title = "Irodori Starter (A1)",
        subtitleBn = "প্রাথমিক কথোপকথন, দৈনন্দিন শব্দ ও জাপানি জীবনযাত্রা",
        subtitleEn = "Basic daily conversations, vocabulary & life in Japan",
        lessonsCount = "18 Lessons",
        route = ROUTE_BOOK_A1,
        accentIndex = 0
    ),
    BookItem(
        badge = "A2 · 1",
        jpTitle = "いろどり 初級 1",
        title = "Irodori Elementary 1 (A2)",
        subtitleBn = "কর্মক্ষেত্র, ভ্রমণ, হাসপাতাল ও সামাজিক যোগাযোগ",
        subtitleEn = "Workplace, travel, hospital & social communication",
        lessonsCount = "18 Lessons",
        route = ROUTE_BOOK_A2_1,
        accentIndex = 1
    ),
    BookItem(
        badge = "A2 · 2",
        jpTitle = "いろどり 初級 2",
        title = "Irodori Elementary 2 (A2)",
        subtitleBn = "স্থানীয় উৎসব, কেনাকাটা, দুর্যোগ প্রস্তুতি ও ভবিষ্যৎ পরিকল্পনা",
        subtitleEn = "Local events, shopping, disaster prep & future goals",
        lessonsCount = "18 Lessons",
        route = ROUTE_BOOK_A2_2,
        accentIndex = 2
    )
)

private data class BasicTile(val glyph: String, val title: String, val route: String)

private val basicTiles = listOf(
    BasicTile("あ", "Hiragana", ROUTE_HIRAGANA),
    BasicTile("ア", "Katakana", ROUTE_KATAKANA),
    BasicTile("数", "Number", ROUTE_NUMBER)
)

@Composable
private fun greeting(): Triple<String, String, String> {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 11 -> Triple("おはよう", "Ohayou", tr("সুপ্রভাত", "Good morning"))
        h < 18 -> Triple("こんにちは", "Konnichiwa", tr("শুভ দুপুর", "Good afternoon"))
        else -> Triple("こんばんは", "Konbanwa", tr("শুভ সন্ধ্যা", "Good evening"))
    }
}

@Composable
private fun HomeHub(statsVm: StatsViewModel, onRoute: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val s by statsVm.stats.collectAsState()
    val today = remember(s) { Gamification.dayOf() }
    val streak = Gamification.currentStreak(s, today)
    val greet = greeting()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        HomeHeader(greet, streak)

        // Irodori Books Shelf
        Column(
            Modifier.padding(horizontal = 20.dp).entrance(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(width = 4.dp, height = 22.dp)
                        .clip(CircleShape)
                        .background(cs.primary)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    tr("IRODORI পাঠ্যবইসমূহ", "IRODORI TEXTBOOKS"),
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface
                )
            }

            irodoriBooks.forEach { book ->
                val accent = when (book.accentIndex) {
                    0 -> cs.secondary
                    1 -> cs.tertiary
                    else -> cs.primary
                }
                BookCard(book = book, accent = accent, onClick = { onRoute(book.route) })
            }
        }

        // Basic Letters & Numbers
        Column(
            Modifier.padding(horizontal = 20.dp).entrance(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(width = 4.dp, height = 22.dp)
                        .clip(CircleShape)
                        .background(cs.outline)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    tr("বর্ণমালা ও সংখ্যা (BASIC)", "ALPHABET & NUMBERS (BASIC)"),
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                basicTiles.forEach { t ->
                    SoftCard(
                        Modifier.weight(1f).height(84.dp),
                        color = cs.surfaceContainerLow,
                        borderColor = cs.outlineVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onRoute(t.route) }
                    ) {
                        Column(
                            Modifier.fillMaxSize().padding(10.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BigText(t.glyph, 22.sp, color = cs.primary, weight = FontWeight.Bold)
                            Text(
                                t.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = cs.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCard(book: BookItem, accent: Color, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SoftCard(
        Modifier.fillMaxWidth(),
        color = cs.surfaceContainerLow,
        borderColor = accent.copy(alpha = 0.35f),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover spine / badge
            Box(
                Modifier
                    .size(width = 62.dp, height = 78.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "彩",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            book.badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = cs.onPrimary
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    book.jpTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    book.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    tr(book.subtitleBn, book.subtitleEn),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    book.lessonsCount,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(8.dp))
            AppIcon(AppIcons.ChevronRight, size = 24.dp, tint = accent)
        }
    }
}

@Composable
private fun HomeHeader(greet: Triple<String, String, String>, streak: Int) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 20.dp, top = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "${greet.second} · ${greet.third}",
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurfaceVariant
            )
            BigText(greet.first, 32.sp, color = cs.onBackground, weight = FontWeight.Bold)
            Text(
                tr("JFT-Basic · একটি বই নির্বাচন করুন", "JFT-Basic · Select a textbook"),
                style = MaterialTheme.typography.labelMedium,
                color = cs.primary
            )
        }
        Row(
            Modifier
                .clip(CircleShape)
                .background(cs.surfaceContainerHigh)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(AppIcons.Fire, size = 18.dp, tint = Color(0xFFFF7043))
            Spacer(Modifier.width(6.dp))
            Text("$streak", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
        }
    }
}
