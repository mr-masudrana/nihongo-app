package com.example.nihongo.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class KanjiItem(
    val kanji: String, val onyomi: String, val kunyomi: String,
    val bn: String, val en: String,
    val example: String, val exampleReading: String, val exampleBn: String
)

data class GrammarExample(val jp: String, val romaji: String, val bn: String)

data class GrammarItem(
    val title: String, val pattern: String, val explain: String,
    val examples: List<GrammarExample>
)

data class WordItem(
    val word: String, val reading: String, val romaji: String,
    val bn: String, val en: String
)

/** একটি লেসনের সব কনটেন্ট */
data class Lesson(
    val no: Int,
    val title: String,
    val vocab: List<WordItem>,
    val grammar: List<GrammarItem>,
    val kanji: List<KanjiItem>
)

/** লেসন তালিকার জন্য সারাংশ। exists = false মানে ফাইলটি এখনো যোগ হয়নি */
data class LessonInfo(
    val no: Int, val title: String,
    val vocab: Int, val grammar: Int, val kanji: Int,
    val exists: Boolean
)

/** সার্চ ও কুইজের জন্য: কোন লেভেল/লেসনের শব্দ */
data class LessonWord(val level: String, val lessonNo: Int, val word: WordItem)

/**
 * লেসন-ভিত্তিক কনটেন্ট — assets/lessons/<level>/lesson_NN.json (level = n5 | n4)।
 * নতুন লেসন যোগ করতে শুধু ফাইলটি ওই ফোল্ডারে রাখুন; কোড বা ডেটাবেজ বদলাতে হবে না।
 * Room ব্যবহার করে না, তাই ডেটাবেজ রিসেটে প্রভাব নেই।
 */
class ContentRepository private constructor(private val context: Context) {

    fun lessonRange(level: String): IntRange = if (level == "n5") 1..25 else 26..50

    private fun fileName(level: String, no: Int) = "lessons/$level/lesson_${"%02d".format(no)}.json"

    fun lesson(level: String, no: Int): Lesson? = runCatching {
        val text = context.assets.open(fileName(level, no)).bufferedReader().use { it.readText() }
        parse(JSONObject(text), no)
    }.getOrNull()

    fun lessonInfos(level: String): List<LessonInfo> = lessonRange(level).map { no ->
        val l = lesson(level, no)
        if (l == null) LessonInfo(no, "", 0, 0, 0, false)
        else LessonInfo(no, l.title, l.vocab.size, l.grammar.size, l.kanji.size, true)
    }

    /** সব লেসনের সব শব্দ (N5 ও N4) */
    fun allWords(): List<LessonWord> = listOf("n5", "n4").flatMap { level ->
        lessonRange(level).flatMap { no ->
            lesson(level, no)?.vocab.orEmpty().map { LessonWord(level, no, it) }
        }
    }

    private fun parse(o: JSONObject, fallbackNo: Int) = Lesson(
        no = o.optInt("lesson", fallbackNo),
        title = o.optString("title", ""),
        vocab = list(o.optJSONArray("vocab")) { w ->
            WordItem(
                w.optString("word"), w.optString("reading"), w.optString("romaji"),
                w.optString("bn"), w.optString("en")
            )
        },
        grammar = list(o.optJSONArray("grammar")) { g ->
            GrammarItem(
                g.optString("title"), g.optString("pattern"), g.optString("explain"),
                list(g.optJSONArray("examples")) { e ->
                    GrammarExample(e.optString("jp"), e.optString("romaji"), e.optString("bn"))
                }
            )
        },
        kanji = list(o.optJSONArray("kanji")) { k ->
            KanjiItem(
                k.optString("kanji"), k.optString("on"), k.optString("kun"),
                k.optString("bn"), k.optString("en"),
                k.optString("ex"), k.optString("exReading"), k.optString("exBn")
            )
        }
    )

    private fun <T> list(a: JSONArray?, f: (JSONObject) -> T): List<T> =
        if (a == null) emptyList() else (0 until a.length()).map { f(a.getJSONObject(it)) }

    companion object {
        @Volatile private var INSTANCE: ContentRepository? = null

        fun get(context: Context): ContentRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContentRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
