package com.example.nihongo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [KanaEntity::class, VocabEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kanaDao(): KanaDao
    abstract fun vocabDao(): VocabDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nihongo.db"
                )
                    // ডেভেলপমেন্ট পর্যায়: স্কিমা বদলালে ডেটাবেজ নতুন করে তৈরি হবে।
                    // লঞ্চের আগে প্রপার Migration লিখে এটা সরিয়ে দিন।
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
