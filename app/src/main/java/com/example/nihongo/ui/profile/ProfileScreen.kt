package com.example.nihongo.ui.profile

import com.example.nihongo.ui.components.tr
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nihongo.data.PROFILE_PHOTO
import com.example.nihongo.domain.Gamification
import com.example.nihongo.ui.components.AppIcon
import com.example.nihongo.ui.components.AppIcons
import com.example.nihongo.ui.components.AppProgressBar
import com.example.nihongo.ui.components.BigText
import com.example.nihongo.ui.components.SoftCard
import com.example.nihongo.ui.components.bounceClick
import com.example.nihongo.ui.components.entrance
import com.example.nihongo.ui.settings.SettingsViewModel
import com.example.nihongo.ui.stats.StatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** প্রোফাইল ছবি; ছবি না থাকলে নামের প্রথম অক্ষর, নাম না থাকলে ব্যক্তি-আইকন */
@Composable
fun ProfileAvatar(version: Long, name: String, size: Dp, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val bmp by produceState<ImageBitmap?>(null, version) {
        value = withContext(Dispatchers.IO) {
            val f = File(ctx.filesDir, PROFILE_PHOTO)
            if (f.exists()) BitmapFactory.decodeFile(f.path)?.asImageBitmap() else null
        }
    }
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f))
            .border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val b = bmp
        when {
            b != null -> Image(
                bitmap = b, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
            name.isNotBlank() -> BigText(
                name.trim().first().uppercase(), (size.value * 0.42f).sp,
                color = Color.White, weight = FontWeight.Bold
            )
            else -> AppIcon(AppIcons.Person, size = size * 0.55f, tint = Color.White)
        }
    }
}

@Composable
fun ProfileScreen(vm: SettingsViewModel, statsVm: StatsViewModel, onOpenSettings: () -> Unit) {
    val s by vm.settings.collectAsState()
    val st by statsVm.stats.collectAsState()
    val cs = MaterialTheme.colorScheme

    val today = remember(st) { Gamification.dayOf() }
    val streak = Gamification.currentStreak(st, today)
    val hasPhoto = remember(s.photoVersion) { vm.hasPhoto() }
    var editName by rememberSaveable { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.setPhoto(uri)
    }
    val pickPhoto = {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Column(Modifier.fillMaxSize()) {
        // শিরোনাম + উপরে ডানে সেটিংস আইকন
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(tr("প্রোফাইল", "Profile"), style = MaterialTheme.typography.headlineMedium, color = cs.onBackground)
                Text(tr("আপনার অগ্রগতি ও পরিচয়", "Your progress and identity"), style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
            Box(
                Modifier
                    .size(44.dp)
                    .bounceClick(onClick = onOpenSettings)
                    .clip(CircleShape)
                    .background(cs.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) { AppIcon(AppIcons.Settings, size = 22.dp, tint = cs.onSurface) }
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IdentityCard(
                name = s.userName, photoVersion = s.photoVersion, hasPhoto = hasPhoto,
                streak = streak, bestStreak = st.bestStreak, quizzes = st.quizzes,
                onPickPhoto = { pickPhoto() },
                onRemovePhoto = vm::removePhoto,
                onEditName = { editName = true }
            )
            HighScoreCard(st)
        }
    }

    if (editName) {
        NameDialog(
            initial = s.userName,
            onDismiss = { editName = false },
            onSave = { vm.setUserName(it); editName = false }
        )
    }
}

@Composable
private fun IdentityCard(
    name: String, photoVersion: Long, hasPhoto: Boolean,
    streak: Int, bestStreak: Int, quizzes: Int,
    onPickPhoto: () -> Unit, onRemovePhoto: () -> Unit, onEditName: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val brand = Brush.linearGradient(listOf(Color(0xFF3F51B5), Color(0xFF7E57C2), Color(0xFFD86C9A)))
    Column(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .entrance()
            .clip(RoundedCornerShape(28.dp))
            .background(brand)
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.10f), radius = size.width * 0.38f, center = Offset(size.width * 0.98f, -size.height * 0.05f))
                drawCircle(Color.White.copy(alpha = 0.06f), radius = size.width * 0.26f, center = Offset(size.width * 0.05f, size.height * 1.05f))
            }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(96.dp).bounceClick(onClick = onPickPhoto)) {
                ProfileAvatar(photoVersion, name, 96.dp)
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) { AppIcon(AppIcons.Camera, size = 17.dp, tint = cs.primary) }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    Modifier.clickable(onClick = onEditName),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        name.ifBlank { tr("আপনার নাম যোগ করুন", "Add your name") },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = if (name.isBlank()) 0.8f else 1f),
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    AppIcon(AppIcons.Edit, size = 18.dp, tint = Color.White.copy(alpha = 0.9f))
                }
                if (hasPhoto) {
                    Text(
                        tr("ছবি মুছুন", "Remove photo"),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.clickable(onClick = onRemovePhoto)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileStat(AppIcons.Fire, tr("$streak দিন", "$streak days"), tr("স্ট্রিক", "Streak"), Modifier.weight(1f))
            ProfileStat(AppIcons.Trophy, tr("$bestStreak দিন", "$bestStreak days"), tr("সর্বোচ্চ স্ট্রিক", "Best streak"), Modifier.weight(1f))
            ProfileStat(AppIcons.Check, "$quizzes", tr("কুইজ", "Quizzes"), Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProfileStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AppIcon(icon, size = 20.dp, tint = Color.White)
        Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
    }
}

/** তিন সেকশনের হাই স্কোর (টানা সঠিক উত্তর) */
@Composable
private fun HighScoreCard(st: com.example.nihongo.domain.Stats) {
    val cs = MaterialTheme.colorScheme
    val rows = listOf(
        Triple("kana", tr("Basic (কানা)", "Basic (Kana)"), cs.primary),
        Triple("n5", "N5", cs.secondary),
        Triple("n4", "N4", cs.tertiary)
    )
    SoftCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(cs.tertiary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) { AppIcon(AppIcons.Trophy, size = 22.dp, tint = cs.tertiary) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(tr("হাই স্কোর", "High scores"), style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Text(
                        tr("আনলিমিটেড কুইজে সর্বোচ্চ টানা সঠিক উত্তর", "Longest streak of correct answers in the quiz"),
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                    )
                }
            }
            rows.forEach { (kind, label, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(width = 4.dp, height = 20.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(10.dp))
                    Text(label, style = MaterialTheme.typography.titleSmall, color = cs.onSurface, modifier = Modifier.weight(1f))
                    Text("${st.highScore(kind)}", style = MaterialTheme.typography.titleLarge, color = color)
                }
            }
        }
    }
}

@Composable
private fun NameDialog(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("আপনার নাম", "Your name")) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 24) text = it },
                singleLine = true,
                placeholder = { Text(tr("নাম লিখুন", "Enter your name")) },
                shape = RoundedCornerShape(14.dp)
            )
        },
        confirmButton = { TextButton(onClick = { onSave(text.trim()) }) { Text(tr("সংরক্ষণ", "Save")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("বাতিল", "Cancel")) } }
    )
}
