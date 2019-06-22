package de.aditu.splitter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jsoup.Jsoup
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.ApplicationArguments
import org.slf4j.LoggerFactory
import org.springframework.context.support.AbstractApplicationContext
import java.io.BufferedReader
import java.io.File
import java.net.URL

@SpringBootApplication
open class Application(private val args: ApplicationArguments,
                       private val applicationContext: AbstractApplicationContext,
                       private val crawler: Crawler,
                       private val parser: Parser) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun run(vararg arguments: String?) {
        when {
            args.containsOption("crawl") -> fetch(args.getOptionValues("crawl")[0])
            args.containsOption("test") -> {
                val url = "https://www.splitter-verlag.de/die-legende-der-drachenritter-bd-27.html"
                val content = Jsoup.parse(URL(url), 5000)
                parser.process(content, url, "https://www.splitter-verlag.de")
            }
            args.containsOption("nogenre") -> nogenre(args.getOptionValues("nogenre")[0])
            else -> log.info("no option given")
        }
        SpringApplication.exit(applicationContext)
    }

    private fun nogenre(target: String) {
        val json = File(target).bufferedReader().use(BufferedReader::readText)
        val books: List<Book> = jacksonObjectMapper().readValue(json, object : TypeReference<List<Book>>() {})
        log.info("books without genre: " + books.filter { it.genre.isNullOrBlank() }.count())
        log.info("books with genre: " + books.filter { it.genre.isNullOrBlank() == false }.count())
    }

    private fun fetch(target: String) {
        val baseUrl = "https://www.splitter-verlag.de"
        crawler.start(
                baseUrl = baseUrl,
                success = { content, url ->
                    log.info("$url SUCCESS ${content.body().toString().length} bytes")
                    parser.process(content, url, baseUrl)
                },
                error = { e, url ->
                    log.error("$url error ${e.message}")
                })
        log.info("website crawled")
        val books = jacksonObjectMapper().writeValueAsString(parser.getAllBooks())
        File(target).writeText(books)
        log.info("books safed")
        log.info("finished")
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
