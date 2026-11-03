package com.example.nihongo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

class KanaRepository private constructor(
    private val context: Context,
    private val dao: KanaDao
) {
    private val seedLock = Mutex()

    fun kana(type: String, grp: String): Flow<List<KanaEntity>> = dao.byTypeAndGroup(type, grp)

    suspend fun allKana(): List<KanaEntity> = dao.all()

    /** প্রথমবার চালালে assets/kana.json থেকে ডেটাবেজে ঢোকায় (একাধিকবার ডাকলেও নিরাপদ) */
    suspend fun seedIfEmpty() = seedLock.withLock {
        withContext(Dispatchers.IO) {
            if (dao.count() > 0) return@withContext
            val json = context.assets.open("kana.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(json)
            val list = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                KanaEntity(
                    symbol = o.getString("symbol"),
                    romaji = o.getString("romaji"),
                    bn = o.getString("bn"),
                    type = o.getString("type"),
                    grp = o.getString("grp"),
                    rowName = o.getString("row")
                )
            }
            dao.insertAll(list)
        }
    }

    companion object {
        @Volatile private var INSTANCE: KanaRepository? = null

        fun get(context: Context): KanaRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: KanaRepository(
                    context.applicationContext,
                    AppDatabase.get(context).kanaDao()
                ).also { INSTANCE = it }
            }
    }
}
