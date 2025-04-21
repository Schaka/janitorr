package com.github.schaka.janitorr

import com.github.schaka.janitorr.jellyseerr.JellyseerrClient
import com.github.schaka.janitorr.mediaserver.MediaServerClient
import com.github.schaka.janitorr.mediaserver.MediaServerUserClient
import com.github.schaka.janitorr.mediaserver.emby.EmbyMediaServerClient
import com.github.schaka.janitorr.servarr.RestClientProperties
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.bazarr.BazarrClient
import com.github.schaka.janitorr.servarr.radarr.RadarrClient
import com.github.schaka.janitorr.servarr.sonarr.SonarrClient
import com.github.schaka.janitorr.stats.StatsClientProperties
import com.github.schaka.janitorr.stats.jellystat.JellystatClient
import com.github.schaka.janitorr.stats.streamystats.StreamystatsClient
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableConfigurationProperties
@EnableAsync
@EnableCaching
@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
@ImportRuntimeHints(JanitorrApplication.Hints::class)
class JanitorrApplication {

    class Hints : RuntimeHintsRegistrar {
        override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
            hints.proxies().registerJdkProxy(JellyseerrClient::class.java)
            hints.proxies().registerJdkProxy(JellystatClient::class.java)
            hints.proxies().registerJdkProxy(StreamystatsClient::class.java)
            hints.proxies().registerJdkProxy(EmbyMediaServerClient::class.java)
            hints.proxies().registerJdkProxy(MediaServerClient::class.java)
            hints.proxies().registerJdkProxy(MediaServerUserClient::class.java)
            hints.proxies().registerJdkProxy(RadarrClient::class.java)
            hints.proxies().registerJdkProxy(SonarrClient::class.java)
            hints.proxies().registerJdkProxy(BazarrClient::class.java)
            hints.proxies().registerJdkProxy(ServarrService::class.java)

            hints.proxies().registerJdkProxy(RestClientProperties::class.java)
            hints.proxies().registerJdkProxy(StatsClientProperties::class.java)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<JanitorrApplication>(*args)
}