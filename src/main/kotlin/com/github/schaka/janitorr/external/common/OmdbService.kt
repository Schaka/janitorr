package com.github.schaka.janitorr.external.common

import com.github.schaka.janitorr.external.omdb.OmdbClient
import com.github.schaka.janitorr.servarr.LibraryItem
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "external-apis.omdb", name = ["enabled"], havingValue = "true")
class OmdbService(
    private val omdbClient: OmdbClient,
    private val externalDataProperties: ExternalDataProperties
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    private val apiKey get() = externalDataProperties.omdb.apiKey

    fun getRating(item: LibraryItem): Double? {
        return try {
            item.imdbId?.let { imdbId ->
                val response = omdbClient.getByImdbId(imdbId, apiKey)
                if (response.Response == "True") {
                    response.imdbRating?.toDoubleOrNull()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            log.debug("Failed to get OMDb rating for item: ${item.filePath}", e)
            null
        }
    }

    fun getMetascore(item: LibraryItem): Int? {
        return try {
            item.imdbId?.let { imdbId ->
                val response = omdbClient.getByImdbId(imdbId, apiKey)
                if (response.Response == "True") {
                    response.Metascore?.toIntOrNull()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            log.debug("Failed to get Metascore for item: ${item.filePath}", e)
            null
        }
    }

    fun hasAwards(item: LibraryItem): Boolean {
        return try {
            item.imdbId?.let { imdbId ->
                val response = omdbClient.getByImdbId(imdbId, apiKey)
                if (response.Response == "True") {
                    val awards = response.Awards ?: ""
                    awards.contains("Oscar", ignoreCase = true) ||
                            awards.contains("Emmy", ignoreCase = true) ||
                            awards.contains("Golden Globe", ignoreCase = true) ||
                            awards.contains("won", ignoreCase = true)
                } else {
                    false
                }
            } ?: false
        } catch (e: Exception) {
            log.debug("Failed to check awards for item: ${item.filePath}", e)
            false
        }
    }
}
