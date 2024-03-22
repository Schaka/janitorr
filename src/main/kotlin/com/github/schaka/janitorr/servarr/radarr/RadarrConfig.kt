package com.github.schaka.janitorr.servarr.radarr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.data_structures.Tag
import com.github.schaka.janitorr.servarr.history.HistoryResponse
import com.github.schaka.janitorr.servarr.quality_profile.QualityProfile
import com.github.schaka.janitorr.servarr.radarr.movie.MoviePayload
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Only required for native image
 */
@Configuration(proxyBeanMethods = false)
@RegisterReflectionForBinding(classes = [QualityProfile::class, Tag::class, MoviePayload::class, HistoryResponse::class])
class RadarrConfig(
    val radarrNoOpService: RadarrNoOpService
) {

    @Bean
    @Radarr
    fun radarrService(
        radarrProperties: RadarrProperties,
        radarrClient: RadarrClient,
        applicationProperties: ApplicationProperties,
        fileSystemProperties: FileSystemProperties
    ): ServarrService {
        if (radarrProperties.enabled) {
            val service = RadarrRestService(radarrClient, applicationProperties, fileSystemProperties)
            service.postConstruct()
            return service
        }
        return radarrNoOpService
    }
}