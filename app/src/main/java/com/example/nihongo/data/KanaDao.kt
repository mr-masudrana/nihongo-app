package com.example.nihongo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KanaDao {
    @Query("SELECT * FROM kana WHERE type = :type AND (:grp = 'all' OR grp = :grp) ORDER BY id")
    fun byTypeAndGroup(type: String, grp: String): Flow<List<KanaEntity>>

    @Query("SELECT * FROM kana")
    suspend fun all(): List<KanaEntity>

    @Query("SELECT COUNT(*) FROM kana")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(list: List<KanaEntity>)
}
