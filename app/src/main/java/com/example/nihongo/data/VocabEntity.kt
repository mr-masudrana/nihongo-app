package com.example.nihongo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocab")
data class VocabEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,        // কাঞ্জি/কানা রূপ
    val reading: String,     // হিরাগানা/কাতাকানা পড়া
    val romaji: String,
    val meaningBn: String,
    val meaningEn: String,
    val category: String,
    // SRS
    val level: Int = 0,
    val nextReview: Long = 0L
)
