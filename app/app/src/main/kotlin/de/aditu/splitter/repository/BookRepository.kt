package de.aditu.splitter.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.aditu.splitter.R
import de.aditu.splitter.http.BackendClient
import de.aditu.splitter.model.Book
import de.aditu.splitter.model.BookSort
import de.aditu.splitter.room.BookDao
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.*

class BookRepository(private val context: Context,
                     private val backendClient: BackendClient,
                     private val sharedPreferencesRepository: SharedPreferencesRepository,
                     private val bookDao: BookDao) {

    fun load(bookSort: BookSort) = bookDao.list(bookSort)

    fun findByIsbn(isbn: String) = bookDao.findByIsbn(isbn)

    fun searchByTitle(q: String, bookSort: BookSort) = bookDao.searchByTitle("%$q%", bookSort)

    fun searchByGenre(genre: String, bookSort: BookSort) = bookDao.searchByGenre("%$genre%", bookSort)

    fun searchByTitleAndGenre(q: String, genre: String, bookSort: BookSort) = bookDao.searchByTitleAndGenre("%$q%", "%$genre%", bookSort)

    fun genres() = bookDao.getAllGenres()

    fun lastModified() =
        if (sharedPreferencesRepository.lastModified == 0L) {
            context.getString(R.string.books_json_last_modified).lastModifiedToDate()
        } else {
            sharedPreferencesRepository.lastModified
        }

    fun refreshBookDatabase() =
        hasCachedBooks()
            .flatMap { if (it) refreshFromBackend() else refreshFromAssets() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, { Log.e(BookRepository::class.java.canonicalName, "error refresh books in database", it) })

    private fun refreshFromAssets() =
        Observable.fromCallable {
            Log.i(BookRepository::class.java.canonicalName, "refreshFromAssets()")
            val json = context.assets.open("books.json").bufferedReader().use(BufferedReader::readText)
            val books = Gson().fromJson<List<Book>>(json, object : TypeToken<List<Book>>() {}.type)
            bookDao.insertAll(books)
            sharedPreferencesRepository.lastModified = context.getString(R.string.books_json_last_modified).lastModifiedToDate()
            Log.i(BookRepository::class.java.canonicalName, "successfully refreshFromAssets()")
            true
        }

    private fun refreshFromBackend() =
        hasNotTodayRefreshed()
            .flatMap { hasBackendNewData() }
            .flatMap { backendClient.all().toObservable() }
            .map {
                bookDao.deleteAll()
                bookDao.insertAll(it.body()!!)
                sharedPreferencesRepository.lastModified = it.parseLastModified() ?: 0
                sharedPreferencesRepository.lastRefresh = todayAsString()
                Log.i(BookRepository::class.java.canonicalName, "successfully refreshFromBackend()")
                true
            }

    private fun hasBackendNewData() =
        backendClient.allHeader()
            .map { it.parseLastModified() != sharedPreferencesRepository.lastModified }
            .filter {
                Log.i(BookRepository::class.java.canonicalName, "hasBackendNewData = $it")
                if (!it) {
                    sharedPreferencesRepository.lastRefresh = todayAsString()
                }
                it
            }
            .toObservable()

    private fun hasCachedBooks() = Observable.fromCallable { bookDao.count() > 0 }

    private fun hasNotTodayRefreshed() =
        Observable.just(sharedPreferencesRepository.lastRefresh != todayAsString())
            .filter {
                Log.i(BookRepository::class.java.canonicalName, "hasNotTodayRefreshed = $it")
                it
            }

    private fun <T> Response<T>.parseLastModified() = this.headers()["Last-Modified"]?.lastModifiedToDate()

    private fun todayAsString() = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(Date())

    private fun String.lastModifiedToDate() = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH).parse(this).time
}