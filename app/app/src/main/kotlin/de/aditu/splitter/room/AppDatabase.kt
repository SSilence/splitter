package de.aditu.splitter.room

import androidx.room.Database
import androidx.room.RoomDatabase
import de.aditu.splitter.model.Book

@Database(entities = arrayOf(Book::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}