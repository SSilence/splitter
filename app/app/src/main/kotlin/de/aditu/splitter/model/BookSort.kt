package de.aditu.splitter.model

import de.aditu.splitter.R

enum class BookSort(val string: Int) {
    RELEASE_DESC(R.string.release_desc),
    RELEASE_ASC(R.string.release_asc),
    TITLE_ASC(R.string.title_asc),
    PRICE_ASC(R.string.price_asc),
    PRICE_DESC(R.string.price_desc),
}