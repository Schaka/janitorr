package com.github.schaka.janitorr.servarr.sonarr

import com.github.schaka.janitorr.ApplicationProperties
import com.github.schaka.janitorr.FileSystemProperties
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.radarr.RadarrService
import com.github.schaka.janitorr.servarr.sonarr.series.SeriesPayload
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.exists

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
        val history = sonarrClient.getAllSeries().flatMap { series ->
            series.seasons.map { season ->
                sonarrClient.getHistory(series.id, season.seasonNumber)
                        .filter { it.eventType == "downloadFolderImported" && it.data.droppedPath != null }
                        .map {
                            LibraryItem(
                                    series.id,
                                    LocalDateTime.parse(it.date.substring(0, it.date.length - 1)),
                                    it.data.droppedPath!!,
                                    it.data.importedPath!!,
                                    series.path,
                                    series.rootFolderPath,
                                    it.data.importedPath, //points to the file
                                    season = season.seasonNumber,
                                    tvdbId = series.tvdbId,
                                    imdbId = series.imdbId
                            )
                        }
                        .sortedWith(byDate(upgradesAllowed))
                        .firstOrNull()
            }
        }.filterNotNull()

        // history may be outdated, we need to find the current path, as it currently stands in the library
        return history.map {
            val episodeResponse = sonarrClient.getAllEpisodes(it.id, it.season!!).firstOrNull { ep -> ep.hasFile && ep.episodeFileId != null }
            if (episodeResponse == null) {
                return@map it
            }

            val fileResponse = sonarrClient.getEpisodeFile(episodeResponse.episodeFileId!!)

            it.copy(filePath = fileResponse.path!!)
        }
    }

    override fun removeEntries(items: List<LibraryItem>) {
        // we are always treating seasons as a whole, even if technically episodes could be handled individually
        for (item in items) {

            if (fileSystemProperties.access && Path.of(item.originalPath).exists()) {
                log.info("Can't delete season [still seeding - file exists] ({}), id: {}, imdb: {}", item.originalPath, item.id, item.imdbId)
                item.seeding = true
                continue
            }

            val episodes = sonarrClient.getAllEpisodes(item.id, item.season!!)
            for (episode in episodes) {
                if (episode.episodeFileId != null && episode.episodeFileId != 0) {
                    if (!applicationProperties.dryRun) {
                        sonarrClient.deleteEpisodeFile(episode.episodeFileId)
                        log.info("Deleting {} - episode {} ({}) of season {}", item.parentPath, episode.episodeNumber, episode.episodeFileId, episode.seasonNumber)
                    } else {
                        log.info("Deleting {} - episode {} ({}) of season {}", item.parentPath, episode.episodeNumber, episode.episodeFileId, episode.seasonNumber)
                    }
                }
            }

            if (!applicationProperties.dryRun) {
                unmonitorSeason(item.id, item.season)
            }
        }

        // We could be more efficient and do this when grabbing the series in unmonitorSeason anyway, but more clear cut code should be better here
        val affectedShows = items.map { it.id }.distinct().map { sonarrClient.getSeries(it) }
        deleteEmptyShows(affectedShows)

    }

    private fun unmonitorSeason(seriesId: Int, seasonNumber: Int) {
        val series = sonarrClient.getSeries(seriesId)
        val seasonToEdit = series.seasons.firstOrNull { it.seasonNumber == seasonNumber }
        val isMonitored = seasonToEdit?.monitored
        seasonToEdit?.monitored = false
        sonarrClient.updateSeries(seriesId, series)

        if (isMonitored == true) {
            log.info("Unmonitoring {} - season {}", series.title, seasonToEdit.seasonNumber)
        }
    }

    // If all seasons are unmonitored or don't have any files, we delete them
    // If the latest season is monitored, we have to assume the intention is to grab future episodes on release and not delete anything
    private fun deleteEmptyShows(affectedShows: List<SeriesPayload>) {

        for (show in affectedShows) {
            val latestSeason = show.seasons.maxBy { it.seasonNumber }
            if (latestSeason.monitored) {
                continue
            }

            val allUnmonitored = show.seasons.all { !it.monitored }
            if (!allUnmonitored) {
                continue
            }

            val noFiles = show.seasons.all {
                val filesForSeason = sonarrClient.getAllEpisodes(show.id, it.seasonNumber)
                filesForSeason.none { ep -> ep.hasFile }
            }

            if (noFiles) {
                sonarrClient.deleteSeries(show.id, true)
            }
        }
    }
}