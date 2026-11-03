package com.example.nihongo.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.components.ActionButton
import com.example.nihongo.ui.components.AppProgressBar
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.EmptyState
import com.example.nihongo.ui.components.LocalTts
import com.example.nihongo.ui.components.ProgressRing
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.SpeakButton
import com.example.nihongo.ui.components.StatPill
import com.example.nihongo.ui.components.entrance

private val optionLetters = listOf("ক", "খ", "গ", "ঘ", "ঙ")

@Composable
fun QuizPlay(ui: QuizUi, onSelect: (Int) -> Unit, onNext: () -> Unit) {
    val q = ui.current ?: return
    val cs = MaterialTheme.colorScheme
    val tts = LocalTts.current
    val haptic = LocalHapticFeedback.current

    // উত্তর দেওয়ার পর সঠিক উচ্চারণ বাজাও
    LaunchedEffect(ui.index, ui.selected) {
        if (ui.selected != null) q.speak?.let { tts.speak(it) }
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppProgressBar(
                    progress = (ui.index + if (ui.answered) 1 else 0).toFloat() / ui.questions.size,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "${ui.index + 1} / ${ui.questions.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.onSurfaceVariant
                )
            }

            // প্রশ্ন কার্ড
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(cs.primaryContainer, cs.surfaceContainerHigh)))
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BigText(
                        q.prompt, q.promptSp.sp,
                        color = cs.onPrimaryContainer, weight = FontWeight.Medium, textAlign = TextAlign.Center
                    )
                    if (q.hint != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            q.hint,
                            style = MaterialTheme.typography.bodyLarge,
                            color = cs.onPrimaryContainer.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (q.speak != null) {
                        Spacer(Modifier.height(12.dp))
                        SpeakButton(onClick = { tts.speak(q.speak) }, diameter = 44.dp)
                    }
                }
            }

            q.options.forEachIndexed { i, text ->
                OptionCard(
                    letter = optionLetters.getOrElse(i) { "•" },
                    text = text,
                    sp = q.optionSp,
                    answered = ui.answered,
                    isCorrect = i == q.correctIndex,
                    isSelected = i == ui.selected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(i)
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // নিচ থেকে উঠে আসা ফিডব্যাক বার
        AnimatedVisibility(
            visible = ui.answered,
            enter = slideInVertically(tween(280)) { it } + fadeIn(tween(200)),
            exit = ExitTransition.None
        ) {
            val correct = ui.selected == q.correctIndex
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(if (correct) cs.tertiaryContainer else cs.errorContainer)
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp)
            ) {
                Text(
                    if (correct) "✓ সঠিক!  +${Gamification.XP_CORRECT} XP" else "✕ ভুল হয়েছে",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (correct) cs.onTertiaryContainer else cs.onErrorContainer
                )
                if (!correct) {
                    Text(
                        "সঠিক উত্তর: ${q.options[q.correctIndex]}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onErrorContainer
                    )
                }
                Spacer(Modifier.height(14.dp))
                ActionButton(
                    text = if (ui.isLast) "ফলাফল দেখুন" else "পরবর্তী",
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    container = if (correct) cs.tertiary else cs.error,
                    content = if (correct) cs.onTertiary else cs.onError
                )
            }
        }
    }
}

@Composable
private fun OptionCard(
    letter: String,
    text: String,
    sp: Int,
    answered: Boolean,
    isCorrect: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val container by animateColorAsState(
        when {
            answered && isCorrect -> cs.tertiaryContainer
            answered && isSelected -> cs.errorContainer
            else -> cs.surfaceContainerLow
        },
        tween(250), label = "optBg"
    )
    val border by animateColorAsState(
        when {
            answered && isCorrect -> cs.tertiary
            answered && isSelected -> cs.error
            else -> cs.outlineVariant.copy(alpha = 0.6f)
        },
        tween(250), label = "optBorder"
    )
    val content = when {
        answered && isCorrect -> cs.onTertiaryContainer
        answered && isSelected -> cs.onErrorContainer
        else -> cs.onSurface
    }

    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        color = container,
        borderColor = border,
        shape = RoundedCornerShape(20.dp),
        onClick = if (!answered) onClick else null
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp)) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(content.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(letter, style = MaterialTheme.typography.labelLarge, color = content)
            }
            BigText(
                text, sp.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                color = content, textAlign = TextAlign.Center
            )
            if (answered && (isCorrect || isSelected)) {
                Text(
                    if (isCorrect) "✓" else "✕",
                    modifier = Modifier.align(Alignment.CenterEnd),
                    style = MaterialTheme.typography.titleLarge,
                    color = content
                )
            }
        }
    }
}

@Composable
fun QuizResult(ui: QuizUi, onRetry: () -> Unit, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val total = ui.questions.size

    if (total == 0) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyState("🤔", "প্রশ্ন তৈরি করা যায়নি", "অন্য বিভাগ বা সেটিংস বেছে নিয়ে আবার চেষ্টা করুন।")
            Spacer(Modifier.height(8.dp))
            ActionButton("ফিরে যান", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
        return
    }

    val pct = ui.score * 100 / total
    val (emoji, message) = when {
        pct == 100 -> "🏆" to "অসাধারণ! সবগুলো সঠিক!"
        pct >= 70 -> "🎉" to "দারুণ করেছেন!"
        pct >= 40 -> "👍" to "ভালো চেষ্টা — আরেকবার খেলে দেখুন"
        else -> "💪" to "অনুশীলন চালিয়ে যান, পারবেন!"
    }
    val ringColor = if (pct >= 70) cs.tertiary else if (pct >= 40) cs.primary else cs.secondary

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .entrance(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ProgressRing(
            progress = ui.score.toFloat() / total,
            diameter = 190.dp,
            strokeWidth = 16.dp,
            color = ringColor
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(emoji, fontSize = 36.sp)
                BigText("${ui.score} / $total", 30.sp, color = cs.onSurface, weight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(message, style = MaterialTheme.typography.titleLarge, color = cs.onSurface, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        StatPill("⭐ এই কুইজে অর্জন: +${ui.xpEarned} XP", cs.primaryContainer, cs.onPrimaryContainer)
        if (pct == 100) {
            Spacer(Modifier.height(8.dp))
            Text(
                "পারফেক্ট বোনাস +${Gamification.XP_PERFECT_BONUS} XP যোগ হয়েছে",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(28.dp))
        ActionButton("আবার খেলুন", onClick = onRetry, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        ActionButton(
            "সেটিংসে ফিরুন", onClick = onBack, modifier = Modifier.fillMaxWidth(),
            container = cs.surfaceContainerHigh, content = cs.onSurface
        )
    }
}
