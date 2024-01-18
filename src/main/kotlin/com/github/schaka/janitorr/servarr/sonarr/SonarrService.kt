package com.github.schaka.janitorr.servarr.sonarr

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.FileSystemProperties
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@Service
class SonarrService(

        val sonarrClient: SonarrClient,

        val fileSystemProperties: FileSystemProperties,

        val applicationProperties: ApplicationProperties,

        @Sonarr
        val client: RestTemplate,

        var upgradesAllowed: Boolean = false

) : ServarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @PostConstruct
    fun postConstruct() {
        upgradesAllowed = sonarrClient.getAllQualityProfiles().any { it.items.isNotEmpty() && it.upgradeAllowed }
    }

    override fun getEntries(): List<LibraryItem> {
        return sonarrClient.getAllSeries().flatMap { series ->
            series.seasons.map { season ->
                sonarrClient.getHistory(series.id, season.seasonNumber)
                        .filter { it.eventType == "downloadFolderImported" && it.data.droppedPath != null }
                        .map {

                            var seasonPath = series.path;
                            if (fileSystemProperties.access) {
                                // if filesystem access is available, we create an entry for every season
                                // TODO: use /config/naming endpoint to get season naming scheme
                                seasonPath = series.path + "/Season " + season.seasonNumber.toString().padStart(2, '0')
                            }

                            LibraryItem(
                                    series.id,
                                    LocalDateTime.parse(it.date.substring(0, it.date.length - 1)),
                                    it.data.droppedPath!!,
                                    it.data.importedPath!!,
                                    series.path,
                                    series.rootFolderPath,
                                    it.data.importedPath,
                                    season = season.seasonNumber,
                                    tvdbId = series.tvdbId,
                                    imdbId = series.imdbId
                            )
                        }
                        .sortedWith ( byDate(upgradesAllowed) )
                        .firstOrNull()
            }
        }.filterNotNull()
    }

    override fun removeEntries(items: List<LibraryItem>) {
        // we are always treating seasons as a whole, even if technically episodes could be handled individually
        for (item in items) {
            val episodes = sonarrClient.getAllEpisodes(item.id, item.season!!)
            for (episode in episodes) {
                if (episode.episodeFileId != null) {
                    if (!applicationProperties.dryRun) {
                        sonarrClient.deleteEpisodeFile(episode.episodeFileId)
                    } else {
                        log.info("Deleting {} - episode {} ({}) from item {}", item.fullPath, episode.episodeNumber, episode.episodeFileId, episode.seasonNumber)
                    }
                }
            }
            if (!applicationProperties.dryRun) {
                unmonitorSeason(item.id, item.season)
            }
        }

        // TODO: Clean up entire show, if all seasons are unmonitored
        // Also requires support for deletion in Jellyfin
    }

    private fun unmonitorSeason(seriesId: Int, seasonNumber: Int) {
        val series = sonarrClient.getSeries(seriesId)
        val seasonToEdit = series.seasons.firstOrNull { it.seasonNumber == seasonNumber }
        seasonToEdit?.monitored = false
        sonarrClient.updateSeries(seriesId, series)
    }
}