package com.example.nihongo.domain

import com.example.nihongo.data.KanaEntity
import kotlin.random.Random

data class QuizQuestion(
    val prompt: String,
    val hint: String?,
    val speak: String?,        // উত্তর দেওয়ার পর যা বাজবে
    val options: List<String>,
    val correctIndex: Int,
    val promptSp: Int,
    val optionSp: Int
)

/** শব্দ কুইজের জন্য লেসনের শব্দ। category = "<level>_<লেসন নম্বর>", যেমন n5_1 */
data class QuizWord(
    val id: Int,
    val word: String,
    val reading: String,
    val romaji: String,
    val meaningBn: String,
    val meaningEn: String,
    val category: String
)

enum class KanaDir { KANA_TO_ROMAJI, ROMAJI_TO_KANA }
enum class VocabDir { WORD_TO_MEANING, MEANING_TO_WORD }

object QuizBuilder {
    const val DEFAULT_COUNT = 10

    // ---------------- কানা কুইজ ----------------
    fun kanaQuestions(
        all: List<KanaEntity>,
        scripts: Set<String>,
        dir: KanaDir,
        count: Int = DEFAULT_COUNT,
        rnd: Random = Random.Default
    ): List<QuizQuestion> {
        val inScripts = all.filter { it.type in scripts }
        val pool = if (dir == KanaDir.ROMAJI_TO_KANA) {
            // একই রোমাজির দুটি কানা (যেমন じ/ぢ = ji) থাকলে রোমাজি→কানা প্রশ্নে অস্পষ্টতা হয়, তাই বাদ
            val counts = all.groupingBy { it.type to it.romaji }.eachCount()
            inScripts.filter { counts[it.type to it.romaji] == 1 }
        } else inScripts

        return pool.shuffled(rnd).take(count).map { k ->
            // একই স্ক্রিপ্টের কানা থেকে ভুল অপশন; একই গ্রুপের গুলো আগে (বেশি বিভ্রান্তিকর)
            val others = all
                .filter { it.type == k.type && it.romaji != k.romaji }
                .shuffled(rnd)
                .sortedByDescending { it.grp == k.grp }
            when (dir) {
                KanaDir.KANA_TO_ROMAJI -> build(
                    prompt = k.symbol, hint = null, speak = k.symbol,
                    correct = k.romaji,
                    distractors = others.map { it.romaji }.distinct().take(3),
                    promptSp = 72, optionSp = 22, rnd = rnd
                )
                KanaDir.ROMAJI_TO_KANA -> build(
                    prompt = k.romaji, hint = k.bn, speak = k.symbol,
                    correct = k.symbol,
                    distractors = others.map { it.symbol }.distinct().take(3),
                    promptSp = 48, optionSp = 36, rnd = rnd
                )
            }
        }
    }

    // ---------------- শব্দ কুইজ ----------------
    fun vocabQuestions(
        all: List<QuizWord>,
        category: String,
        dir: VocabDir,
        count: Int = DEFAULT_COUNT,
        rnd: Random = Random.Default,
        english: Boolean = false   // true হলে অর্থ ইংরেজিতে (ইংরেজি থাকলে), নইলে বাংলায়
    ): List<QuizQuestion> {
        fun meaning(w: QuizWord) = if (english && w.meaningEn.isNotBlank()) w.meaningEn else w.meaningBn
        fun other(w: QuizWord) = if (english && w.meaningEn.isNotBlank()) w.meaningBn else w.meaningEn
        val pool = all.filter { category == "all" || it.category == category }
        return pool.shuffled(rnd).take(count).map { v ->
            val others = all
                .filter { it.id != v.id && !similarMeaning(meaning(it), meaning(v)) }
                .shuffled(rnd)
                .sortedByDescending { it.category == v.category }
            when (dir) {
                VocabDir.WORD_TO_MEANING -> build(
                    prompt = v.word,
                    hint = if (v.reading != v.word) "${v.reading} · ${v.romaji}" else v.romaji,
                    speak = v.reading,
                    correct = meaning(v),
                    distractors = others.map { meaning(it) }.distinct().take(3),
                    promptSp = 44, optionSp = 18, rnd = rnd
                )
                VocabDir.MEANING_TO_WORD -> build(
                    prompt = meaning(v), hint = other(v), speak = v.reading,
                    correct = label(v),
                    distractors = others.map { label(it) }.distinct().take(3),
                    promptSp = 26, optionSp = 20, rnd = rnd
                )
            }
        }
    }

    private fun label(v: QuizWord) = if (v.reading != v.word) "${v.word} (${v.reading})" else v.word

    private fun build(
        prompt: String, hint: String?, speak: String?, correct: String,
        distractors: List<String>, promptSp: Int, optionSp: Int, rnd: Random
    ): QuizQuestion {
        val options = (distractors + correct).shuffled(rnd)
        return QuizQuestion(prompt, hint, speak, options, options.indexOf(correct), promptSp, optionSp)
    }

    // ---- প্রায় একই অর্থের বাংলা (যেমন "ছোট" বনাম "ছোট (দৈর্ঘ্যে)") ভুল-অপশন হিসেবে আসা আটকায় ----
    private val parens = Regex("\\(.*?\\)")

    private fun tokens(s: String): List<String> =
        s.replace(parens, "").substringBefore("—").split("/").map { it.trim() }.filter { it.isNotEmpty() }

    private fun similarMeaning(a: String, b: String): Boolean {
        val ta = tokens(a)
        val tb = tokens(b)
        return ta.any { x -> tb.any { y -> x == y || x.contains(y) || y.contains(x) } }
    }
}
