package de.aditu.splitter.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// popup_images = full size
// info_images = big thumb
// gallery_images = mini thumb

@Entity
class Book(
    @PrimaryKey
    var isbn: String = "",
    var url: String? = null,
    var title: String? = null,
    var description: String? = null,
    var album: String? = null,
    var genre: String? = null,
    var release: Long? = null,
    var scenario: String? = null,
    var painter: String? = null,
    var format: String? = null,
    var pages: Int? = null,
    var number: Int? = null,
    var parts: Int? = null,
    var price: Double? = null,
    var images: String? = null
) {

    fun getThumbnailUrl() = this.images
        ?.split("\n")
        ?.filter { it.contains("info_images") }
        ?.firstOrNull()

    fun getPreviewImages() = this.images
        ?.split("\n")
        ?.filter { it.contains("popup_images") }
        ?: emptyList()

    override fun toString(): String {
        return "Book(isbn=$isbn, title=$title)"
    }

}