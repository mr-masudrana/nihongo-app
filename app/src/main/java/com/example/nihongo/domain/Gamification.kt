package com.example.nihongo.domain

import java.util.TimeZone

data class Stats(
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastDay: Long = -1L,   // সর্বশেষ যেদিন শেখা হয়েছে (epoch day, লোকাল টাইম)
    val quizzes: Int = 0,
    /** প্রতিটি সেকশনের হাই স্কোর (সর্বোচ্চ টানা সঠিক উত্তর): "kana" | "n5" | "n4" */
    val highScores: Map<String, Int> = emptyMap()
) {
    fun highScore(kind: String): Int = highScores[kind] ?: 0
}

object Gamification {
    private const val DAY_MS = 86_400_000L

    /** লোকাল টাইমজোন অনুযায়ী "আজ" কততম দিন (java.time ছাড়া — minSdk 24 তে নিরাপদ) */
    fun dayOf(
        nowMs: Long = System.currentTimeMillis(),
        tzOffsetMs: Int = TimeZone.getDefault().getOffset(nowMs)
    ): Long = Math.floorDiv(nowMs + tzOffsetMs, DAY_MS)

    /** যেকোনো শেখার অ্যাক্টিভিটি স্ট্রিকে গণ্য হয় */
    fun applyActivity(s: Stats, today: Long): Stats {
        val streak = when {
            s.lastDay == today -> maxOf(s.streak, 1)
            s.lastDay == today - 1 -> s.streak + 1
            else -> 1
        }
        return s.copy(streak = streak, bestStreak = maxOf(s.bestStreak, streak), lastDay = today)
    }

    /** গতকাল বা আজ শেখা না হলে স্ট্রিক ভেঙে গেছে (০) */
    fun currentStreak(s: Stats, today: Long): Int =
        if (s.lastDay == today || s.lastDay == today - 1) s.streak else 0
}
