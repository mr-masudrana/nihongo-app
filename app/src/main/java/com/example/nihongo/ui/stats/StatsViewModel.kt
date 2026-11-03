package com.example.nihongo.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.SettingsRepository
import com.example.nihongo.data.StatsRepository
import com.example.nihongo.domain.Stats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    val stats: StateFlow<Stats> = StatsRepository.get(app).stats

    val dailyGoal: StateFlow<Int> = SettingsRepository.get(app).settings
        .map { it.dailyGoal }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsRepository.get(app).settings.value.dailyGoal)

}
