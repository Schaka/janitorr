package com.github.schaka.janitorr.torrent.rest

import com.github.schaka.janitorr.torrent.qbit.QBittorrent
import com.github.schaka.janitorr.torrent.qbit.QbitAuthInterceptor
import com.github.schaka.janitorr.torrent.transmission.Transmission
import com.github.schaka.janitorr.torrent.transmission.TransmissionAuthHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class TorrentClientConfig {

    @ConditionalOnProperty("clients.torrent.type", havingValue = "QBITTORRENT")
    @QBittorrent
    @Bean
    fun qBittorrentTemplate(builder: RestTemplateBuilder, properties: TorrentClientProperties): RestTemplate {
        return builder
            .rootUri("${properties.url}/api/v2")
            .interceptors(listOf(QbitAuthInterceptor(properties)))
            .build()
    }

    @ConditionalOnProperty("clients.torrent.type", havingValue = "TRANSMISSION")
    @Transmission
    @Bean
    fun transmissionTemplate(builder: RestTemplateBuilder, properties: TorrentClientProperties): RestTemplate {
        val transmissionAuth = TransmissionAuthHandler(properties)
        return builder
            .rootUri("${properties.url}/transmission/rpc")
            .basicAuthentication(properties.username, properties.password)
            .interceptors(listOf(transmissionAuth))
            .build()
    }
}