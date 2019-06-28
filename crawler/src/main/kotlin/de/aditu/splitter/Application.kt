package de.aditu.splitter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("bitte Ziel als Argument angeben z.B. C:\\tmp\\books.json")
        return
    }
    val target = args[0]
    val baseUrl = "https://www.splitter-verlag.de"
    val parser = Parser()
    Crawler().start(
            baseUrl = baseUrl,
            success = { content, url ->
                System.out.println("$url SUCCESS ${content.body().toString().length} bytes")
                parser.process(content, url, baseUrl)
            },
            error = { e, url ->
                System.err.println("$url error ${e.message}")
            })
    System.out.println("website crawled")
    val books = jacksonObjectMapper().writeValueAsString(parser.getAllBooks())
    File(target).writeText(books)
    System.out.println("books safed")
    System.out.println("finished")
}