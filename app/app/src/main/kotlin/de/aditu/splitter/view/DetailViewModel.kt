package de.aditu.splitter.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.aditu.splitter.repository.BookRepository
import androidx.lifecycle.Transformations
import de.aditu.splitter.model.Book

class DetailViewModel(private val bookRepository: BookRepository) : ViewModel() {

    var isbnLiveData = MutableLiveData<String>()

    val book = Transformations.switchMap(isbnLiveData) { isbn ->
        when {
            isbn.isNullOrEmpty() == false -> bookRepository.findByIsbn(isbn)
            else -> MutableLiveData<Book>()
        }
    }

    fun load(isbn: String) {
        isbnLiveData.value = isbn
    }

}
