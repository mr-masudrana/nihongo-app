package com.example.nihongo.ui.search

import androidx.compose.foundation.clickable
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.AppIcon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.data.LessonWord
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.EmptyState
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.components.SoftCard

@Composable
fun SearchScreen(vm: SearchViewModel) {
    val tts = LocalTts.current
    val results by vm.results.collectAsState()
    val query by vm.query.collectAsState()
    val total by vm.total.collectAsState()
    val loaded by vm.loaded.collectAsState()
    val cs = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Search", "লেসনের শব্দে খুঁজুন ও উচ্চারণ শুনুন")
        TextField(
            value = query,
            onValueChange = vm::setQuery,
            singleLine = true,
            placeholder = { Text("খুঁজুন — জাপানি / romaji / বাংলা / English") },
            leadingIcon = { AppIcon(AppIcons.Search, size = 22.dp, tint = cs.onSurfaceVariant) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    AppIcon(
                        AppIcons.Close, size = 20.dp, tint = cs.onSurfaceVariant,
                        modifier = Modifier.clickable { vm.setQuery("") }.padding(12.dp)
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cs.surfaceContainerHigh,
                unfocusedContainerColor = cs.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )
        Text(
            "${results.size}টি শব্দ",
            style = MaterialTheme.typography.labelMedium,
            color = cs.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )

        when {
            loaded && total == 0 -> EmptyState(
                AppIcons.Inbox, "এখনো কোনো শব্দ নেই",
                "লেসনের JSON ফাইলে শব্দ যোগ করলে এখানে খোঁজা যাবে।"
            )
            results.isEmpty() && loaded -> EmptyState(
                AppIcons.Search, "কোনো শব্দ পাওয়া যায়নি", "অন্য কিছু লিখে খুঁজে দেখুন।"
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results) { lw -> SearchRow(lw) { tts.speak(lw.word.reading) } }
            }
        }
    }
}

@Composable
private fun SearchRow(lw: LessonWord, onSpeak: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val w = lw.word
    SoftCard(Modifier.fillMaxWidth(), onClick = onSpeak) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                BigText(w.word, 26.sp, color = cs.onSurface, weight = FontWeight.Medium)
                if (w.reading != w.word) {
                    Text(w.reading, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                }
                Text(w.romaji, style = MaterialTheme.typography.labelMedium, color = cs.outline)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    w.bn,
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.primary,
                    textAlign = TextAlign.End
                )
                Text(
                    w.en,
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.outline,
                    textAlign = TextAlign.End
                )
                Text(
                    "${lw.level.uppercase()} · L${lw.lessonNo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.tertiary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
