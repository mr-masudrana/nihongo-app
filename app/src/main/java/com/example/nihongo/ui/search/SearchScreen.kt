package com.example.nihongo.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.nihongo.ui.components.ScreenHeader
import com.example.nihongo.ui.vocab.VocabList
import com.example.nihongo.ui.vocab.VocabListViewModel

@Composable
fun SearchScreen(vm: VocabListViewModel) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Search", "শব্দ খুঁজুন ও উচ্চারণ শুনুন")
        VocabList(vm)
    }
}
