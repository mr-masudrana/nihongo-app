package com.example.nihongo.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.nihongo.data.StatsRepository
import com.example.nihongo.domain.Stats
import kotlinx.coroutines.flow.StateFlow

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    val stats: StateFlow<Stats> = StatsRepository.get(app).stats
}
