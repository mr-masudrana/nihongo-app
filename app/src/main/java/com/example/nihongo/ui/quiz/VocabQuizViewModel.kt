package com.example.nihongo.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.ContentRepository
import com.example.nihongo.data.StatsRepository
import com.example.nihongo.domain.QuizBuilder
import com.example.nihongo.domain.QuizWord
import com.example.nihongo.domain.VocabDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class VocabQuizConfig(
    val category: String = "all",
    val dir: VocabDir = VocabDir.WORD_TO_MEANING,
    val mode: QuizMode = QuizMode.UNLIMITED
)

/** শব্দ কুইজ — প্রশ্ন আসে লেসনের শব্দ (assets/<level>/vocabulary/...) থেকে; বিভাগ = লেসন */
class VocabQuizViewModel(app: Application) : AndroidViewModel(app) {
    private val content = ContentRepository.get(app)
    private val runner = QuizRunner(StatsRepository.get(app))

    val ui: StateFlow<QuizUi> = runner.ui
    val config = MutableStateFlow(VocabQuizConfig())

    /** (key, লেবেল) — প্রথমটি সবসময় "all" */
    val categories = MutableStateFlow(listOf("all" to "সব"))
    private var words: List<QuizWord> = emptyList()
    private var level = "n5"
    private var loadedLevel: String? = null

    /** "n5" বা "n4" — শুধু ওই লেভেলের লেসনের শব্দ কুইজে আসবে */
    fun setLevel(l: String) {
        if (l == level && loadedLevel == l) return
        level = l
        config.update { it.copy(category = "all") }
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val lvl = level
        val all = withContext(Dispatchers.IO) { content.allWords() }.filter { it.level == lvl }
        loadedLevel = lvl
        words = all.mapIndexed { i, lw ->
            QuizWord(
                id = i,
                word = lw.word.word, reading = lw.word.reading, romaji = lw.word.romaji,
                meaningBn = lw.word.bn, meaningEn = lw.word.en,
                category = "${lw.level}_${lw.lessonNo}"
            )
        }
        categories.value = listOf("all" to "সব") + all
            .map { it.level to it.lessonNo }
            .distinct()
            .map { (lv, no) -> "${lv}_$no" to "L$no" }
    }

    fun setCategory(c: String) { config.update { it.copy(category = c) } }
    fun setDir(d: VocabDir) { config.update { it.copy(dir = d) } }
    fun setMode(m: QuizMode) { config.update { it.copy(mode = m) } }

    fun start() {
        viewModelScope.launch {
            if (loadedLevel != level) load()
            val c = config.value
            val pool = words
            runner.start(level, c.mode) {
                QuizBuilder.vocabQuestions(pool, c.category, c.dir, count = Int.MAX_VALUE)
            }
        }
    }

    fun select(i: Int, fractionLeft: Float) { runner.select(i, fractionLeft) }
    fun timeout() { runner.timeout() }
    fun next() { runner.next() }
    fun back() { runner.backToSetup() }
}
