package com.example.nihongo.data

import android.content.Context
import com.example.nihongo.domain.Gamification
import com.example.nihongo.domain.Stats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** XP/স্ট্রিক SharedPreferences-এ থাকে (Room ডেটাবেজ রিসেট হলেও মুছবে না) */
class StatsRepository private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("stats", Context.MODE_PRIVATE)
    private val _stats = MutableStateFlow(load())
    val stats: StateFlow<Stats> = _stats.asStateFlow()

    private fun load() = Stats(
        xp = prefs.getInt("xp", 0),
        streak = prefs.getInt("streak", 0),
        bestStreak = prefs.getInt("bestStreak", 0),
        lastDay = prefs.getLong("lastDay", -1L),
        todayXp = prefs.getInt("todayXp", 0),
        todayDay = prefs.getLong("todayDay", -1L),
        quizzes = prefs.getInt("quizzes", 0)
    )

    private fun save(s: Stats) {
        prefs.edit()
            .putInt("xp", s.xp)
            .putInt("streak", s.streak)
            .putInt("bestStreak", s.bestStreak)
            .putLong("lastDay", s.lastDay)
            .putInt("todayXp", s.todayXp)
            .putLong("todayDay", s.todayDay)
            .putInt("quizzes", s.quizzes)
            .apply()
    }

    /** XP যোগ করে এবং আজকের অ্যাক্টিভিটি/স্ট্রিক আপডেট করে। amount = ০ হলেও স্ট্রিক গণ্য হয়। */
    @Synchronized
    fun addXp(amount: Int, nowMs: Long = System.currentTimeMillis()) {
        val updated = Gamification.applyActivity(_stats.value, amount, Gamification.dayOf(nowMs))
        save(updated)
        _stats.value = updated
    }

    @Synchronized
    fun recordQuiz() {
        val updated = _stats.value.copy(quizzes = _stats.value.quizzes + 1)
        save(updated)
        _stats.value = updated
    }

    companion object {
        @Volatile private var INSTANCE: StatsRepository? = null

        fun get(context: Context): StatsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StatsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
