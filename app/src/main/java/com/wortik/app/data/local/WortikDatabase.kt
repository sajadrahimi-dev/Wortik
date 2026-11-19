package com.wortik.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WordEntity::class, WordFtsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class WortikDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}
