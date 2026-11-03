package com.example.nihongo.domain

import java.util.TimeZone

data class Stats(
    val xp: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastDay: Long = -1L,   // সর্বশেষ যেদিন শেখা হয়েছে (epoch day, লোকাল টাইম)
    val todayXp: Int = 0,
    val todayDay: Long = -1L,  // todayXp যে দিনের
    val quizzes: Int = 0
)

object Gamification {
    const val XP_CORRECT = 10          // কুইজে সঠিক উত্তর
    const val XP_PERFECT_BONUS = 20    // সব উত্তর সঠিক হলে বোনাস
    const val XP_FLASH_KNOWN = 5       // ফ্ল্যাশকার্ডে "জানি"
    const val XP_FLASH_AGAIN = 1       // ফ্ল্যাশকার্ডে "জানি না" (চেষ্টাও স্ট্রিকে গণ্য)
    const val DAILY_GOAL = 50
    const val XP_PER_LEVEL = 100

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
