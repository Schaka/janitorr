package com.github.schaka.janitorr.external.common

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@ConditionalOnProperty(prefix = "external-apis", name = ["enabled"], havingValue = "true")
class ExternalDataCacheConfig(
    private val externalDataProperties: ExternalDataProperties
) {

    @Bean
    fun externalDataCacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager(ExternalDataService.CACHE_NAME)
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(externalDataProperties.cacheRefreshInterval.toHours(), TimeUnit.HOURS)
                .maximumSize(1000)
        )
        return cacheManager
    }
}
