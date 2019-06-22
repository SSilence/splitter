package de.aditu.splitter.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import de.aditu.splitter.model.Book
import de.aditu.splitter.model.BookSort

@Dao
abstract class BookDao {

    fun list(bookSort: BookSort): LiveData<List<Book>> =
        list(SimpleSQLiteQuery("SELECT isbn, title, painter, images, release FROM Book ORDER BY " + bookSort.sql()))

    @RawQuery(observedEntities = arrayOf(Book::class))
    abstract fun list(query: SupportSQLiteQuery): LiveData<List<Book>>

    @Insert
    abstract fun insertAll(books: List<Book>)

    @Query("DELETE FROM book")
    abstract fun deleteAll()

    @Query("SELECT COUNT(*) FROM book")
    abstract fun count(): Int

    @RawQuery(observedEntities = arrayOf(Book::class))
    fun searchByTitle(q: String, bookSort: BookSort): LiveData<List<Book>> =
        list(SimpleSQLiteQuery("SELECT isbn, title, painter, images, release FROM book WHERE title LIKE ? OR painter LIKE ? ORDER BY " + bookSort.sql(), arrayOf(q, q)))

    @RawQuery(observedEntities = arrayOf(Book::class))
    fun searchByGenre(genre: String, bookSort: BookSort): LiveData<List<Book>> =
        list(SimpleSQLiteQuery("SELECT isbn, title, painter, images, release FROM book WHERE genre LIKE ? ORDER BY " + bookSort.sql(), arrayOf(genre)))

    @RawQuery(observedEntities = arrayOf(Book::class))
    fun searchByTitleAndGenre(q: String, genre: String, bookSort: BookSort): LiveData<List<Book>> =
        list(SimpleSQLiteQuery("SELECT isbn, title, painter, images, release FROM book WHERE genre LIKE ? AND (title LIKE ? OR painter LIKE ?) ORDER BY " + bookSort.sql(), arrayOf(genre, q, q)))

    @Query("SELECT DISTINCT genre FROM book ORDER BY genre")
    abstract fun getAllGenres(): LiveData<List<String>>

    @Query("SELECT * FROM book WHERE isbn=:isbn")
    abstract fun findByIsbn(isbn: String): LiveData<Book>

    fun BookSort.sql() = when(this) {
        BookSort.RELEASE_ASC -> " release ASC"
        BookSort.TITLE_ASC -> " title ASC"
        BookSort.PRICE_ASC -> " price ASC"
        BookSort.PRICE_DESC -> " price DESC"
        else -> " release DESC"
    }

}