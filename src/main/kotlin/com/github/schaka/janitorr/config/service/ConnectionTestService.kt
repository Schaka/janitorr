package com.github.schaka.janitorr.config.service

import com.github.schaka.janitorr.api.dto.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.net.URI

/**
 * Service for testing connections to external services.
 * 
 * Provides connection validation for:
 * - Sonarr
 * - Radarr
 * - Jellyfin
 * - Emby
 * - Jellyseerr
 * - Jellystat
 * - Streamystats
 * - Bazarr
 */
@Profile("!leyden")
@ConditionalOnProperty(prefix = "management.ui", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@Service
class ConnectionTestService {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionTestService::class.java)
    }

    private val webClient = WebClient.builder().build()

    /**
     * Test all enabled service connections
     */
    fun testAllConnections(config: ConfigurationDto): ConnectionTestsResult {
        return ConnectionTestsResult(
            sonarr = if (config.clients.sonarr.enabled) testSonarr(config.clients.sonarr) else null,
            radarr = if (config.clients.radarr.enabled) testRadarr(config.clients.radarr) else null,
            jellyfin = if (config.clients.jellyfin.enabled) testJellyfin(config.clients.jellyfin) else null,
            emby = if (config.clients.emby.enabled) testEmby(config.clients.emby) else null,
            jellyseerr = if (config.clients.jellyseerr.enabled) testJellyseerr(config.clients.jellyseerr) else null,
            jellystat = if (config.clients.jellystat.enabled) testJellystat(config.clients.jellystat) else null,
            streamystats = if (config.clients.streamystats.enabled) testStreamystats(config.clients.streamystats) else null,
            bazarr = if (config.clients.bazarr.enabled) testBazarr(config.clients.bazarr) else null
        )
    }

    /**
     * Test Sonarr connection
     */
    fun testSonarr(config: SonarrConfigDto): ConnectionTestResult {
        if (!config.enabled) {
            return ConnectionTestResult(false, "Sonarr is disabled")
        }
        
        return try {
            val url = "${config.url.trimEnd('/')}/api/v3/system/status"
            
            val response = webClient.get()
                .uri(url)
                .header("X-Api-Key", config.apiKey)
                .retrieve()
                .toEntity(String::class.java)
                .block()
            
            if (response != null && response.statusCode.is2xxSuccessful) {
                ConnectionTestResult(true, "✅ Connected to Sonarr successfully", response.body)
            } else {
                ConnectionTestResult(false, "❌ Unexpected response from Sonarr: ${response?.statusCode}")
            }
        } catch (e: WebClientResponseException) {
            if (e.statusCode.value() == 401) {
                ConnectionTestResult(false, "❌ Authentication failed. Check API key.", e.message)
            } else {
                ConnectionTestResult(false, "❌ HTTP error: ${e.statusCode} - ${e.statusText}", e.message)
            }
        } catch (e: WebClientRequestException) {
            log.error("Network error testing Sonarr connection", e)
            ConnectionTestResult(false, "❌ Connection error: ${e.message}. Check URL and network connectivity.", e.stackTraceToString())
        } catch (e: Exception) {
            log.error("Error testing Sonarr connection", e)
            ConnectionTestResult(false, "❌ Error: ${e.message}", e.stackTraceToString())
        }
    }

    /**
     * Test Radarr connection
     */
    fun testRadarr(config: RadarrConfigDto): ConnectionTestResult {
        if (!config.enabled) {
            return ConnectionTestResult(false, "Radarr is disabled")
        }
        
        return try {
            val url = "${config.url.trimEnd('/')}/api/v3/system/status"
            
            val response = webClient.get()
                .uri(url)
                .header("X-Api-Key", config.apiKey)
                .retrieve()
                .toEntity(String::class.java)
                .block()
            
            if (response != null && response.statusCode.is2xxSuccessful) {
                ConnectionTestResult(true, "✅ Connected to Radarr successfully", response.body)
            } else {
                ConnectionTestResult(false, "❌ Unexpected response from Radarr: ${response?.statusCode}")
            }
        } catch (e: WebClientResponseException) {
            if (e.statusCode.value() == 401) {
                ConnectionTestResult(false, "❌ Authentication failed. Check API key.", e.message)
            } else {
                ConnectionTestResult(false, "❌ HTTP error: ${e.statusCode} - ${e.statusText}", e.message)
            }
        } catch (e: WebClientRequestException) {
            log.error("Network error testing Radarr connection", e)
            ConnectionTestResult(false, "❌ Connection error: ${e.message}. Check URL and network connectivity.", e.stackTraceToString())
        } catch (e: Exception) {
            log.error("Error testing Radarr connection", e)
            ConnectionTestResult(false, "❌ Error: ${e.message}", e.stackTraceToString())
        }
    }

    /**
     * Test Jellyfin connection
     */
    fun testJellyfin(config: JellyfinConfigDto): ConnectionTestResult {
        if (!config.enabled) {
            return ConnectionTestResult(false, "Jellyfin is disabled")
        }
        
        return try {
            val url = "${config.url.trimEnd('/')}/System/Info/Public"
            
            val response = webClient.get()
                .uri(url)
                .retrieve()
                .toEntity(String::class.java)
                .block()
            
            if (response != null && response.statusCode.is2xxSuccessful) {
                ConnectionTestResult(true, "✅ Connected to Jellyfin successfully", response.body)
            } else {
                ConnectionTestResult(false, "❌ Unexpected response from Jellyfin: ${response?.statusCode}")
            }
        } catch (e: WebClientRequestException) {
            log.error("Network error testing Jellyfin connection", e)
            ConnectionTestResult(false, "❌ Connection error: ${e.message}. Check URL and network connectivity.", e.stackTraceToString())
        } catch (e: Exception) {
            log.error("Error testing Jellyfin connection", e)
            ConnectionTestResult(false, "❌ Error: ${e.message}", e.stackTraceToString())
        }
    }

    /**
     * Test Emby connection
     */
    fun testEmby(config: EmbyConfigDto): ConnectionTestResult {
        if (!config.enabled) {
            return ConnectionTestResult(false, "Emby is disabled")
        }
        
        return try {
            val url = "${config.url.trimEnd('/')}/System/Info/Public"
            
            val response = webClient.get()
                .uri(url)
                .retrieve()
                .toEntity(String::class.java)
                .block()
            
            if (response != null && response.statusCode.is2xxSuccessful) {
                ConnectionTestResult(true, "✅ Connected to Emby successfully", response.body)
            } else {
                ConnectionTestResult(false, "❌ Unexpected response from Emby: ${response?.statusCode}")
            }
        } catch (e: WebClientRequestException) {
            log.error("Network error testing Emby connection", e)
            ConnectionTestResult(false, "❌ Connection error: ${e.message}. Check URL and network connectivity.", e.stackTraceToString())
        } catch (e: Exception) {
            log.error("Error testing Emby connection", e)
            ConnectionTestResult(false, "❌ Error: ${e.message}", e.stackTraceToString())
        }
    }

    /**
     * Test Jellyseerr connection
     */
    fun testJellyseerr(config: JellyseerrConfigDto): ConnectionTestResult {
        if (!config.enabled) {
            return ConnectionTestResult(false, "Jellyseerr is disabled")
        }
        
        return try {
            val url = "${config.url.trimEnd('/')}/api/v1/status"
            
            val response = webClient.get()
                .uri(url)
                .header("X-Api-Key", config.apiKey)
                .retrieve()
                .toEntity(String::class.java)
                .block()
            
            if (response != null && response.statusCode.is2xxSuccessful) {
                ConnectionTestResult(true, "✅ Connected to Jellyseerr successfully", response.body)
            } else {
                ConnectionTestResult(false, "❌ Unexpected response from Jellyseerr: ${response?.statusCode}")
            }
        } catch (e: WebClientResponseException) {
            if (e.statusCode.value() == 401) {
                ConnectionTestResult(false, "❌ Authentication failed. Check API key.", e.message)
            } else {
                ConnectionTestResult(false, "❌ HTTP error: ${e.statusCode} - ${e.statusText}", e.message)
            }
        } catch (e: WebClientRequestException) {
            log.error("Network error testing Jellyseerr connection", e)
            ConnectionTestResult(false, "❌ Connection error: ${e.message}. Check URL and network connectivity.", e.stackTraceToString())
        } catch (e: Exception) {
            log.error("Error testing Jellyseerr connection", e)
            ConnectionTestResult(false, "❌ Error: ${e.message}", e.stackTraceToString())
        }
    }

    /**
     * Test Jellystat connection
     */
    fun testJellystat(config: JellystatConfigDto): ConnectionTestResult {
        if (!config.enabled) {
            return ConnectionTestResult(false, "Jellystat is disabled")
        }
        
        return try {
            val url = "${config.url.trimEnd('/')}/api/getInfo"
            
            val response = webClient.get()
                .uri(url)
                .header("X-API-Token", config.apiKey)
                .retrieve()
                .toEntity(String::class.java)
                .block()
            
            if (response != null && response.statusCode.is2xxSuccessful) {
                ConnectionTestResult(true, "✅ Connected to Jellystat successfully", response.body)
            } else {
                ConnectionTestResult(false, "❌ Unexpected response from Jellystat: ${response?.statusCode}")
            }
        } catch (e: WebClientResponseException) {
            if (e.statusCode.value() == 401) {
                ConnectionTestResult(false, "❌ Authentication failed. Check API key.", e.message)
            } else {
                ConnectionTestResult(false, "❌ HTTP error: ${e.statusCode} - ${e.statusText}", e.message)
            }
        } catch (e: WebClientRequestException) {
            log.error("Network error testing Jellystat connection", e)
            ConnectionTestResult(false, "❌ Connection error: ${e.message}. Check URL and network connectivity.", e.stackTraceToString())
        } catch (e: Exception) {
            log.error("Error testing Jellystat connection", e)
            ConnectionTestResult(false, "❌ Error: ${e.message}", e.stackTraceToString())
        }
    }

    /**
     * Test Streamystats connection
     */
    fun testStreamystats(config: StreamystatsConfigDto): ConnectionTestResult {
        if (!config.enabled) {
            return ConnectionTestResult(false, "Streamystats is disabled")
        }
        
        return try {
            val url = "${config.url.trimEnd('/')}/api/getInfo"
            
            val response = webClient.get()
                .uri(url)
                .header("X-API-Token", config.apiKey)
                .retrieve()
                .toEntity(String::class.java)
                .block()
            
            if (response != null && response.statusCode.is2xxSuccessful) {
                ConnectionTestResult(true, "✅ Connected to Streamystats successfully", response.body)
            } else {
                ConnectionTestResult(false, "❌ Unexpected response from Streamystats: ${response?.statusCode}")
            }
        } catch (e: WebClientResponseException) {
            if (e.statusCode.value() == 401) {
                ConnectionTestResult(false, "❌ Authentication failed. Check API key.", e.message)
            } else {
                ConnectionTestResult(false, "❌ HTTP error: ${e.statusCode} - ${e.statusText}", e.message)
            }
        } catch (e: WebClientRequestException) {
            log.error("Network error testing Streamystats connection", e)
            ConnectionTestResult(false, "❌ Connection error: ${e.message}. Check URL and network connectivity.", e.stackTraceToString())
        } catch (e: Exception) {
            log.error("Error testing Streamystats connection", e)
            ConnectionTestResult(false, "❌ Error: ${e.message}", e.stackTraceToString())
        }
    }

    /**
     * Test Bazarr connection
     */
    fun testBazarr(config: BazarrConfigDto): ConnectionTestResult {
        if (!config.enabled) {
            return ConnectionTestResult(false, "Bazarr is disabled")
        }
        
        return try {
            val url = "${config.url.trimEnd('/')}/api/system/status"
            
            val response = webClient.get()
                .uri(url)
                .header("X-Api-Key", config.apiKey)
                .retrieve()
                .toEntity(String::class.java)
                .block()
            
            if (response != null && response.statusCode.is2xxSuccessful) {
                ConnectionTestResult(true, "✅ Connected to Bazarr successfully", response.body)
            } else {
                ConnectionTestResult(false, "❌ Unexpected response from Bazarr: ${response?.statusCode}")
            }
        } catch (e: WebClientResponseException) {
            if (e.statusCode.value() == 401) {
                ConnectionTestResult(false, "❌ Authentication failed. Check API key.", e.message)
            } else {
                ConnectionTestResult(false, "❌ HTTP error: ${e.statusCode} - ${e.statusText}", e.message)
            }
        } catch (e: WebClientRequestException) {
            log.error("Network error testing Bazarr connection", e)
            ConnectionTestResult(false, "❌ Connection error: ${e.message}. Check URL and network connectivity.", e.stackTraceToString())
        } catch (e: Exception) {
            log.error("Error testing Bazarr connection", e)
            ConnectionTestResult(false, "❌ Error: ${e.message}", e.stackTraceToString())
        }
    }
}
