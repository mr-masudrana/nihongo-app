package com.example.nihongo.ui.quiz

import android.os.SystemClock
import com.example.nihongo.data.StatsRepository
import com.example.nihongo.domain.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class QuizPhase { SETUP, PLAYING, FINISHED }

/**
 * score = টানা সঠিক উত্তর। index = এখন কততম প্রশ্ন (০ থেকে)। answeredCount = এ পর্যন্ত উত্তর দেওয়া প্রশ্ন।
 * startedAt / finishedAt = SystemClock.elapsedRealtime() — কুইজের সময় মাপার জন্য।
 * over = ভুল উত্তর দেওয়া হয়েছে; "ফলাফল দেখুন" চাপলে FINISHED হবে।
 */
data class QuizUi(
    val phase: QuizPhase = QuizPhase.SETUP,
    val current: QuizQuestion? = null,
    val selected: Int? = null,
    val score: Int = 0,
    val highScore: Int = 0,
    val newRecord: Boolean = false,
    val over: Boolean = false,
    val empty: Boolean = false,
    val index: Int = 0,
    val answeredCount: Int = 0,
    val startedAt: Long = 0L,
    val finishedAt: Long = 0L
) {
    val answered: Boolean get() = selected != null
    val finishing: Boolean get() = over
    /** মোট সময় (মিলিসেকেন্ড) — চলার সময় null, শেষ হলে নির্দিষ্ট */
    val totalMs: Long get() = if (finishedAt > 0L) finishedAt - startedAt else 0L
}

/**
 * কানা ও শব্দ কুইজ — অসীম প্রশ্ন, প্রথম ভুলে রান শেষ।
 * - শুরু হওয়ার সাথে সাথে সময় চলে ও প্রশ্ন গোনা হয়।
 * - হাই স্কোর (টানা সঠিক) শুধু ভুল উত্তরে রান শেষ হলে সেভ হয়; মাঝপথে বের হলে রানটি বাদ।
 */
class QuizRunner(private val stats: StatsRepository) {
    private val _ui = MutableStateFlow(QuizUi())
    val ui: StateFlow<QuizUi> = _ui.asStateFlow()

    private var kind = ""
    private var source: (() -> List<QuizQuestion>)? = null
    private val queue = ArrayDeque<QuizQuestion>()
    private var lastPrompt: String? = null

    /** kind = হাই স্কোরের সেকশন ("kana" | "n5" | "n4"); source প্রতিবার নতুন শাফল করা প্রশ্নের ব্যাচ দেয় */
    fun start(kind: String, source: () -> List<QuizQuestion>) {
        this.kind = kind
        this.source = source
        queue.clear()
        lastPrompt = null
        stats.recordActivity() // শেখার অ্যাক্টিভিটি হিসেবে স্ট্রিকে গণ্য
        val first = pull()
        _ui.value = if (first == null) QuizUi(phase = QuizPhase.FINISHED, empty = true)
        else QuizUi(
            phase = QuizPhase.PLAYING, current = first,
            highScore = stats.stats.value.highScore(kind),
            startedAt = SystemClock.elapsedRealtime()
        )
    }

    private fun pull(): QuizQuestion? {
        if (queue.isEmpty()) {
            val batch = source?.invoke().orEmpty().toMutableList()
            // নতুন ব্যাচের প্রথম প্রশ্ন আগের প্রশ্নের হুবহু একই হলে সরিয়ে দিই
            if (batch.size > 1 && batch.first().prompt == lastPrompt) batch.add(batch.removeAt(0))
            queue.addAll(batch)
        }
        return queue.removeFirstOrNull()?.also { lastPrompt = it.prompt }
    }

    fun select(i: Int) {
        val s = _ui.value
        val q = s.current ?: return
        if (s.selected != null || s.over) return
        if (i == q.correctIndex) {
            _ui.value = s.copy(selected = i, score = s.score + 1, answeredCount = s.answeredCount + 1)
        } else {
            val record = stats.recordRun(kind, s.score)
            _ui.value = s.copy(
                selected = i, over = true, newRecord = record,
                answeredCount = s.answeredCount + 1,
                highScore = stats.stats.value.highScore(kind),
                finishedAt = SystemClock.elapsedRealtime() // ভুল উত্তরের মুহূর্তে সময় থামে
            )
        }
    }

    fun next() {
        val s = _ui.value
        if (s.selected == null) return
        if (s.over) {
            _ui.value = s.copy(phase = QuizPhase.FINISHED)
            return
        }
        val q = pull() ?: run {
            _ui.value = s.copy(phase = QuizPhase.FINISHED, empty = true)
            return
        }
        _ui.value = s.copy(current = q, selected = null, index = s.index + 1)
    }

    fun backToSetup() {
        _ui.value = QuizUi()
    }
}
