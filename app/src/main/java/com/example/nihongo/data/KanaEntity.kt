package com.example.nihongo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kana")
data class KanaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val romaji: String,
    val bn: String,          // বাংলা উচ্চারণ
    val type: String,        // "hiragana" | "katakana"
    val grp: String,         // "basic" | "dakuten" | "yoon"
    val rowName: String,
    // SRS
    val level: Int = 0,      // 0 = নতুন/ভুলে গেছি, 1..7 = শেখা
    val nextReview: Long = 0L // 0 = এখনো দেখা হয়নি; নইলে epoch millis
)
