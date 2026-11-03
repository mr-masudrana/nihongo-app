package com.example.nihongo.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.ContentRepository
import com.example.nihongo.data.LessonWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** সব লেসনের শব্দে খোঁজে (assets/lessons/...) */
class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ContentRepository.get(app)

    val query = MutableStateFlow("")
    private val all = MutableStateFlow<List<LessonWord>>(emptyList())
    val loaded = MutableStateFlow(false)

    val results: StateFlow<List<LessonWord>> = combine(all, query) { list, q ->
        val t = q.trim()
        if (t.isEmpty()) list
        else list.filter { lw ->
            val w = lw.word
            w.word.contains(t, ignoreCase = true) ||
                w.reading.contains(t, ignoreCase = true) ||
                w.romaji.contains(t, ignoreCase = true) ||
                w.bn.contains(t, ignoreCase = true) ||
                w.en.contains(t, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** কোনো শব্দ আদৌ আছে কিনা (খালি অবস্থার বার্তার জন্য) */
    val total: StateFlow<Int> = combine(all, loaded) { list, _ -> list.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            all.value = withContext(Dispatchers.IO) { repo.allWords() }
            loaded.value = true
        }
    }

    fun setQuery(q: String) { query.value = q }
}
