package com.beomsoo.sentencelibrary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Publication::class, Issue::class, Article::class, Quote::class, Tag::class,
        QuoteTagCrossRef::class, StatusDefinition::class, CustomFieldDefinition::class,
        CustomFieldValue::class, AppPreference::class, RecoverySnapshot::class, SyncMetadata::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sentence-library-v3.db"
            ).build().also { INSTANCE = it }
        }
    }
}
