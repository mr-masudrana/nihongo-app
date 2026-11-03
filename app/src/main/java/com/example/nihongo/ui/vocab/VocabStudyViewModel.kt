package com.example.nihongo.ui.vocab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.VocabEntity
import com.example.nihongo.data.StatsRepository
import com.example.nihongo.data.VocabRepository
import com.example.nihongo.domain.Gamification
import com.example.nihongo.domain.Srs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VocabStudyUi(
    val category: String = "all",
    val queue: List<VocabEntity> = emptyList(),
    val index: Int = 0,
    val flipped: Boolean = false,
    val loading: Boolean = true,
    val known: Int = 0,
    val again: Int = 0
) {
    val current: VocabEntity? get() = queue.getOrNull(index)
    val finished: Boolean get() = !loading && queue.isNotEmpty() && index >= queue.size
    val empty: Boolean get() = !loading && queue.isEmpty()
}

class VocabStudyViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = VocabRepository.get(app)
    private val stats = StatsRepository.get(app)
    private val _ui = MutableStateFlow(VocabStudyUi())
    val ui: StateFlow<VocabStudyUi> = _ui.asStateFlow()
    private var loadJob: Job? = null

    companion object {
        const val MAX_REVIEWS = 20
        const val MAX_NEW = 10
    }

    init { load("all") }

    fun setCategory(c: String) { if (c != _ui.value.category) load(c) }
    fun restart() = load(_ui.value.category)

    private fun load(cat: String) {
        loadJob?.cancel()
        _ui.value = VocabStudyUi(category = cat, loading = true)
        loadJob = viewModelScope.launch {
            repo.seedIfEmpty()
            val now = System.currentTimeMillis()
            val reviews = repo.dueReviews(cat, now, MAX_REVIEWS)
            val fresh = repo.newCards(cat, MAX_NEW)
            _ui.value = VocabStudyUi(category = cat, queue = reviews + fresh, loading = false)
        }
    }

    fun flip() = _ui.update { it.copy(flipped = true) }

    fun answer(known: Boolean) {
        val s = _ui.value
        val card = s.current ?: return
        val st = Srs.next(card.level, known)
        val updated = card.copy(level = st.level, nextReview = st.nextReview)
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
