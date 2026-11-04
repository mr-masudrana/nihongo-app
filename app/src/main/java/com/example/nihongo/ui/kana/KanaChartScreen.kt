package com.example.nihongo.ui.kana

import com.example.nihongo.ui.components.tr
import androidx.compose.animation.animateColorAsState
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.AppIcon
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

/** ট্যাবের ক্রম: মূল | দাকুতেন | যৌগিক */
private val GROUPS = listOf(Triple("basic", "মূল", "Basic"), Triple("dakuten", "দাকুতেন", "Dakuten"), Triple("yoon", "যৌগিক", "Combined"))

/** গ্রিডে কানার কলাম: মূল/দাকুতেনে ৫টি (a i u e o), যৌগিকে ৩টি (ya yu yo)। না-থাকা অক্ষরের ঘর ফাঁকা থাকে। */
private fun slotOf(k: KanaEntity, group: String): Int {
    val v = k.romaji.last()
    return if (group == "yoon") {
        when (v) { 'a' -> 0; 'u' -> 1; else -> 2 }
    } else {
        when (v) { 'a' -> 0; 'i' -> 1; 'u' -> 2; 'e' -> 3; 'o' -> 4; else -> 0 }   // ん → প্রথম ঘর
    }
}

@Composable
fun KanaChartScreen(vm: KanaViewModel, type: String) {
    val tts = LocalTts.current
    val allKana by vm.kana.collectAsState()
    val savedGroup by vm.group.collectAsState()
    LaunchedEffect(type) { vm.selectType(type) }
    // পুরোনো "all" অবস্থা থাকলে মূল ট্যাবে নিয়ে যায়
    LaunchedEffect(savedGroup) { if (GROUPS.none { it.first == savedGroup }) vm.selectGroup("basic") }
    val group = if (GROUPS.any { it.first == savedGroup }) savedGroup else "basic"

    // ডেটা বদলের ঝলক এড়াতে স্ক্রিনেও টাইপ ও গ্রুপ দিয়ে ছেঁকে নেওয়া হয়
    val kana = remember(allKana, type, group) { allKana.filter { it.type == type && it.grp == group } }
    var selectedId by rememberSaveable { mutableStateOf(-1) }
    val selected = kana.firstOrNull { it.id == selectedId }

    val cols = if (group == "yoon") 3 else 5
    // প্রতি সারিতে নির্দিষ্ট কলামে অক্ষর, বাকি ঘর null (ফাঁকা)
    val rows = remember(kana, group) {
        kana.groupBy { it.rowName }.values.map { items ->
            val slots = arrayOfNulls<KanaEntity>(cols)
            items.forEach { slots[slotOf(it, group)] = it }
            slots.toList()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(8.dp))
        SegmentedTabs(
            options = GROUPS.map { tr(it.second, it.third) },
            selectedIndex = GROUPS.indexOfFirst { it.first == group },
            onSelect = { vm.selectGroup(GROUPS[it].first) }
        )
        Spacer(Modifier.height(12.dp))

        PreviewCard(selected) { selected?.let { tts.speak(it.symbol) } }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rows) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { k ->
                        if (k == null) {
                            Spacer(Modifier.weight(1f))
                        } else {
                            KanaTile(k, selected = k.id == selectedId, modifier = Modifier.weight(1f)) {
                                selectedId = k.id
                                tts.speak(k.symbol)
                            }
                        }
                    }
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
                tr("যেকোনো কানায় ট্যাপ করুন", "Tap any kana"),
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
                        tr("বাংলায়: ${k.bn}", "In Bengali: ${k.bn}"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                AppIcon(AppIcons.Speaker, size = 28.dp, tint = cs.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun KanaTile(k: KanaEntity, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) cs.primary else cs.surfaceContainerLow, tween(200), label = "tileBg")
    val fg by animateColorAsState(if (selected) cs.onPrimary else cs.onSurface, tween(200), label = "tileFg")
    SoftCard(
        modifier = modifier.fillMaxWidth(),
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
