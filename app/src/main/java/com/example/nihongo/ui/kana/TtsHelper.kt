package com.example.nihongo.ui.kana

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsHelper(context: Context) {
    private var ready: Boolean = false
    private var tts: TextToSpeech? = null
    private var rate: Float = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    val result = engine.setLanguage(Locale.JAPANESE)
                    ready = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                    engine.setSpeechRate(rate)
                }
            }
        }
    }

    /** 1.0 = স্বাভাবিক; ইঞ্জিন তৈরি হওয়ার আগে ডাকলেও পরে প্রয়োগ হবে */
    fun setRate(r: Float) {
        rate = r
        tts?.setSpeechRate(r)
    }

    fun speak(text: String) {
        if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kana")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
