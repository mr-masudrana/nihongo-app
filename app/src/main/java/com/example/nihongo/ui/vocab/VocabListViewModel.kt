package com.example.nihongo.ui.vocab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.VocabEntity
import com.example.nihongo.data.VocabRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VocabListViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = VocabRepository.get(app)

    val category = MutableStateFlow("all")
    val query = MutableStateFlow("")

    val words: StateFlow<List<VocabEntity>> = combine(category, query) { c, q -> c to q.trim() }
        .flatMapLatest { (c, q) -> repo.search(c, q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val learned: StateFlow<Int> = repo.learnedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch { repo.seedIfEmpty() }
    }

    fun selectCategory(c: String) { category.value = c }
    fun setQuery(q: String) { query.value = q }
}
