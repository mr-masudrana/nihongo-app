package com.example.nihongo.ui.kana

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.data.KanaEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanaChartScreen(vm: KanaViewModel) {
    val context = LocalContext.current
    val tts = remember { TtsHelper(context) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val kana by vm.kana.collectAsState()
    val type by vm.type.collectAsState()
    val group by vm.group.collectAsState()
    val tabs = listOf("hiragana" to "হিরাগানা", "katakana" to "কাতাকানা")
    val groups = listOf("all" to "সব", "basic" to "মূল", "dakuten" to "দাকুতেন", "yoon" to "যৌগিক")

    Column {
        TopAppBar(title = { Text("কানা চার্ট") }, windowInsets = WindowInsets(0))
        TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == type }) {
            tabs.forEach { (key, label) ->
                Tab(selected = type == key, onClick = { vm.selectType(key) }, text = { Text(label) })
            }
        }
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            groups.forEach { (key, label) ->
                FilterChip(
                    selected = group == key,
                    onClick = { vm.selectGroup(key) },
                    label = { Text(label) }
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(kana, key = { it.id }) { k -> KanaCard(k) { tts.speak(k.symbol) } }
        }
    }
}

@Composable
private fun KanaCard(k: KanaEntity, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(
            Modifier.padding(vertical = 10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(k.symbol, fontSize = 28.sp)
            Text(k.romaji, fontSize = 12.sp)
            Text(k.bn, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}
