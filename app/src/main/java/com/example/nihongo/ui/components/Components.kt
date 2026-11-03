package com.example.nihongo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.ui.kana.TtsHelper

/** পুরো অ্যাপে একটাই TextToSpeech ইঞ্জিন — প্রতি স্ক্রিনে নতুন করে চালু হয় না, তাই দ্রুত */
val LocalTts = staticCompositionLocalOf<TtsHelper> { error("TtsHelper not provided") }

// ---------------------------------------------------------------- মডিফায়ার

/** চাপলে হালকা ছোট হয়ে আবার ফিরে আসে (স্প্রিং অ্যানিমেশন) */
fun Modifier.bounceClick(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}

/** প্রথমবার দেখানোর সময় নরমভাবে ফেড + স্কেল হয়ে আসে */
fun Modifier.entrance(): Modifier = composed {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
    this.graphicsLayer {
        alpha = a.value
        val s = 0.92f + 0.08f * a.value
        scaleX = s
        scaleY = s
    }
}

// ---------------------------------------------------------------- টেক্সট

/** বড় জাপানি/বাংলা লেখার জন্য — lineHeight ফন্টের সাথে মিলিয়ে নেয়, যাতে কাটা না পড়ে */
@Composable
fun BigText(
    text: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    weight: FontWeight? = null,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        textAlign = textAlign,
        lineHeight = size * 1.3f
    )
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

// ---------------------------------------------------------------- কার্ড ও বাটন

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .then(if (onClick != null) Modifier.bounceClick(onClick = onClick) else Modifier)
            .clip(shape)
            .background(color)
            .border(1.dp, borderColor, shape),
        content = content
    )
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .bounceClick(enabled = enabled, onClick = onClick)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) container else container.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = content, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SpeakButton(onClick: () -> Unit, modifier: Modifier = Modifier, diameter: Dp = 44.dp) {
    Box(
        modifier = modifier
            .size(diameter)
            .bounceClick(onClick = onClick)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text("🔊", fontSize = (diameter.value * 0.45f).sp)
    }
}

@Composable
fun StatPill(
    text: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = content, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

// ---------------------------------------------------------------- সিলেক্টর

/** iOS-ধাঁচের সেগমেন্টেড সুইচ (যেমন হিরাগানা | কাতাকানা) */
@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cs.surfaceContainerHigh)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            val bg by animateColorAsState(
                if (selected) cs.primary else Color.Transparent, tween(220), label = "segBg"
            )
            val fg by animateColorAsState(
                if (selected) cs.onPrimary else cs.onSurfaceVariant, tween(220), label = "segFg"
            )
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = fg, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
fun AppChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) cs.primary else cs.surfaceContainerLow, tween(200), label = "chipBg")
    val fg by animateColorAsState(if (selected) cs.onPrimary else cs.onSurfaceVariant, tween(200), label = "chipFg")
    val border by animateColorAsState(if (selected) Color.Transparent else cs.outlineVariant, tween(200), label = "chipBorder")
    Box(
        Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = fg, style = MaterialTheme.typography.labelLarge)
    }
}

/** অনুভূমিকভাবে স্ক্রল করা যায় এমন ফিল্টার চিপ সারি */
@Composable
fun ChipRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp)
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { (key, label) ->
            AppChip(label = label, selected = key == selected, onClick = { onSelect(key) })
        }
    }
}

// ---------------------------------------------------------------- প্রগ্রেস

@Composable
fun AppProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(450, easing = FastOutSlowInEasing), label = "bar")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(progress) { anim.animateTo(progress.coerceIn(0f, 1f), tween(900, easing = FastOutSlowInEasing)) }
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke)
            )
            if (anim.value > 0f) {
                drawArc(
                    color = color, startAngle = -90f, sweepAngle = 360f * anim.value, useCenter = false,
                    topLeft = topLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

// ---------------------------------------------------------------- খালি / শেষ অবস্থা

@Composable
fun EmptyState(emoji: String, title: String, message: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxWidth().padding(32.dp).entrance(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(96.dp).clip(CircleShape).background(cs.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 44.sp) }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = cs.onSurface, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun SessionDone(known: Int, again: Int, onRestart: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp).entrance(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(104.dp).clip(CircleShape).background(cs.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) { Text("🎉", fontSize = 48.sp) }
        Spacer(Modifier.height(20.dp))
        Text("সেশন শেষ!", style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill("✓ জানি: $known", cs.tertiaryContainer, cs.onTertiaryContainer)
            StatPill("↻ জানি না: $again", cs.errorContainer, cs.onErrorContainer)
        }
        Spacer(Modifier.height(28.dp))
        ActionButton("আবার শুরু করুন", onClick = onRestart, modifier = Modifier.fillMaxWidth())
    }
}
