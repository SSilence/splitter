package de.aditu.splitter

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

// popup_images = full size
// info_images = big thumb
// gallery_images = mini thumb

class Book(
        var isbn: String? = null,
        var url: String? = null,
        var title: String? = null,
        var description: String? = null,
        var album: String? = null,
        var genre: String? = null,
        var release: Date? = null,
        var scenario: String? = null,
        var painter: String? = null,
        var format: String? = null,
        var pages: Int? = null,
        var number: Int? = null,
        var parts: Int? = null,
        var price: Double? = null,
        var images: String? = null) {

        @JsonIgnore
        fun getThumbnailUrl() = this.images
                ?.split("\n")
                ?.filter { it.contains("info_images") }
                ?.firstOrNull()
}