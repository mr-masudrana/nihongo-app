package com.example.nihongo.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.KanaRepository
import com.example.nihongo.data.StatsRepository
import com.example.nihongo.data.VocabRepository
import com.example.nihongo.domain.Stats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    val stats: StateFlow<Stats> = StatsRepository.get(app).stats

    val learnedKana: StateFlow<Int> = KanaRepository.get(app).learnedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val learnedVocab: StateFlow<Int> = VocabRepository.get(app).learnedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
