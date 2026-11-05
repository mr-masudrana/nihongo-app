package com.example.nihongo.ui.quiz

import com.example.nihongo.ui.components.LocalEnglish
import com.example.nihongo.ui.components.tr
import androidx.compose.animation.AnimatedVisibility
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.AppIcon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import android.os.SystemClock
import kotlinx.coroutines.delay
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

private val optionLettersBn = listOf("ক", "খ", "গ", "ঘ", "ঙ")
private val optionLettersEn = listOf("A", "B", "C", "D", "E")

@Composable
fun QuizPlay(
    ui: QuizUi,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    val q = ui.current ?: return
    val cs = MaterialTheme.colorScheme
    val tts = LocalTts.current
    val haptic = LocalHapticFeedback.current

    // স্টপওয়াচ: কুইজ শুরু থেকে চলে, ভুল উত্তরে থামে
    var nowMs by remember { mutableStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(ui.over) {
        while (!ui.over) {
            nowMs = SystemClock.elapsedRealtime()
            delay(250)
        }
    }
    val elapsed = (if (ui.finishedAt > 0L) ui.finishedAt else nowMs) - ui.startedAt

    // উত্তর দেওয়ার পর সঠিক উচ্চারণ বাজাও
    LaunchedEffect(q, ui.selected) {
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
                StatPill(tr("প্রশ্ন ${ui.index + 1}", "Question ${ui.index + 1}"), cs.primaryContainer, cs.onPrimaryContainer)
                Spacer(Modifier.weight(1f))
                StatPill(tr("সময় ${formatTime(elapsed)}", "Time ${formatTime(elapsed)}"), cs.secondaryContainer, cs.onSecondaryContainer)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatPill(tr("স্কোর: ${ui.score}", "Score: ${ui.score}"), cs.tertiaryContainer, cs.onTertiaryContainer, icon = AppIcons.Check)
                Spacer(Modifier.weight(1f))
                StatPill(
                    tr("হাই স্কোর: ${maxOf(ui.highScore, ui.score)}", "High score: ${maxOf(ui.highScore, ui.score)}"),
                    cs.tertiaryContainer, cs.onTertiaryContainer, icon = AppIcons.Trophy
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
                    letter = (if (LocalEnglish.current) optionLettersEn else optionLettersBn).getOrElse(i) { "•" },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(
                        if (correct) AppIcons.Check else AppIcons.Close, size = 24.dp,
                        tint = if (correct) cs.onTertiaryContainer else cs.onErrorContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (correct) tr("সঠিক!", "Correct!") else tr("ভুল হয়েছে", "Wrong"),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (correct) cs.onTertiaryContainer else cs.onErrorContainer
                    )
                }
                if (!correct) {
                    Text(
                        tr("সঠিক উত্তর: ${q.options[q.correctIndex]}", "Correct answer: ${q.options[q.correctIndex]}"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onErrorContainer
                    )
                }
                Spacer(Modifier.height(14.dp))
                ActionButton(
                    text = if (ui.finishing) tr("ফলাফল দেখুন", "See result") else tr("পরবর্তী", "Next"),
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
                AppIcon(
                    if (isCorrect) AppIcons.Check else AppIcons.Close, size = 24.dp, tint = content,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp)
                )
            }
        }
    }
}

@Composable
fun QuizResult(ui: QuizUi, onRetry: () -> Unit, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme

    if (ui.empty) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyState(AppIcons.Help, tr("প্রশ্ন তৈরি করা যায়নি", "Couldn't create questions"), tr("অন্য বিভাগ বা সেটিংস বেছে নিয়ে আবার চেষ্টা করুন।", "Pick another category or setting and try again."))
            Spacer(Modifier.height(8.dp))
            ActionButton(tr("ফিরে যান", "Go back"), onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
        return
    }

    val ringColor = if (ui.newRecord) cs.tertiary else cs.primary
    val message = when {
        ui.newRecord -> tr("নতুন হাই স্কোর!", "New high score!")
        ui.score == 0 -> tr("এবার হলো না — আবার চেষ্টা করুন", "Not this time — try again")
        else -> tr("ভালো খেলেছেন!", "Well played!")
    }

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
            progress = if (ui.highScore > 0) (ui.score.toFloat() / ui.highScore).coerceAtMost(1f) else 0f,
            diameter = 190.dp,
            strokeWidth = 16.dp,
            color = ringColor
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppIcon(if (ui.newRecord) AppIcons.Trophy else AppIcons.Star, size = 40.dp, tint = ringColor)
                BigText("${ui.score}", 40.sp, color = cs.onSurface, weight = FontWeight.Bold)
                Text(tr("টানা সঠিক", "In a row"), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(message, style = MaterialTheme.typography.titleLarge, color = cs.onSurface, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        StatPill(tr("হাই স্কোর: ${ui.highScore}", "High score: ${ui.highScore}"), cs.tertiaryContainer, cs.onTertiaryContainer, icon = AppIcons.Trophy)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill(tr("সময়: ${formatTime(ui.totalMs)}", "Time: ${formatTime(ui.totalMs)}"), cs.secondaryContainer, cs.onSecondaryContainer)
            StatPill(tr("প্রশ্ন: ${ui.answeredCount}", "Questions: ${ui.answeredCount}"), cs.primaryContainer, cs.onPrimaryContainer)
        }
        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.height(28.dp))
        ActionButton(tr("আবার শুরু করুন", "Start again"), onClick = onRetry, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        ActionButton(
            tr("সেটিংসে ফিরুন", "Back to settings"), onClick = onBack, modifier = Modifier.fillMaxWidth(),
            container = cs.surfaceContainerHigh, content = cs.onSurface
        )
    }
}

/** মিলিসেকেন্ড → mm:ss */
private fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(total / 60, total % 60)
}
