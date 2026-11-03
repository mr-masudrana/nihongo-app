package com.example.nihongo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.nihongo.ui.flashcard.FlashcardScreen
import com.example.nihongo.ui.flashcard.FlashcardViewModel
import com.example.nihongo.ui.kana.KanaChartScreen
import com.example.nihongo.ui.kana.KanaViewModel
import com.example.nihongo.ui.quiz.KanaQuizViewModel
import com.example.nihongo.ui.quiz.QuizScreen
import com.example.nihongo.ui.quiz.VocabQuizViewModel
import com.example.nihongo.ui.stats.StatsScreen
import com.example.nihongo.ui.stats.StatsViewModel
import com.example.nihongo.ui.vocab.VocabListViewModel
import com.example.nihongo.ui.vocab.VocabScreen
import com.example.nihongo.ui.vocab.VocabStudyViewModel

class MainActivity : ComponentActivity() {
    private val kanaVm: KanaViewModel by viewModels()
    private val flashVm: FlashcardViewModel by viewModels()
    private val vocabListVm: VocabListViewModel by viewModels()
    private val vocabStudyVm: VocabStudyViewModel by viewModels()
    private val kanaQuizVm: KanaQuizViewModel by viewModels()
    private val vocabQuizVm: VocabQuizViewModel by viewModels()
    private val statsVm: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var tab by remember { mutableIntStateOf(0) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0, onClick = { tab = 0 },
                                icon = { Text("あ") }, label = { Text("চার্ট") }
                            )
                            NavigationBarItem(
                                selected = tab == 1, onClick = { tab = 1 },
                                icon = { Text("📇") }, label = { Text("কার্ড") }
                            )
                            NavigationBarItem(
                                selected = tab == 2, onClick = { tab = 2 },
                                icon = { Text("語") }, label = { Text("শব্দ") }
                            )
                            NavigationBarItem(
                                selected = tab == 3, onClick = { tab = 3 },
                                icon = { Text("❓") }, label = { Text("কুইজ") }
                            )
                            NavigationBarItem(
                                selected = tab == 4, onClick = { tab = 4 },
                                icon = { Text("🔥") }, label = { Text("প্রগ্রেস") }
                            )
                        }
                    }
                ) { pad ->
                    Box(Modifier.padding(pad)) {
                        when (tab) {
                            0 -> KanaChartScreen(kanaVm)
                            1 -> FlashcardScreen(flashVm)
                            2 -> VocabScreen(vocabListVm, vocabStudyVm)
                            3 -> QuizScreen(kanaQuizVm, vocabQuizVm, statsVm)
                            else -> StatsScreen(statsVm)
                        }
                    }
                }
            }
        }
    }
}
