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
        quizzes = prefs.getInt("quizzes", 0),
        highScores = HIGH_KEYS.associateWith { prefs.getInt("high_$it", 0) }
    )

    private fun save(s: Stats) {
        val e = prefs.edit()
            .putInt("xp", s.xp)
            .putInt("streak", s.streak)
            .putInt("bestStreak", s.bestStreak)
            .putLong("lastDay", s.lastDay)
            .putInt("todayXp", s.todayXp)
            .putLong("todayDay", s.todayDay)
            .putInt("quizzes", s.quizzes)
        s.highScores.forEach { (k, v) -> e.putInt("high_$k", v) }
        e.apply()
    }

    /** XP যোগ করে এবং আজকের অ্যাক্টিভিটি/স্ট্রিক আপডেট করে। amount = ০ হলেও স্ট্রিক গণ্য হয়। */
    @Synchronized
    fun addXp(amount: Int, nowMs: Long = System.currentTimeMillis()) {
        val updated = Gamification.applyActivity(_stats.value, amount, Gamification.dayOf(nowMs))
        save(updated)
        _stats.value = updated
    }

    /** চ্যালেঞ্জ কুইজ শেষ — কুইজ সংখ্যা বাড়ায় */
    @Synchronized
    fun recordQuiz() {
        val updated = _stats.value.copy(quizzes = _stats.value.quizzes + 1)
        save(updated)
        _stats.value = updated
    }

    /** আনলিমিটেড মোডের একটি রান শেষ (ভুল উত্তর) — কুইজ সংখ্যা বাড়ায় ও হাই স্কোর আপডেট করে। নতুন রেকর্ড হলে true */
    @Synchronized
    fun recordRun(kind: String, score: Int): Boolean {
        val cur = _stats.value
        val newRecord = score > cur.highScore(kind)
        val updated = cur.copy(
            quizzes = cur.quizzes + 1,
            highScores = if (newRecord) cur.highScores + (kind to score) else cur.highScores
        )
        save(updated)
        _stats.value = updated
        return newRecord
    }

    companion object {
        private val HIGH_KEYS = listOf("kana", "n5", "n4")
        @Volatile private var INSTANCE: StatsRepository? = null

        fun get(context: Context): StatsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StatsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
