package de.aditu.splitter.http

import de.aditu.splitter.model.Book
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HEAD

interface BackendClient {
    @GET("books.json")
    fun all() : Single<Response<List<Book>>>

    @HEAD("books.json")
    fun allHeader() : Single<Response<Void>>
}