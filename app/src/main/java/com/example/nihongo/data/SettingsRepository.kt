package com.example.nihongo.data

import android.content.Context
import com.example.nihongo.domain.Gamification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** প্রোফাইল ছবির ফাইলের নাম (filesDir-এ থাকে) */
const val PROFILE_PHOTO = "profile.jpg"

/** অ্যাপের ভাষা: SYSTEM = ফোনের ভাষা (বাংলা হলে বাংলা, নইলে ইংরেজি) */
enum class AppLang { SYSTEM, BN, EN }

fun isEnglishUi(lang: AppLang): Boolean = when (lang) {
    AppLang.BN -> false
    AppLang.EN -> true
    AppLang.SYSTEM -> java.util.Locale.getDefault().language != "bn"
}

/** শব্দ কার্ডের যে অংশগুলো দেখানো/লুকানো যায় */
enum class WordField(val label: String, val labelEn: String, val hint: String, val hintEn: String) {
    WORD("কাঞ্জি / লেখা", "Kanji / writing", "শব্দের মূল লেখার রূপ", "The word as written"),
    READING("কানা পড়া", "Kana reading", "হিরাগানা / কাতাকানায় উচ্চারণ", "Pronunciation in hiragana / katakana"),
    ROMAJI("রোমাজি", "Romaji", "ইংরেজি হরফে উচ্চারণ", "Pronunciation in Latin letters"),
    BN("বাংলা অর্থ", "Bengali meaning", "বাংলা অর্থ ও ব্যবহারের টীকা", "Bengali meaning and usage notes"),
    EN("ইংরেজি অর্থ", "English meaning", "ইংরেজি অর্থ", "English meaning")
}

/** themeMode: 0 = সিস্টেম অনুযায়ী, 1 = লাইট, 2 = ডার্ক */
data class AppSettings(
    val dailyGoal: Int = Gamification.DAILY_GOAL,
    val themeMode: Int = 0,
    val ttsRate: Float = 1.0f,
    val userName: String = "",
    val lang: AppLang = AppLang.SYSTEM,
    val audioOn: Boolean = true,
    val photoVersion: Long = 0L,  // ছবি বদলালে বাড়ে — UI নতুন করে লোড করে
    /** শব্দ কার্ডে যেগুলো দেখানো হবে (কমপক্ষে একটি সবসময় চালু থাকে) */
    val wordShow: Set<WordField> = WordField.values().toSet()
) {
    fun shows(f: WordField) = f in wordShow
}

/** সেটিংস SharedPreferences-এ থাকে (Room রিসেট হলেও মুছবে না) */
class SettingsRepository private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun load() = AppSettings(
        dailyGoal = prefs.getInt("dailyGoal", Gamification.DAILY_GOAL),
        themeMode = prefs.getInt("themeMode", 0),
        ttsRate = prefs.getFloat("ttsRate", 1.0f),
        userName = prefs.getString("userName", "").orEmpty(),
        lang = runCatching { AppLang.valueOf(prefs.getString("lang", "SYSTEM").orEmpty()) }.getOrDefault(AppLang.SYSTEM),
        audioOn = prefs.getBoolean("audioOn", true),
        photoVersion = prefs.getLong("photoVersion", 0L),
        wordShow = WordField.values().filter { prefs.getBoolean("show_${it.name}", true) }.toSet()
            .ifEmpty { WordField.values().toSet() }
    )

    @Synchronized
    fun setDailyGoal(v: Int) {
        prefs.edit().putInt("dailyGoal", v).apply()
        _settings.value = _settings.value.copy(dailyGoal = v)
    }

    @Synchronized
    fun setThemeMode(v: Int) {
        prefs.edit().putInt("themeMode", v).apply()
        _settings.value = _settings.value.copy(themeMode = v)
    }

    @Synchronized
    fun setTtsRate(v: Float) {
        prefs.edit().putFloat("ttsRate", v).apply()
        _settings.value = _settings.value.copy(ttsRate = v)
    }

    @Synchronized
    fun setLang(v: AppLang) {
        prefs.edit().putString("lang", v.name).apply()
        _settings.value = _settings.value.copy(lang = v)
    }

    @Synchronized
    fun setAudioOn(v: Boolean) {
        prefs.edit().putBoolean("audioOn", v).apply()
        _settings.value = _settings.value.copy(audioOn = v)
    }

    @Synchronized
    fun setUserName(v: String) {
        prefs.edit().putString("userName", v).apply()
        _settings.value = _settings.value.copy(userName = v)
    }

    /** শব্দ কার্ডের একটি অংশ দেখানো/লুকানো। সব লুকানো যায় না — শেষ চালুটি বন্ধ করা যাবে না */
    @Synchronized
    fun setWordField(f: WordField, show: Boolean) {
        val cur = _settings.value.wordShow
        val next = if (show) cur + f else cur - f
        if (next.isEmpty()) return
        prefs.edit().putBoolean("show_${f.name}", show).apply()
        _settings.value = _settings.value.copy(wordShow = next)
    }

    @Synchronized
    fun bumpPhoto() {
        val v = System.currentTimeMillis()
        prefs.edit().putLong("photoVersion", v).apply()
        _settings.value = _settings.value.copy(photoVersion = v)
    }

    companion object {
        @Volatile private var INSTANCE: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
