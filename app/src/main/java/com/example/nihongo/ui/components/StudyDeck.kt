package com.example.nihongo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.launch

/**
 * ফ্ল্যাশকার্ড ও শব্দ-প্র্যাকটিস — দুই জায়গাতেই এই একই ডেক।
 *  • কার্ডে ট্যাপ করলে ৩D ফ্লিপ হয়ে উত্তর দেখায়
 *  • উত্তর দেখার পর ডানে সোয়াইপ = "জানি", বামে সোয়াইপ = "জানি না" (বাটনও আছে)
 *  • নতুন কার্ড নরমভাবে ভেসে আসে
 */
@Composable
fun StudyDeck(
    index: Int,
    total: Int,
    flipped: Boolean,
    onFlip: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    front: @Composable ColumnScope.() -> Unit,
    back: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val currentOnAnswer by rememberUpdatedState(onAnswer)

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
    ) {
        // উপরে প্রগ্রেস
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppProgressBar(
                progress = index.toFloat() / total.coerceAtLeast(1),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "${index + 1} / $total",
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(16.dp))

        // প্রতিটি কার্ডের নিজস্ব অ্যানিমেশন স্টেট (index বদলালে নতুন করে শুরু হয়)
        key(index) {
            val scope = rememberCoroutineScope()
            val offsetX = remember { Animatable(0f) }
            val enter = remember { Animatable(0f) }
            var busy by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { enter.animateTo(1f, tween(320, easing = FastOutSlowInEasing)) }

            val rotation by animateFloatAsState(
                targetValue = if (flipped) 180f else 0f,
                animationSpec = tween(480, easing = FastOutSlowInEasing),
                label = "flip"
            )
            val showBack = rotation > 90f

            // কার্ড উড়ে গিয়ে তারপর উত্তর জমা হয়
            val dismiss: (Boolean) -> Unit = { known ->
                if (!busy) {
                    busy = true
                    scope.launch {
                        offsetX.animateTo(if (known) 1100f else -1100f, tween(230))
                        currentOnAnswer(known)
                    }
                }
            }

            val shape = RoundedCornerShape(28.dp)
            val frontBrush = Brush.linearGradient(listOf(cs.primaryContainer, cs.surfaceContainerHigh))
            val backBrush = Brush.linearGradient(
                listOf(cs.surfaceContainerLowest, cs.secondaryContainer.copy(alpha = 0.55f))
            )
            val tint = if (offsetX.value > 0f) cs.tertiary else cs.error
            val tintAlpha = (abs(offsetX.value) / 600f).coerceIn(0f, 0.4f)

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationX = offsetX.value
                        rotationZ = offsetX.value / 45f
                        alpha = enter.value
                        val s = 0.94f + 0.06f * enter.value
                        scaleX = s
                        scaleY = s
                    }
                    .pointerInput(flipped) {
                        if (flipped) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    val x = offsetX.value
                                    when {
                                        x > 220f -> dismiss(true)
                                        x < -220f -> dismiss(false)
                                        else -> scope.launch { offsetX.animateTo(0f, spring()) }
                                    }
                                },
                                onDragCancel = { scope.launch { offsetX.animateTo(0f, spring()) } },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                                }
                            )
                        }
                    }
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 14f * density
                        }
                        .shadow(6.dp, shape)
                        .clip(shape)
                        .background(if (showBack) backBrush else frontBrush)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !flipped
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFlip()
                        }
                ) {
                    // সোয়াইপের সময় সবুজ/লাল আভা
                    Box(Modifier.fillMaxSize().background(tint.copy(alpha = tintAlpha)))
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .graphicsLayer { rotationY = if (showBack) 180f else 0f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (showBack) back() else front()
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // নিচে উত্তরের বাটন / ইঙ্গিত
            Box(Modifier.fillMaxWidth().height(56.dp)) {
                AnimatedVisibility(
                    visible = flipped,
                    enter = fadeIn(tween(250)),
                    exit = fadeOut(tween(100))
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionButton(
                            text = "←  জানি না",
                            onClick = { dismiss(false) },
                            modifier = Modifier.weight(1f),
                            container = cs.errorContainer,
                            content = cs.onErrorContainer,
                            enabled = !busy
                        )
                        ActionButton(
                            text = "জানি  →",
                            onClick = { dismiss(true) },
                            modifier = Modifier.weight(1f),
                            container = cs.tertiary,
                            content = cs.onTertiary,
                            enabled = !busy
                        )
                    }
                }
                AnimatedVisibility(
                    visible = !flipped,
                    modifier = Modifier.align(Alignment.Center),
                    enter = fadeIn(tween(250)),
                    exit = fadeOut(tween(100))
                ) {
                    Text(
                        "কার্ডে ট্যাপ করে উত্তর দেখুন",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
