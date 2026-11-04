package com.example.nihongo.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class KanjiWord(val word: String, val reading: String, val bn: String)

/** কাঞ্জি লেসনের অংশ নয় — আলাদা তালিকা (assets/<level>/kanji_<level>.json) */
data class KanjiItem(
    val kanji: String, val onyomi: String, val kunyomi: String,
    val bn: String, val en: String,
    val strokes: Int,
    val words: List<KanjiWord>,
    val sentenceJp: String, val sentenceBn: String
)

/** কথোপকথনের একটি লাইন। speaker = "A" | "B" */
data class ConvoLine(val speaker: String, val jp: String, val romaji: String, val bn: String, val en: String = "")
data class Dialogue(val title: String, val lines: List<ConvoLine>, val titleEn: String = "")

/** assets/<level>/conversation/lesson_NN.json */
data class ConvoLesson(val no: Int, val title: String, val dialogues: List<Dialogue>, val titleEn: String = "")

/** দৈনন্দিন বাক্য (assets/sentences.json) */
data class SentenceItem(val jp: String, val romaji: String, val bn: String, val en: String = "")
data class SentenceSection(val id: String, val titleBn: String, val titleEn: String, val items: List<SentenceItem>)

/** সংখ্যা (assets/number.json) */
data class NumberItem(val number: String, val kanji: String, val reading: String, val romaji: String, val note: String)
data class NumberSection(val id: String, val titleBn: String, val titleEn: String, val items: List<NumberItem>)

data class GrammarExample(val jp: String, val romaji: String, val bn: String, val en: String = "")

/** ব্যাকরণের একটি পয়েন্ট (বইয়ের "1. N1 は N2 です"-এর মতো)। note ঐচ্ছিক */
data class GrammarItem(
    val title: String, val pattern: String, val explain: String,
    val examples: List<GrammarExample>,
    val note: String = "",
    val titleEn: String = "", val patternEn: String = "",
    val explainEn: String = "", val noteEn: String = ""
)

/** একটি শব্দ। word = কাঞ্জি/লেখার রূপ, reading = কানা পড়া। note ঐচ্ছিক (ব্যবহারের টীকা) */
data class WordItem(
    val word: String, val reading: String, val romaji: String,
    val bn: String, val en: String,
    val note: String = ""
)

/** assets/<level>/vocabulary/lesson_NN.json */
data class VocabLesson(val no: Int, val title: String, val words: List<WordItem>, val titleEn: String = "")

/** assets/<level>/grammar/lesson_NN.json */
data class GrammarLesson(val no: Int, val title: String, val points: List<GrammarItem>, val titleEn: String = "")

/** লেসন তালিকার জন্য সারাংশ। exists = false মানে ফাইলটি এখনো যোগ হয়নি */
data class LessonInfo(val no: Int, val title: String, val count: Int, val exists: Boolean, val titleEn: String = "")

/** সার্চ ও কুইজের জন্য: কোন লেভেল/লেসনের শব্দ */
data class LessonWord(val level: String, val lessonNo: Int, val word: WordItem)

/**
 * ডেটার ফোল্ডার (level = n5 | n4):
 *   assets/<level>/vocabulary/lesson_NN.json   — লেসনের শব্দ
 *   assets/<level>/grammar/lesson_NN.json      — লেসনের ব্যাকরণ
 *   assets/<level>/conversation/lesson_NN.json — লেসনের কথোপকথন
 *   assets/<level>/kanji_<level>.json          — কাঞ্জি (লেসন ছাড়া)
 *   assets/sentences.json                      — দৈনন্দিন বাক্য (Basic → Sentences)
 *   assets/number.json                         — সংখ্যা (Basic → Number)
 * নতুন ফাইল রাখলেই অ্যাপে দেখাবে; কোড বা ডেটাবেজ বদলাতে হবে না।
 * Room ব্যবহার করে না, তাই ডেটাবেজ রিসেটে প্রভাব নেই।
 */
class ContentRepository private constructor(private val context: Context) {

    /** N5 = লেসন ১–২৫, N4 = ২৬–৫০ */
    fun lessonRange(level: String): IntRange = if (level == "n5") 1..25 else 26..50

    private fun lessonPath(level: String, folder: String, no: Int) =
        "$level/$folder/lesson_${"%02d".format(no)}.json"

    private fun readObject(path: String): JSONObject? = runCatching {
        JSONObject(context.assets.open(path).bufferedReader().use { it.readText() })
    }.getOrNull()

    // ---------------------------------------------------------------- শব্দ

    fun vocabLesson(level: String, no: Int): VocabLesson? =
        readObject(lessonPath(level, "vocabulary", no))?.let { o ->
            VocabLesson(
                o.optInt("lesson", no), o.optString("title", ""),
                list(o.optJSONArray("words")) { w ->
                    WordItem(
                        w.optString("word"), w.optString("reading"), w.optString("romaji"),
                        w.optString("bn"), w.optString("en"), w.optString("note")
                    )
                },
                o.optString("title_en")
            )
        }

    // ---------------------------------------------------------------- ব্যাকরণ

    fun grammarLesson(level: String, no: Int): GrammarLesson? =
        readObject(lessonPath(level, "grammar", no))?.let { o ->
            GrammarLesson(
                o.optInt("lesson", no), o.optString("title", ""),
                list(o.optJSONArray("points")) { g ->
                    GrammarItem(
                        g.optString("title"), g.optString("pattern"), g.optString("explain"),
                        list(g.optJSONArray("examples")) { e ->
                            GrammarExample(e.optString("jp"), e.optString("romaji"), e.optString("bn"), e.optString("en"))
                        },
                        g.optString("note"),
                        g.optString("title_en"), g.optString("pattern_en"),
                        g.optString("explain_en"), g.optString("note_en")
                    )
                },
                o.optString("title_en")
            )
        }

    // ---------------------------------------------------------------- কথোপকথন

    fun convoLesson(level: String, no: Int): ConvoLesson? =
        readObject(lessonPath(level, "conversation", no))?.let { o ->
            ConvoLesson(
                o.optInt("lesson", no), o.optString("title", ""),
                list(o.optJSONArray("dialogues")) { d ->
                    Dialogue(
                        d.optString("title"),
                        list(d.optJSONArray("lines")) { l ->
                            ConvoLine(l.optString("speaker"), l.optString("jp"), l.optString("romaji"), l.optString("bn"), l.optString("en"))
                        },
                        d.optString("title_en")
                    )
                },
                o.optString("title_en")
            )
        }

    /** type = "vocab" | "grammar" | "convo" — লেসন তালিকার সারাংশ (ফাইল না থাকলে exists = false) */
    fun lessonInfos(level: String, type: String): List<LessonInfo> = lessonRange(level).map { no ->
        when (type) {
            "vocab" -> vocabLesson(level, no)?.let { LessonInfo(no, it.title, it.words.size, true, it.titleEn) }
            "convo" -> convoLesson(level, no)?.let { LessonInfo(no, it.title, it.dialogues.size, true, it.titleEn) }
            else -> grammarLesson(level, no)?.let { LessonInfo(no, it.title, it.points.size, true, it.titleEn) }
        } ?: LessonInfo(no, "", 0, false)
    }

    /** সব লেসনের সব শব্দ (N5 ও N4) — সার্চ ও কুইজের জন্য */
    fun allWords(): List<LessonWord> = listOf("n5", "n4").flatMap { level ->
        lessonRange(level).flatMap { no ->
            vocabLesson(level, no)?.words.orEmpty().map { LessonWord(level, no, it) }
        }
    }

    // ---------------------------------------------------------------- কাঞ্জি

    private val kanjiCache = HashMap<String, List<KanjiItem>>()

    /** level = "n5" | "n4" — assets/<level>/kanji_<level>.json */
    @Synchronized
    fun kanji(level: String): List<KanjiItem> = kanjiCache.getOrPut(level) {
        runCatching {
            val text = context.assets.open("$level/kanji_$level.json").bufferedReader().use { it.readText() }
            list(JSONArray(text)) { k ->
                KanjiItem(
                    k.optString("kanji"), k.optString("on"), k.optString("kun"),
                    k.optString("bn"), k.optString("en"),
                    k.optInt("strokes", 0),
                    list(k.optJSONArray("words")) { w ->
                        KanjiWord(w.optString("w"), w.optString("r"), w.optString("bn"))
                    },
                    k.optJSONObject("sentence")?.optString("jp").orEmpty(),
                    k.optJSONObject("sentence")?.optString("bn").orEmpty()
                )
            }
        }.getOrDefault(emptyList())
    }

    // ---------------------------------------------------------------- দৈনন্দিন বাক্য

    private var sentenceCache: List<SentenceSection>? = null

    /** assets/sentences.json — Basic → Sentences */
    @Synchronized
    fun sentences(): List<SentenceSection> = sentenceCache ?: runCatching {
        val text = context.assets.open("sentences.json").bufferedReader().use { it.readText() }
        list(JSONObject(text).optJSONArray("sections")) { sec ->
            SentenceSection(
                sec.optString("id"), sec.optString("title_bn"), sec.optString("title_en"),
                list(sec.optJSONArray("items")) { n ->
                    SentenceItem(n.optString("jp"), n.optString("romaji"), n.optString("bn"), n.optString("en"))
                }
            )
        }
    }.getOrDefault(emptyList()).also { if (it.isNotEmpty()) sentenceCache = it }

    // ---------------------------------------------------------------- সংখ্যা

    private var numberCache: List<NumberSection>? = null

    /** assets/number.json — Basic → Number */
    @Synchronized
    fun numbers(): List<NumberSection> = numberCache ?: runCatching {
        val text = context.assets.open("number.json").bufferedReader().use { it.readText() }
        list(JSONObject(text).optJSONArray("sections")) { sec ->
            NumberSection(
                sec.optString("id"), sec.optString("title_bn"), sec.optString("title_en"),
                list(sec.optJSONArray("items")) { n ->
                    NumberItem(
                        n.optString("number"), n.optString("kanji"), n.optString("reading"),
                        n.optString("romaji"), n.optString("note")
                    )
                }
            )
        }
    }.getOrDefault(emptyList()).also { if (it.isNotEmpty()) numberCache = it }

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
