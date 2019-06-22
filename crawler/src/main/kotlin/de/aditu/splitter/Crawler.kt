package de.aditu.splitter

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private const val RETRY_HTTP_CLIENT = 3L
private const val READ_TIMEOUT_IN_MS = 10000L
private const val THREADS = 8

@Service
class Crawler(@Autowired private val webClient: WebClient) {
    fun start(baseUrl: String,
              success: (content: Document, url: String) -> Unit,
              error: (exception: Throwable, url: String) -> Unit) {
        CrawlerWorker(baseUrl, webClient, success, error).run()
    }
}

class CrawlerWorker(val baseUrl: String,
                    val webClient: WebClient,
                    val success: (content: Document, url: String) -> Unit,
                    val error: (exception: Throwable, url: String) -> Unit) {

    private val executor: ExecutorService = Executors.newFixedThreadPool(THREADS)
    private val processed: ConcurrentHashMap<String, Boolean> = ConcurrentHashMap()
    private val running = AtomicInteger(1)

    fun run() {
        scan(baseUrl)
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
    }

    fun scan(link: String) {
        try {
            val content = try {
                load(link)
            } catch (e: Exception) {
                error(e, link)
                null
            }

            var links = listOf<Element>()
            if (content != null) {
                val doc = Jsoup.parse(content)
                success(doc, link)
                links = doc.select("a")
            }

            for (a in links) {
                val linkUrl = parseUrl(baseUrl, a)
                if (isInternalLink(linkUrl, baseUrl) && !processed.containsKey(linkUrl)) {
                    processed.put(linkUrl, false)
                    running.incrementAndGet()
                    executor.submit { scan(linkUrl) }
                }
            }
        } finally {
            if (running.decrementAndGet() == 0) {
                executor.shutdownNow()
            }
        }
    }

    private fun load(url: String)=
            webClient.mutate().build() // make new instance 4 thread safety
                .get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String::class.java)
                .retry(RETRY_HTTP_CLIENT)
                .block(Duration.ofMillis(READ_TIMEOUT_IN_MS))

    private fun isInternalLink(link: String, baseUrl: String) = link.toLowerCase().startsWith(baseUrl.toLowerCase())

    // handle ./ links and #hashes
    private fun parseUrl(baseUrl: String, a: Element?): String {
        if (a == null) {
            return ""
        }
        var url = a.absUrl("href")
        if (url.isEmpty()) {
            url = a.attr("href")
            url = if (url.startsWith("./")) url.substring(2) else url

            url = when {
                url.startsWith("/") -> URI(baseUrl).scheme + "://" + URI(baseUrl).host + url
                url == "." -> baseUrl
                else -> baseUrl + "/" + url
            }
        }
        return removeHash(url)
    }

    private fun removeHash(url: String) = if (url.contains("#")) url.substring(0, url.indexOf("#")) else url
}