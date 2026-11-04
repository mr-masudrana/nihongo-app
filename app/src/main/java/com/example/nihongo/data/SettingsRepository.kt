package com.example.nihongo.data

import android.content.Context
import com.example.nihongo.domain.Gamification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** প্রোফাইল ছবির ফাইলের নাম (filesDir-এ থাকে) */
const val PROFILE_PHOTO = "profile.jpg"

/** themeMode: 0 = সিস্টেম অনুযায়ী, 1 = লাইট, 2 = ডার্ক */
data class AppSettings(
    val dailyGoal: Int = Gamification.DAILY_GOAL,
    val themeMode: Int = 0,
    val ttsRate: Float = 1.0f,
    val userName: String = "",
    val photoVersion: Long = 0L   // ছবি বদলালে বাড়ে — UI নতুন করে লোড করে
)

/** সেটিংস SharedPreferences-এ থাকে (Room রিসেট হলেও মুছবে না) */
class SettingsRepository private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun load() = AppSettings(
        dailyGoal = prefs.getInt("dailyGoal", Gamification.DAILY_GOAL),
        themeMode = prefs.getInt("themeMode", 0),
        ttsRate = prefs.getFloat("ttsRate", 1.0f),
        userName = prefs.getString("userName", "").orEmpty(),
        photoVersion = prefs.getLong("photoVersion", 0L)
    )

    @Synchronized
    fun setDailyGoal(v: Int) {
        prefs.edit().putInt("dailyGoal", v).apply()
        _settings.value = _settings.value.copy(dailyGoal = v)
    }

    @Synchronized
    fun setThemeMode(v: Int) {
        prefs.edit().putInt("themeMode", v).apply()
        _settings.value = _settings.value.copy(themeMode = v)
    }

    @Synchronized
    fun setTtsRate(v: Float) {
        prefs.edit().putFloat("ttsRate", v).apply()
        _settings.value = _settings.value.copy(ttsRate = v)
    }

    @Synchronized
    fun setUserName(v: String) {
        prefs.edit().putString("userName", v).apply()
        _settings.value = _settings.value.copy(userName = v)
    }

    @Synchronized
    fun bumpPhoto() {
        val v = System.currentTimeMillis()
        prefs.edit().putLong("photoVersion", v).apply()
        _settings.value = _settings.value.copy(photoVersion = v)
    }

    companion object {
        @Volatile private var INSTANCE: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
