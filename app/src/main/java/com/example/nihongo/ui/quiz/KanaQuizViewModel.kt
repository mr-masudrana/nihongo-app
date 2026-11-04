package com.example.nihongo.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.KanaRepository
import com.example.nihongo.data.StatsRepository
import com.example.nihongo.domain.KanaDir
import com.example.nihongo.domain.QuizBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KanaQuizConfig(
    val scripts: String = "hiragana",   // "hiragana" | "katakana" | "both"
    val dir: KanaDir = KanaDir.KANA_TO_ROMAJI,
    val mode: QuizMode = QuizMode.UNLIMITED
)

class KanaQuizViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = KanaRepository.get(app)
    private val runner = QuizRunner(StatsRepository.get(app))

    val ui: StateFlow<QuizUi> = runner.ui
    val config = MutableStateFlow(KanaQuizConfig())

    fun setScripts(s: String) { config.update { it.copy(scripts = s) } }
    fun setDir(d: KanaDir) { config.update { it.copy(dir = d) } }
    fun setMode(m: QuizMode) { config.update { it.copy(mode = m) } }

    fun start() {
        viewModelScope.launch {
            repo.seedIfEmpty()
            val c = config.value
            val scripts = if (c.scripts == "both") setOf("hiragana", "katakana") else setOf(c.scripts)
            val all = repo.allKana()
            runner.start("kana", c.mode) {
                // পুরো পুল শাফল করে দেওয়া হয়; আনলিমিটেডে শেষ হলে আবার নতুন শাফল
                QuizBuilder.kanaQuestions(all, scripts, c.dir, count = Int.MAX_VALUE)
            }
        }
    }

    fun select(i: Int, fractionLeft: Float) { runner.select(i, fractionLeft) }
    fun timeout() { runner.timeout() }
    fun next() { runner.next() }
    fun back() { runner.backToSetup() }
}
