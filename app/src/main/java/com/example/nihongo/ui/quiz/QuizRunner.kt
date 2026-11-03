package com.example.nihongo.ui.quiz

import com.example.nihongo.data.StatsRepository
import com.example.nihongo.domain.Gamification
import com.example.nihongo.domain.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class QuizPhase { SETUP, PLAYING, FINISHED }

data class QuizUi(
    val phase: QuizPhase = QuizPhase.SETUP,
    val questions: List<QuizQuestion> = emptyList(),
    val index: Int = 0,
    val selected: Int? = null,
    val score: Int = 0,
    val xpEarned: Int = 0
) {
    val current: QuizQuestion? get() = questions.getOrNull(index)
    val answered: Boolean get() = selected != null
    val isLast: Boolean get() = index >= questions.lastIndex
}

/** কানা ও শব্দ কুইজ — দুটোর ViewModel-ই এই একই খেলার লজিক ব্যবহার করে */
class QuizRunner(private val stats: StatsRepository) {
    private val _ui = MutableStateFlow(QuizUi())
    val ui: StateFlow<QuizUi> = _ui.asStateFlow()

    fun start(questions: List<QuizQuestion>) {
        _ui.value = if (questions.isEmpty()) QuizUi(phase = QuizPhase.FINISHED)
        else QuizUi(phase = QuizPhase.PLAYING, questions = questions)
    }

    fun select(i: Int) {
        val s = _ui.value
        val q = s.current ?: return
        if (s.selected != null) return
        val correct = i == q.correctIndex
        val xp = if (correct) Gamification.XP_CORRECT else 0
        stats.addXp(xp) // ভুল হলেও অ্যাক্টিভিটি হিসেবে স্ট্রিকে গণ্য
        _ui.value = s.copy(
            selected = i,
            score = s.score + if (correct) 1 else 0,
            xpEarned = s.xpEarned + xp
        )
    }

    fun next() {
        val s = _ui.value
        if (s.selected == null) return
        if (s.isLast) finish(s) else _ui.value = s.copy(index = s.index + 1, selected = null)
    }

    private fun finish(s: QuizUi) {
        val perfect = s.questions.isNotEmpty() && s.score == s.questions.size
        val bonus = if (perfect) Gamification.XP_PERFECT_BONUS else 0
        if (bonus > 0) stats.addXp(bonus)
        stats.recordQuiz()
        _ui.value = s.copy(phase = QuizPhase.FINISHED, xpEarned = s.xpEarned + bonus)
    }

    fun backToSetup() {
        _ui.value = QuizUi()
    }
}
