package de.aditu.splitter

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import java.text.SimpleDateFormat
import java.util.*

class Parser {

    private val urlToAlbum = Collections.synchronizedMap(mutableMapOf<String, AlbumPage>())
    private val books = Collections.synchronizedMap(mutableMapOf<String, Book>())

    fun process(content: Document, url: String) {
        when {
            isBookPage(content) -> processBookPage(content, url)
            isAlbumPage(url) -> processAlbumPage(content)
        }
    }

    fun getAllBooks(): Collection<Book> = books.values

    private fun processBookPage(content: Document, url: String) {
        val metadata = fetchBooksMetaData(content)
        val images = fetchImages(content)
        val genre = if (urlToAlbum[url]?.genre == null && content.select("#breadcrumb_navi > span:nth-child(2) span").html().cleanHtml()?.trim() == "Genre") {
            content.select("#breadcrumb_navi > span:nth-child(3) span").html().cleanHtml()?.trim()
        } else {
            urlToAlbum[url]?.genre
        }
        val book = Book(
                isbn = metadata.get("ISBN")?.removeNonNumbers(),
                url = url,
                title = content.select(".product-info-title-desktop span, h1.product-info-title-desktop").first().html().cleanHtml().cleanHtmlEntitiesAndToManyLinebreaks(),
                description = content.select(".tab-body.active").html().cleanHtmlPreserveLinebreaks().cleanHtmlEntitiesAndToManyLinebreaks(),
                album = urlToAlbum[url]?.album,
                genre = genre,
                release = fetchRelease(metadata),
                scenario = metadata.get("Szenario"),
                painter = metadata.get("Zeichnung"),
                format = metadata.get("Einband"),
                pages = metadata.get("Seitenzahl")?.split(" ")?.get(0)?.removeNonNumbers()?.toIntOrNull(),
                number = metadata.get("Band")?.split(" von ")?.get(0)?.removeNonNumbers()?.toIntOrNull(),
                parts = metadata.get("Band")?.split(" von ")?.get(1)?.removeNonNumbers()?.toIntOrNull(),
                price = content.select(".current-price-container span").attr("content").toDoubleOrNull() ?:
                            content.select(".product-info-details .current-price-container").html().replace(",", ".").replace("EUR", "").toDoubleOrNull(),
                images = images.joinToString("\n")
        )

        books.put(book.isbn!!, book)
    }

    private fun processAlbumPage(content: Document) {
        val albumPage = AlbumPage(
            urls = content.select("a.product-url").map { it.attr("href").trim() },
            genre = content.select("#breadcrumb_navi > span:nth-child(3) span").html().cleanHtml(),
            album = content.select("#breadcrumb_navi > span:nth-child(4) span").html().cleanHtml()
        )

        for (url in albumPage.urls) {
            urlToAlbum.put(url, albumPage)
            books[url]?.let {
                it.album = albumPage.album
                it.genre = albumPage.genre
            }
        }
    }

    private fun fetchImages(content: Document) =
            content.select(".swiper-slide-inside")
                    .map { it.select("img").attr("src").cleanHtml() }
                    .filter { it.trim().isNotEmpty() }
                    .distinct()

    private fun fetchRelease(metadata: Map<String, String>) = try {
            val str = metadata.get("Erschienen am") ?: metadata.get("Lieferbar ab")
            SimpleDateFormat("dd.MM.yyyy").parse(str)
        } catch (e: Exception) {
            null
        }

    private fun fetchBooksMetaData(content: Document): Map<String, String> {
        val list = mutableListOf<Pair<String, String>>()
        for (e in content.select("dt")) {
            list.add(Pair(e.text().cleanHtml().trim(':'), ""))
        }

        content.select("dd").forEachIndexed { i, e ->
            list[i] = Pair(list[i].first, e.html().cleanHtml().trim())
        }

        return list.toMap()
    }

    private fun isBookPage(content: Document) = content.select("body.page-product-info").size > 0

    private fun isAlbumPage(url: String) = ".*/alben/[^/]+/.+".toRegex().matches(url) // https://www.splitter-verlag.de/alben/abenteuer/aeropostal-legendaere-piloten/

}

fun String.cleanHtml() = Jsoup.clean(this, Whitelist.none())
fun String.cleanHtmlPreserveLinebreaks() = Jsoup.clean(this.replace("<br>", "\n"), "", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
fun String.cleanHtmlEntitiesAndToManyLinebreaks() = this.replace("[\n]{2,}".toRegex(), "\n")
        .replace("\n \n \n \n".toRegex(), "\n\n")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
fun String.removeNonNumbers() = this.replace("[^\\d]".toRegex(), "")

private class AlbumPage(val urls: List<String>, val genre: String, val album: String)