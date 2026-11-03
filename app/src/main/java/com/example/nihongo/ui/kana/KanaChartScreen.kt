package com.example.nihongo.ui.kana

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.data.KanaEntity
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.ChipRow
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.components.SegmentedTabs
import com.example.nihongo.ui.components.SoftCard

@Composable
fun KanaChartScreen(vm: KanaViewModel, type: String) {
    val tts = LocalTts.current
    val allKana by vm.kana.collectAsState()
    LaunchedEffect(type) { vm.selectType(type) }
    // ডেটা বদলের ঝলক এড়াতে স্ক্রিনেও টাইপ দিয়ে ছেঁকে নেওয়া হয়
    val kana = remember(allKana, type) { allKana.filter { it.type == type } }
    val group by vm.group.collectAsState()
    var selectedId by rememberSaveable { mutableStateOf(-1) }
    val selected = kana.firstOrNull { it.id == selectedId }

    val groups = listOf("all" to "সব", "basic" to "মূল", "dakuten" to "দাকুতেন", "yoon" to "যৌগিক")

    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(8.dp))
        ChipRow(groups, group, vm::selectGroup)
        Spacer(Modifier.height(12.dp))

        PreviewCard(selected) { selected?.let { tts.speak(it.symbol) } }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(kana, key = { it.id }) { k ->
                KanaTile(k, selected = k.id == selectedId) {
                    selectedId = k.id
                    tts.speak(k.symbol)
                }
            }
        }
    }
}

/** বেছে নেওয়া কানার বড় প্রিভিউ — রোমাজি ও বাংলা উচ্চারণ সহ */
@Composable
private fun PreviewCard(k: KanaEntity?, onSpeak: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    SoftCard(
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(92.dp),
        color = cs.primaryContainer,
        borderColor = cs.primaryContainer,
        onClick = if (k != null) onSpeak else null
    ) {
        if (k == null) {
            Text(
                "যেকোনো কানায় ট্যাপ করুন 👇",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = cs.onPrimaryContainer.copy(alpha = 0.7f)
            )
        } else {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BigText(k.symbol, 52.sp, color = cs.onPrimaryContainer, weight = FontWeight.Medium)
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(k.romaji, style = MaterialTheme.typography.titleLarge, color = cs.onPrimaryContainer)
                    Text(
                        "বাংলায়: ${k.bn}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Text("🔊", fontSize = 26.sp)
            }
        }
    }
}

@Composable
private fun KanaTile(k: KanaEntity, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) cs.primary else cs.surfaceContainerLow, tween(200), label = "tileBg")
    val fg by animateColorAsState(if (selected) cs.onPrimary else cs.onSurface, tween(200), label = "tileFg")
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        color = bg,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BigText(k.symbol, 28.sp, color = fg, weight = FontWeight.Medium)
            Text(k.romaji, style = MaterialTheme.typography.labelMedium, color = fg.copy(alpha = 0.7f))
        }
    }
}
