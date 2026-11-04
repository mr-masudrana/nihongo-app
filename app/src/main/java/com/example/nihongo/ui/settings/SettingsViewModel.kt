package com.example.nihongo.ui.settings

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nihongo.data.AppSettings
import com.example.nihongo.data.PROFILE_PHOTO
import com.example.nihongo.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository.get(app)
    val settings: StateFlow<AppSettings> = repo.settings

    fun setDailyGoal(v: Int) = repo.setDailyGoal(v)
    fun setThemeMode(v: Int) = repo.setThemeMode(v)
    fun setTtsRate(v: Float) = repo.setTtsRate(v)
    fun setUserName(v: String) = repo.setUserName(v.trim().take(24))

    private fun photoFile() = File(getApplication<Application>().filesDir, PROFILE_PHOTO)

    fun hasPhoto(): Boolean = photoFile().exists()

    /** বাছাই করা ছবি ছোট (৫১২px, বর্গাকার) করে অ্যাপের ভেতরে রাখে — কোনো পারমিশন লাগে না */
    fun setPhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val cr = getApplication<Application>().contentResolver
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                val src = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                if (src != null) {
                    val orientation = runCatching {
                        cr.openInputStream(uri)?.use {
                            ExifInterface(it).getAttributeInt(
                                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                            )
                        }
                    }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL
                    val degrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                    val upright = if (degrees != 0f) {
                        Bitmap.createBitmap(src, 0, 0, src.width, src.height, Matrix().apply { postRotate(degrees) }, true)
                    } else src
                    val side = minOf(upright.width, upright.height)
                    val square = Bitmap.createBitmap(
                        upright, (upright.width - side) / 2, (upright.height - side) / 2, side, side
                    )
                    val out = Bitmap.createScaledBitmap(square, 512, 512, true)
                    FileOutputStream(photoFile()).use { out.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                    repo.bumpPhoto()
                }
            }
        }
    }

    fun removePhoto() {
        photoFile().delete()
        repo.bumpPhoto()
    }
}
