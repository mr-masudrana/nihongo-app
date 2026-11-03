package com.example.nihongo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

class VocabRepository private constructor(
    private val context: Context,
    private val dao: VocabDao
) {
    private val seedLock = Mutex()

    fun search(cat: String, q: String): Flow<List<VocabEntity>> = dao.search(cat, q)
    fun learnedCount(): Flow<Int> = dao.learnedCount()

    suspend fun dueReviews(cat: String, now: Long, limit: Int) = dao.dueReviews(cat, now, limit)
    suspend fun newCards(cat: String, limit: Int) = dao.newCards(cat, limit)
    suspend fun update(card: VocabEntity) = dao.update(card)
    suspend fun allVocab(): List<VocabEntity> = dao.all()

    suspend fun seedIfEmpty() = seedLock.withLock {
        withContext(Dispatchers.IO) {
            if (dao.count() > 0) return@withContext
            val json = context.assets.open("vocab_n5.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            val list = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                VocabEntity(
                    word = o.getString("word"),
                    reading = o.getString("reading"),
                    romaji = o.getString("romaji"),
                    meaningBn = o.getString("bn"),
                    meaningEn = o.getString("en"),
                    category = o.getString("cat")
                )
            }
            dao.insertAll(list)
        }
    }

    companion object {
        @Volatile private var INSTANCE: VocabRepository? = null

        fun get(context: Context): VocabRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: VocabRepository(
                    context.applicationContext,
                    AppDatabase.get(context).vocabDao()
                ).also { INSTANCE = it }
            }
    }
}
