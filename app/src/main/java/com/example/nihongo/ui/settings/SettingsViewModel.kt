package com.example.nihongo.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.nihongo.data.AppSettings
import com.example.nihongo.data.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository.get(app)
    val settings: StateFlow<AppSettings> = repo.settings

    fun setDailyGoal(v: Int) = repo.setDailyGoal(v)
    fun setThemeMode(v: Int) = repo.setThemeMode(v)
    fun setTtsRate(v: Float) = repo.setTtsRate(v)
}
