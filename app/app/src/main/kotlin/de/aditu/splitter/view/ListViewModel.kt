package de.aditu.splitter.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.aditu.splitter.repository.BookRepository
import androidx.lifecycle.Transformations
import de.aditu.splitter.model.BookSort

class ListViewModel(private val bookRepository: BookRepository) : ViewModel() {

    private val filter = MutableLiveData<Filter>()

    val books = Transformations.switchMap(filter) { filter ->
        when {
            filter?.both() ?: false -> bookRepository.searchByTitleAndGenre(filter.q!!, filter.genre!!, filter.bookSort)
            filter?.onlyQ() ?: false -> bookRepository.searchByTitle(filter.q!!, filter.bookSort)
            filter?.onlyGenre() ?: false -> bookRepository.searchByGenre(filter.genre!!, filter.bookSort)
            else -> bookRepository.load(filter?.bookSort ?: BookSort.RELEASE_DESC)
        }
    }.apply {
        filter.value = null
    }

    val genres = bookRepository.genres()

    fun refreshBooksInDatabase() {
        bookRepository.refreshBookDatabase()
    }

    fun search(q: String?, genre: String?, bookSort: BookSort) {
        filter.value = Filter(q, genre, bookSort)
    }
}

class Filter(val q: String?, val genre: String?, val bookSort: BookSort) {
    fun onlyQ() = q.isNullOrEmpty() == false && genre.isNullOrEmpty()
    fun onlyGenre() = q.isNullOrEmpty() && genre.isNullOrEmpty() == false
    fun both() = q.isNullOrEmpty() == false && genre.isNullOrEmpty() == false
}