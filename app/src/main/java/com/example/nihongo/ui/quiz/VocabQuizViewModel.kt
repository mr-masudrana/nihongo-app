package com.example.nihongo.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.StatsRepository
import com.example.nihongo.data.VocabRepository
import com.example.nihongo.domain.QuizBuilder
import com.example.nihongo.domain.VocabDir
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VocabQuizConfig(
    val category: String = "all",
    val dir: VocabDir = VocabDir.WORD_TO_MEANING
)

class VocabQuizViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = VocabRepository.get(app)
    private val runner = QuizRunner(StatsRepository.get(app))

    val ui: StateFlow<QuizUi> = runner.ui
    val config = MutableStateFlow(VocabQuizConfig())

    fun setCategory(c: String) { config.update { it.copy(category = c) } }
    fun setDir(d: VocabDir) { config.update { it.copy(dir = d) } }

    fun start() {
        viewModelScope.launch {
            repo.seedIfEmpty()
            val c = config.value
            runner.start(QuizBuilder.vocabQuestions(repo.allVocab(), c.category, c.dir))
        }
    }

    fun select(i: Int) { runner.select(i) }
    fun next() { runner.next() }
    fun back() { runner.backToSetup() }
}
