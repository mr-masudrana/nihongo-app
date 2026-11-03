package com.example.nihongo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabDao {
    @Query(
        """SELECT * FROM vocab
           WHERE (:cat = 'all' OR category = :cat)
             AND (:q = '' OR word LIKE '%' || :q || '%' OR reading LIKE '%' || :q || '%'
                  OR romaji LIKE '%' || :q || '%' OR meaningBn LIKE '%' || :q || '%'
                  OR meaningEn LIKE '%' || :q || '%')
           ORDER BY id"""
    )
    fun search(cat: String, q: String): Flow<List<VocabEntity>>

    /** লেভেল ৩+ হলে "শেখা" ধরা হয় */
    @Query("SELECT COUNT(*) FROM vocab WHERE level >= 3")
    fun learnedCount(): Flow<Int>

    @Query("SELECT * FROM vocab WHERE (:cat = 'all' OR category = :cat) AND nextReview > 0 AND nextReview <= :now ORDER BY nextReview LIMIT :limit")
    suspend fun dueReviews(cat: String, now: Long, limit: Int): List<VocabEntity>

    @Query("SELECT * FROM vocab WHERE (:cat = 'all' OR category = :cat) AND nextReview = 0 ORDER BY id LIMIT :limit")
    suspend fun newCards(cat: String, limit: Int): List<VocabEntity>

    @Query("SELECT * FROM vocab")
    suspend fun all(): List<VocabEntity>

    @Query("SELECT COUNT(*) FROM vocab")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(list: List<VocabEntity>)

    @Update
    suspend fun update(card: VocabEntity)
}
