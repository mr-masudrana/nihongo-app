package com.example.nihongo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KanaDao {
    @Query("SELECT * FROM kana WHERE type = :type AND (:grp = 'all' OR grp = :grp) ORDER BY id")
    fun byTypeAndGroup(type: String, grp: String): Flow<List<KanaEntity>>

    /** আগে দেখা কার্ড, যেগুলোর রিভিউ-এর সময় হয়েছে */
    @Query("SELECT * FROM kana WHERE type = :type AND nextReview > 0 AND nextReview <= :now ORDER BY nextReview LIMIT :limit")
    suspend fun dueReviews(type: String, now: Long, limit: Int): List<KanaEntity>

    /** এখনো না-দেখা নতুন কার্ড (চার্টের ক্রমে) */
    @Query("SELECT * FROM kana WHERE type = :type AND nextReview = 0 ORDER BY id LIMIT :limit")
    suspend fun newCards(type: String, limit: Int): List<KanaEntity>

    @Query("SELECT * FROM kana")
    suspend fun all(): List<KanaEntity>

    /** লেভেল ৩+ হলে "শেখা" ধরা হয় */
    @Query("SELECT COUNT(*) FROM kana WHERE level >= 3")
    fun learnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM kana")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(list: List<KanaEntity>)

    @Update
    suspend fun update(card: KanaEntity)
}
