package com.example.nihongo.ui.quiz

import com.example.nihongo.data.StatsRepository
import com.example.nihongo.domain.Gamification
import com.example.nihongo.domain.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class QuizPhase { SETUP, PLAYING, FINISHED }

/** UNLIMITED = অসীম প্রশ্ন, প্রথম ভুলে শেষ, শুধু হাই স্কোর। CHALLENGE = নির্দিষ্ট প্রশ্ন + প্রতি প্রশ্নে সময়, XP ও লেভেল */
enum class QuizMode { UNLIMITED, CHALLENGE }

/**
 * score = সঠিক উত্তর (আনলিমিটেডে টানা, চ্যালেঞ্জে মোট)।
 * over = আনলিমিটেডে ভুল উত্তর দেওয়া হয়েছে; "ফলাফল দেখুন" চাপলে FINISHED হবে।
 * selected = -1 মানে সময় শেষ (চ্যালেঞ্জ)। empty = প্রশ্নই তৈরি করা যায়নি।
 */
data class QuizUi(
    val phase: QuizPhase = QuizPhase.SETUP,
    val mode: QuizMode = QuizMode.UNLIMITED,
    val current: QuizQuestion? = null,
    val selected: Int? = null,
    val score: Int = 0,
    val highScore: Int = 0,
    val newRecord: Boolean = false,
    val over: Boolean = false,
    val empty: Boolean = false,
    // চ্যালেঞ্জ
    val index: Int = 0,
    val total: Int = 0,
    val timedOut: Boolean = false,
    val lastXp: Int = 0,
    val xpEarned: Int = 0,
    val perfectBonus: Int = 0,
    val xpAtStart: Int = 0
) {
    val answered: Boolean get() = selected != null
    val isLastChallenge: Boolean get() = mode == QuizMode.CHALLENGE && index >= total - 1
    /** এখন "ফলাফল দেখুন" দেখাবে কি না */
    val finishing: Boolean get() = over || isLastChallenge
}

/** কানা ও শব্দ কুইজ — দুই মোডের লজিক এখানে */
class QuizRunner(private val stats: StatsRepository) {
    private val _ui = MutableStateFlow(QuizUi())
    val ui: StateFlow<QuizUi> = _ui.asStateFlow()

    private var kind = ""
    private var source: (() -> List<QuizQuestion>)? = null
    private val queue = ArrayDeque<QuizQuestion>()
    private var lastPrompt: String? = null

    /** kind = হাই স্কোরের সেকশন ("kana" | "n5" | "n4"); source প্রতিবার নতুন শাফল করা প্রশ্নের ব্যাচ দেয় */
    fun start(kind: String, mode: QuizMode, source: () -> List<QuizQuestion>) {
        this.kind = kind
        this.source = source
        queue.clear()
        lastPrompt = null
        _ui.value = QuizUi(mode = mode) // pull() এই মোড দেখে
        stats.addXp(0) // শেখার অ্যাক্টিভিটি হিসেবে স্ট্রিকে গণ্য
        val total: Int
        if (mode == QuizMode.CHALLENGE) {
            queue.addAll(source().take(Gamification.CHALLENGE_QUESTIONS))
            total = queue.size
        } else total = 0
        val first = pull()
        _ui.value = if (first == null) QuizUi(phase = QuizPhase.FINISHED, mode = mode, empty = true)
        else QuizUi(
            phase = QuizPhase.PLAYING, mode = mode, current = first, total = total,
            highScore = stats.stats.value.highScore(kind), xpAtStart = stats.stats.value.xp
        )
    }

    private fun pull(): QuizQuestion? {
        if (queue.isEmpty() && _ui.value.mode == QuizMode.UNLIMITED) {
            val batch = source?.invoke().orEmpty().toMutableList()
            // নতুন ব্যাচের প্রথম প্রশ্ন আগের প্রশ্নের হুবহু একই হলে সরিয়ে দিই
            if (batch.size > 1 && batch.first().prompt == lastPrompt) batch.add(batch.removeAt(0))
            queue.addAll(batch)
        }
        return queue.removeFirstOrNull()?.also { lastPrompt = it.prompt }
    }

    /** fractionLeft = উত্তরের সময় প্রশ্নের সময়ের কত অংশ বাকি ছিল (০..১); আনলিমিটেডে অপ্রয়োজনীয় */
    fun select(i: Int, fractionLeft: Float = 1f) {
        val s = _ui.value
        val q = s.current ?: return
        if (s.selected != null || s.over) return
        val correct = i == q.correctIndex
        if (s.mode == QuizMode.CHALLENGE) {
            val fast = fractionLeft >= FAST_FRACTION
            val xp = if (correct) Gamification.XP_CORRECT + if (fast) Gamification.XP_SPEED_BONUS else 0 else 0
            if (xp > 0) stats.addXp(xp)
            _ui.value = s.copy(
                selected = i, lastXp = xp, xpEarned = s.xpEarned + xp,
                score = s.score + if (correct) 1 else 0
            )
        } else if (correct) {
            _ui.value = s.copy(selected = i, score = s.score + 1)
        } else {
            val record = stats.recordRun(kind, s.score)
            _ui.value = s.copy(
                selected = i, over = true, newRecord = record,
                highScore = stats.stats.value.highScore(kind)
            )
        }
    }

    /** সময় শেষ (শুধু চ্যালেঞ্জ) — উত্তর ভুল ধরা হয়, কিন্তু কুইজ চলতে থাকে */
    fun timeout() {
        val s = _ui.value
        if (s.mode != QuizMode.CHALLENGE || s.selected != null || s.current == null) return
        _ui.value = s.copy(selected = -1, timedOut = true, lastXp = 0)
    }

    fun next() {
        val s = _ui.value
        if (s.selected == null) return
        when {
            s.over -> _ui.value = s.copy(phase = QuizPhase.FINISHED)
            s.mode == QuizMode.CHALLENGE && s.isLastChallenge -> {
                val perfect = s.total > 0 && s.score == s.total
                val bonus = if (perfect) Gamification.XP_PERFECT_BONUS else 0
                if (bonus > 0) stats.addXp(bonus)
                stats.recordQuiz()
                _ui.value = s.copy(phase = QuizPhase.FINISHED, perfectBonus = bonus, xpEarned = s.xpEarned + bonus)
            }
            else -> {
                val q = pull() ?: run {
                    _ui.value = s.copy(phase = QuizPhase.FINISHED, empty = true)
                    return
                }
                _ui.value = s.copy(current = q, selected = null, timedOut = false, lastXp = 0, index = s.index + 1)
            }
        }
    }

    fun backToSetup() {
        _ui.value = QuizUi()
    }

    companion object {
        /** প্রথম ৫ সেকেন্ড (১৫ সেকেন্ডের ২/৩ বাকি) */
        const val FAST_FRACTION = 2f / 3f
    }
}
