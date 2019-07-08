package de.aditu.splitter

import org.apache.http.HttpHeaders.USER_AGENT
import org.apache.http.client.HttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import java.io.InputStream
import kotlin.concurrent.getOrSet

private const val RETRY_HTTP_CLIENT = 3L
private const val READ_TIMEOUT_IN_MS = 10000
private const val CONNECTION_TIMEOUT_IN_MS = 10000
private const val THREADS = 8

class Crawler() {
    fun start(baseUrl: String,
              success: (content: Document, url: String) -> Unit,
              error: (exception: Throwable, url: String) -> Unit) {
        CrawlerWorker(baseUrl, success, error).run()
    }
}

class CrawlerWorker(val baseUrl: String,
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
                httpClient().execute(HttpGet(link)).entity.content.readAllToString()
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
                val linkPath = linkUrl.replace(baseUrl, "")
                if (linkUrl.isInternalLink(baseUrl) && !processed.containsKey(linkPath)) {
                    processed.put(linkPath, false)
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

    private fun httpClient() = ThreadLocal<HttpClient>().getOrSet {
        HttpClients
                .custom()
                .setDefaultHeaders(listOf(BasicHeader(USER_AGENT, "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")))
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(CONNECTION_TIMEOUT_IN_MS)
                                .setConnectionRequestTimeout(READ_TIMEOUT_IN_MS)
                                .build()
                )
                .setRetryHandler { _, executionCount, _ -> executionCount > RETRY_HTTP_CLIENT }
                .build()
    }

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
        return url.removeHash()
    }

    private fun String.removeHash() = if (this.contains("#")) this.substring(0, this.indexOf("#")) else this

    private fun String.isInternalLink(baseUrl: String) = this.toLowerCase().startsWith(baseUrl.toLowerCase())

    private fun InputStream.readAllToString() = this.bufferedReader().use { it.readText() }
}