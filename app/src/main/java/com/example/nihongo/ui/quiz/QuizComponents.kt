package com.example.nihongo.ui.quiz

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
fun QuizPlay(
    ui: QuizUi,
    onSelect: (Int, Float) -> Unit,
    onTimeout: () -> Unit,
    onNext: () -> Unit
) {
    val q = ui.current ?: return
    val cs = MaterialTheme.colorScheme
    val tts = LocalTts.current
    val haptic = LocalHapticFeedback.current

    // চ্যালেঞ্জ মোডের কাউন্টডাউন: প্রতি প্রশ্নে ১৫ সেকেন্ড; উত্তর দিলে থেমে যায়
    val totalMs = Gamification.CHALLENGE_SECONDS * 1000L
    val timed = ui.mode == QuizMode.CHALLENGE
    var leftMs by remember(q) { mutableStateOf(totalMs) }
    val onTimeoutNow by rememberUpdatedState(onTimeout)
    LaunchedEffect(q, ui.answered) {
        if (!timed || ui.answered) return@LaunchedEffect
        val start = withFrameNanos { it }
        while (leftMs > 0) {
            withFrameNanos { now ->
                val left = (totalMs - (now - start) / 1_000_000L).coerceAtLeast(0L)
                if (leftMs - left >= 50L || left == 0L) leftMs = left
            }
        }
        onTimeoutNow()
    }

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
            if (timed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatPill("প্রশ্ন ${ui.index + 1} / ${ui.total}", cs.primaryContainer, cs.onPrimaryContainer)
                    Spacer(Modifier.weight(1f))
                    StatPill("সঠিক: ${ui.score}", cs.tertiaryContainer, cs.onTertiaryContainer, icon = AppIcons.Check)
                }
                val fraction = leftMs.toFloat() / totalMs
                val urgent = leftMs <= 5_000L && !ui.answered
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppProgressBar(
                        progress = fraction,
                        modifier = Modifier.weight(1f),
                        color = if (urgent) cs.error else cs.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${(leftMs + 999L) / 1000L}s",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (urgent) cs.error else cs.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatPill("স্কোর: ${ui.score}", cs.primaryContainer, cs.onPrimaryContainer, icon = AppIcons.Check)
                    Spacer(Modifier.weight(1f))
                    StatPill(
                        "হাই স্কোর: ${maxOf(ui.highScore, ui.score)}",
                        cs.tertiaryContainer, cs.onTertiaryContainer, icon = AppIcons.Trophy
                    )
                }
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
                        onSelect(i, leftMs.toFloat() / totalMs)
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
                        if (correct) {
                            if (timed) "সঠিক!  +${ui.lastXp} XP" else "সঠিক!"
                        } else if (ui.timedOut) "সময় শেষ!" else "ভুল হয়েছে",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (correct) cs.onTertiaryContainer else cs.onErrorContainer
                    )
                }
                if (!correct) {
                    Text(
                        "সঠিক উত্তর: ${q.options[q.correctIndex]}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onErrorContainer
                    )
                }
                Spacer(Modifier.height(14.dp))
                ActionButton(
                    text = if (ui.finishing) "ফলাফল দেখুন" else "পরবর্তী",
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
            EmptyState(AppIcons.Help, "প্রশ্ন তৈরি করা যায়নি", "অন্য বিভাগ বা সেটিংস বেছে নিয়ে আবার চেষ্টা করুন।")
            Spacer(Modifier.height(8.dp))
            ActionButton("ফিরে যান", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
        return
    }

    if (ui.mode == QuizMode.CHALLENGE) { ChallengeResult(ui, onRetry, onBack); return }

    val ringColor = if (ui.newRecord) cs.tertiary else cs.primary
    val message = when {
        ui.newRecord -> "নতুন হাই স্কোর!"
        ui.score == 0 -> "এবার হলো না — আবার চেষ্টা করুন"
        else -> "ভালো খেলেছেন!"
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
                Text("টানা সঠিক", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(message, style = MaterialTheme.typography.titleLarge, color = cs.onSurface, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        StatPill("হাই স্কোর: ${ui.highScore}", cs.tertiaryContainer, cs.onTertiaryContainer, icon = AppIcons.Trophy)
        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.height(28.dp))
        ActionButton("আবার শুরু করুন", onClick = onRetry, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        ActionButton(
            "সেটিংসে ফিরুন", onClick = onBack, modifier = Modifier.fillMaxWidth(),
            container = cs.surfaceContainerHigh, content = cs.onSurface
        )
    }
}

@Composable
private fun ChallengeResult(ui: QuizUi, onRetry: () -> Unit, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val total = ui.total
    val pct = if (total > 0) ui.score * 100 / total else 0
    val (resultIcon, message) = when {
        pct == 100 -> AppIcons.Trophy to "অসাধারণ! সবগুলো সঠিক!"
        pct >= 70 -> AppIcons.Star to "দারুণ করেছেন!"
        pct >= 40 -> AppIcons.ThumbUp to "ভালো চেষ্টা — আরেকবার খেলে দেখুন"
        else -> AppIcons.Fire to "অনুশীলন চালিয়ে যান, পারবেন!"
    }
    val ringColor = if (pct >= 70) cs.tertiary else if (pct >= 40) cs.primary else cs.secondary
    val xpNow = ui.xpAtStart + ui.xpEarned
    val level = Gamification.level(xpNow)
    val levelUp = level > Gamification.level(ui.xpAtStart)

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
            progress = if (total > 0) ui.score.toFloat() / total else 0f,
            diameter = 190.dp, strokeWidth = 16.dp, color = ringColor
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppIcon(resultIcon, size = 40.dp, tint = ringColor)
                BigText("${ui.score} / $total", 30.sp, color = cs.onSurface, weight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(message, style = MaterialTheme.typography.titleLarge, color = cs.onSurface, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        StatPill("এই কুইজে অর্জন: +${ui.xpEarned} XP", cs.primaryContainer, cs.onPrimaryContainer, icon = AppIcons.Star)
        if (ui.perfectBonus > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "পারফেক্ট বোনাস +${ui.perfectBonus} XP যোগ হয়েছে",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(20.dp))
        SoftCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (levelUp) "লেভেল আপ! এখন লেভেল $level" else "লেভেল $level",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (levelUp) cs.tertiary else cs.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${xpNow % Gamification.XP_PER_LEVEL} / ${Gamification.XP_PER_LEVEL} XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.onSurfaceVariant
                    )
                }
                AppProgressBar(progress = Gamification.levelProgress(xpNow))
            }
        }
        Spacer(Modifier.height(24.dp))
        ActionButton("আবার খেলুন", onClick = onRetry, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        ActionButton(
            "সেটিংসে ফিরুন", onClick = onBack, modifier = Modifier.fillMaxWidth(),
            container = cs.surfaceContainerHigh, content = cs.onSurface
        )
    }
}
