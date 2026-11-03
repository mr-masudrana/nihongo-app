package com.example.nihongo.ui.flashcard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.KanaEntity
import com.example.nihongo.data.KanaRepository
import com.example.nihongo.data.StatsRepository
import com.example.nihongo.domain.Gamification
import com.example.nihongo.domain.Srs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlashUi(
    val type: String = "hiragana",
    val queue: List<KanaEntity> = emptyList(),
    val index: Int = 0,
    val flipped: Boolean = false,
    val loading: Boolean = true,
    val known: Int = 0,
    val again: Int = 0
) {
    val current: KanaEntity? get() = queue.getOrNull(index)
    val finished: Boolean get() = !loading && queue.isNotEmpty() && index >= queue.size
    val empty: Boolean get() = !loading && queue.isEmpty()
}

class FlashcardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = KanaRepository.get(app)
    private val stats = StatsRepository.get(app)
    private val _ui = MutableStateFlow(FlashUi())
    val ui: StateFlow<FlashUi> = _ui.asStateFlow()
    private var loadJob: Job? = null

    companion object {
        const val MAX_REVIEWS = 20
        const val MAX_NEW = 10
    }

    init { load("hiragana") }

    fun setType(type: String) { if (type != _ui.value.type) load(type) }
    fun restart() = load(_ui.value.type)

    private fun load(type: String) {
        loadJob?.cancel()
        _ui.value = FlashUi(type = type, loading = true)
        loadJob = viewModelScope.launch {
            repo.seedIfEmpty()
            val now = System.currentTimeMillis()
            val reviews = repo.dueReviews(type, now, MAX_REVIEWS)
            val fresh = repo.newCards(type, MAX_NEW)
            _ui.value = FlashUi(type = type, queue = reviews + fresh, loading = false)
        }
    }

    fun flip() = _ui.update { it.copy(flipped = true) }

    fun answer(known: Boolean) {
        val s = _ui.value
        val card = s.current ?: return
        val updated = Srs.grade(card, known)
        stats.addXp(if (known) Gamification.XP_FLASH_KNOWN else Gamification.XP_FLASH_AGAIN)
        viewModelScope.launch { repo.update(updated) }
        _ui.value = s.copy(
            // ভুল হলে কার্ডটি এই সেশনের শেষে আবার আসবে
            queue = if (known) s.queue else s.queue + updated,
            index = s.index + 1,
            flipped = false,
            known = s.known + if (known) 1 else 0,
            again = s.again + if (known) 0 else 1
        )
    }
}
