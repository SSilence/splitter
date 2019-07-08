package de.aditu.splitter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.io.IOUtils
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.nio.charset.Charset

fun main(args: Array<String>) {
    val target = if (args.isNotEmpty()) args[0] else null
    val baseUrl = "https://www.splitter-verlag.de"
    val parser = Parser()
    Crawler().start(
            baseUrl = baseUrl,
            success = { content, url ->
                println("$url SUCCESS ${content.body().toString().length} bytes")
                parser.process(content, url, baseUrl)
            },
            error = { e, url ->
                System.err.println("$url error ${e.message}")
            })
    println("website crawled")

    val books = jacksonObjectMapper().writeValueAsString(parser.getAllBooks())
    if (target != null) {
        File(target).writeText(books)
        println("books saved: ${target}")
    }

    val hostname = System.getenv("FTP_HOSTNAME")
    val port = if (System.getenv("FTP_PORT") != null) Integer.parseInt(System.getenv("FTP_PORT")) else 21
    val username = System.getenv("FTP_USERNAME")
    val password = System.getenv("FTP_PASSWORD")
    val filename = System.getenv("FILENAME")
    if (hostname != null && username != null && password != null) {
        upload(hostname, port, username, password, filename, books)
        println("books saved: ${hostname}")
    }

    println("finished")
}


fun upload(hostname: String, port: Int, username: String, password: String, filename: String, content: String) {
    val ftpClient = FTPClient()
    try {
        ftpClient.connect(hostname, port);
        ftpClient.login(username, password);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        val out = ftpClient.storeFileStream(filename)
        val ins = content.byteInputStream(Charset.forName("UTF-8"))
        IOUtils.copy(ins, out)
        ins.close()
        out.close()
        val completed = ftpClient.completePendingCommand()
        if (completed) {
            println("upload ${filename} success")
        }
    } catch (e: Exception) {
        System.err.println("upload ${filename} error ${e.message}")
        e.printStackTrace()
    } finally {
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (ex: Exception) {
            ex.printStackTrace();
        }
    }
}