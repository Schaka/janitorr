package com.github.schaka.janitorr.servarr.sonarr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.data_structures.Tag
import com.github.schaka.janitorr.servarr.history.HistoryResponse
import com.github.schaka.janitorr.servarr.quality_profile.QualityProfile
import com.github.schaka.janitorr.servarr.sonarr.episodes.EpisodeResponse
import com.github.schaka.janitorr.servarr.sonarr.series.SeriesPayload
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.exists

@RegisterReflectionForBinding(classes = [QualityProfile::class, Tag::class, SeriesPayload::class, HistoryResponse::class, EpisodeResponse::class])
@Service
class SonarrRestService(

        val sonarrClient: SonarrClient,

        val fileSystemProperties: FileSystemProperties,

        val applicationProperties: ApplicationProperties,

        val sonarrProperties: SonarrProperties,

        var upgradesAllowed: Boolean = false,

        var keepTag: Tag = Tag(Integer.MIN_VALUE, "Not_Set")

) : ServarrService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        const val CACHE_NAME = "sonarr-cache"
    }
    @PostConstruct
    override fun postConstruct() {
        if (!sonarrProperties.enabled) {
            return
        }

        upgradesAllowed = sonarrClient.getAllQualityProfiles().any { it.items.isNotEmpty() && it.upgradeAllowed }
        keepTag = sonarrClient.getAllTags().firstOrNull { it.label == applicationProperties.exclusionTag } ?: keepTag
    }

    @Cacheable(CACHE_NAME)
    override fun getEntries(): List<LibraryItem> {
        val allTags = sonarrClient.getAllTags()

        val history = sonarrClient.getAllSeries()
                .filter { !it.tags.contains(keepTag.id) }
                .flatMap { series ->
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
                                            imdbId = series.imdbId,
                                            tags = allTags.filter { tag -> series.tags.contains(tag.id) }.map { tag -> tag.label }
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
                log.info("Deleting {} [{}] - All seasons were unused", show.title, show.id)
            }
        }
    }
}