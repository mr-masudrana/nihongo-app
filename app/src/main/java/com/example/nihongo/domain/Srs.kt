package com.example.nihongo.domain

import com.example.nihongo.data.KanaEntity

data class SrsState(val level: Int, val nextReview: Long)

/**
 * সহজ Leitner-স্টাইল Spaced Repetition (কানা ও শব্দ — দুটোতেই ব্যবহার হয়)।
 *
 * - "জানি"     → লেভেল +১, পরের রিভিউ INTERVALS_DAYS[লেভেল-১] দিন পর
 * - "জানি না"  → লেভেল ০, ১০ মিনিট পর আবার দেখানো হবে
 */
object Srs {
    private const val MIN = 60_000L
    private const val DAY = 24 * 60 * MIN

    /** লেভেল ১..৭ এর ইন্টারভাল (দিন) */
    val INTERVALS_DAYS = longArrayOf(1, 2, 4, 7, 15, 30, 60)
    val MAX_LEVEL = INTERVALS_DAYS.size

    fun next(level: Int, known: Boolean, now: Long = System.currentTimeMillis()): SrsState =
        if (known) {
            val l = (level + 1).coerceAtMost(MAX_LEVEL)
            SrsState(l, now + INTERVALS_DAYS[l - 1] * DAY)
        } else {
            SrsState(0, now + 10 * MIN)
        }

    fun grade(card: KanaEntity, known: Boolean, now: Long = System.currentTimeMillis()): KanaEntity =
        next(card.level, known, now).let { card.copy(level = it.level, nextReview = it.nextReview) }
}
