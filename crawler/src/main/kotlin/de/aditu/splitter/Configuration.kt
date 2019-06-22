package de.aditu.splitter

import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import io.netty.handler.timeout.ReadTimeoutHandler
import reactor.netty.http.client.HttpClient

private const val READ_TIMEOUT_IN_MS = 5000
private const val CONNECT_TIMEOUT_IN_MS = 5000

@Configuration
class Configuration {

    @Bean
    fun createWebClient() =
            WebClient
                    .builder()
                    .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .clientConnector(ReactorClientHttpConnector(
                            HttpClient.create()
                                    .tcpConfiguration({ client ->
                                        client.doOnConnected({ conn ->
                                            conn.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT_IN_MS))
                                        })
                                        client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_IN_MS)
                                    })
                    ))
                    .build()

}