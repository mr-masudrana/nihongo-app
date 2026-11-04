package com.example.nihongo.domain

import java.util.TimeZone

data class Stats(
    val xp: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastDay: Long = -1L,   // সর্বশেষ যেদিন শেখা হয়েছে (epoch day, লোকাল টাইম)
    val todayXp: Int = 0,
    val todayDay: Long = -1L,  // todayXp যে দিনের
    val quizzes: Int = 0,
    /** প্রতিটি সেকশনের হাই স্কোর (সর্বোচ্চ টানা সঠিক উত্তর): "kana" | "n5" | "n4" */
    val highScores: Map<String, Int> = emptyMap()
) {
    fun highScore(kind: String): Int = highScores[kind] ?: 0
}

object Gamification {
    // চ্যালেঞ্জ মোড (আনলিমিটেড মোডে XP নেই — সেখানে শুধু হাই স্কোর)
    const val XP_CORRECT = 10          // চ্যালেঞ্জে সঠিক উত্তর
    const val XP_SPEED_BONUS = 5       // প্রথম ৫ সেকেন্ডে সঠিক উত্তর দিলে
    const val XP_PERFECT_BONUS = 20    // ১০টিই সঠিক হলে
    const val XP_PER_LEVEL = 100
    const val CHALLENGE_QUESTIONS = 10
    const val CHALLENGE_SECONDS = 15
    const val DAILY_GOAL = 50

    private const val DAY_MS = 86_400_000L

    /** লোকাল টাইমজোন অনুযায়ী "আজ" কততম দিন (java.time ছাড়া — minSdk 24 তে নিরাপদ) */
    fun dayOf(
        nowMs: Long = System.currentTimeMillis(),
        tzOffsetMs: Int = TimeZone.getDefault().getOffset(nowMs)
    ): Long = Math.floorDiv(nowMs + tzOffsetMs, DAY_MS)

    /** যেকোনো শেখার অ্যাক্টিভিটি (xp = ০ হলেও) স্ট্রিকে গণ্য হয় */
    fun applyActivity(s: Stats, amount: Int, today: Long): Stats {
        val streak = when {
            s.lastDay == today -> maxOf(s.streak, 1)
            s.lastDay == today - 1 -> s.streak + 1
            else -> 1
        }
        val todayXp = (if (s.todayDay == today) s.todayXp else 0) + amount
        return s.copy(
            xp = s.xp + amount,
            streak = streak,
            bestStreak = maxOf(s.bestStreak, streak),
            lastDay = today,
            todayXp = todayXp,
            todayDay = today
        )
    }

    /** গতকাল বা আজ শেখা না হলে স্ট্রিক ভেঙে গেছে (০) */
    fun currentStreak(s: Stats, today: Long): Int =
        if (s.lastDay == today || s.lastDay == today - 1) s.streak else 0

    fun xpToday(s: Stats, today: Long): Int = if (s.todayDay == today) s.todayXp else 0

    fun level(xp: Int): Int = xp / XP_PER_LEVEL + 1
    fun levelProgress(xp: Int): Float = (xp % XP_PER_LEVEL) / XP_PER_LEVEL.toFloat()
}
