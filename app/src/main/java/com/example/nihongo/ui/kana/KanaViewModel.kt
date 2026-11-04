package com.example.nihongo.ui.kana

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.KanaEntity
import com.example.nihongo.data.KanaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class KanaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = KanaRepository.get(app)

    val type = MutableStateFlow("hiragana")
    val group = MutableStateFlow("basic")

    val kana: StateFlow<List<KanaEntity>> = combine(type, group) { t, g -> t to g }
        .flatMapLatest { (t, g) -> repo.kana(t, g) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repo.seedIfEmpty() }
    }

    fun selectType(t: String) { type.value = t }
    fun selectGroup(g: String) { group.value = g }
}
