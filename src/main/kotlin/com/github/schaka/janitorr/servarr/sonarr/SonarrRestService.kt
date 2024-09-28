package com.github.schaka.janitorr.servarr.sonarr

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.config.FileSystemProperties
import com.github.schaka.janitorr.servarr.HistorySort
import com.github.schaka.janitorr.servarr.HistorySort.MOST_RECENT
import com.github.schaka.janitorr.servarr.HistorySort.OLDEST
import com.github.schaka.janitorr.servarr.LibraryItem
import com.github.schaka.janitorr.servarr.ServarrService
import com.github.schaka.janitorr.servarr.data_structures.Tag
import com.github.schaka.janitorr.servarr.history.HistoryResponse
import com.github.schaka.janitorr.servarr.history.SonarrHistoryResponse
import com.github.schaka.janitorr.servarr.quality_profile.QualityProfile
import com.github.schaka.janitorr.servarr.sonarr.episodes.EpisodeResponse
import com.github.schaka.janitorr.servarr.sonarr.series.Season
import com.github.schaka.janitorr.servarr.sonarr.series.SeriesPayload
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.exists

@Service
@RegisterReflectionForBinding(classes = [QualityProfile::class, Tag::class, SeriesPayload::class, HistoryResponse::class, SonarrHistoryResponse::class, EpisodeResponse::class])
class SonarrRestService(

        val sonarrClient: SonarrClient,

        val fileSystemProperties: FileSystemProperties,

        val applicationProperties: ApplicationProperties,

        val sonarrProperties: SonarrProperties,

        var upgradesAllowed: Boolean = false,

        var keepTag: Tag = Tag(Integer.MIN_VALUE, "Not_Set"),

        var episodeTag: Tag = Tag(Integer.MIN_VALUE, "Not_Set"),

        var historySort: HistorySort = OLDEST

) : ServarrService {

    init {
        if (sonarrProperties.enabled) {
            upgradesAllowed = sonarrClient.getAllQualityProfiles().any { it.items.isNotEmpty() && it.upgradeAllowed }
            historySort = sonarrProperties.determineAgeBy ?: if (upgradesAllowed) MOST_RECENT else OLDEST
            keepTag = sonarrClient.getAllTags().firstOrNull { it.label == applicationProperties.exclusionTag } ?: keepTag
            episodeTag = sonarrClient.getAllTags().firstOrNull { it.label == applicationProperties.episodeDeletion.tag } ?: episodeTag
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
        const val CACHE_NAME = "sonarr-cache"
    }

    @Cacheable(CACHE_NAME)
    override fun getEntries(): List<LibraryItem> {
        val allTags = sonarrClient.getAllTags()

        if (applicationProperties.wholeTvShow) {
            return getEntriesPerShow(allTags)
        }

        return getEntriesPerSeason(allTags)
    }

    // this function is a bit hacky, but it makes the code much less convoluted and way easier to maintain
    // the regular season based function already grabs all seasons with correct files, paths etc
    // it's much easier to update all seasons with the date information of the whole show
    private fun getEntriesPerShow(allTags: List<Tag>): List<LibraryItem> {
       return getEntriesPerSeason(allTags).map { libItem ->
                val latestEpisode = sonarrClient.getHistory(libItem.id) // returns history of EVERY episode
                    .filter { it.eventType == "downloadFolderImported" && it.data.droppedPath != null }
                    .sortedWith(byHistoryMostRecent())
                    .first()

                // just fake the date so all seasons are treated as being the same age
                val latestDate = parseDate(latestEpisode.date)
                log.trace("Treat TV show as a whole - overwriting season import date - IMDB: ${libItem.imdbId} imported at $latestDate")
                libItem.copy(importedDate = latestDate, lastSeen = latestDate)
            }
    }

    private fun getEntriesPerSeason(allTags: List<Tag>): List<LibraryItem> {
        val history = sonarrClient.getAllSeries()
            .filter { !it.tags.contains(keepTag.id) }
            .filter { !it.tags.contains(episodeTag.id) }
            .flatMap { series ->
                series.seasons.map { season ->
                    sonarrClient.getHistory(series.id, season.seasonNumber)
                        .filter { it.eventType == "downloadFolderImported" && it.data.droppedPath != null }
                        .map{ mapItem(it, series, allTags, season) }
                        .sortedWith(byDate(historySort))
                        .firstOrNull()
                }
            }.filterNotNull()

        // history may be outdated, we need to find the current path, as it currently stands in the library
        return history.map {
            val episodeResponses = sonarrClient.getAllEpisodes(it.id, it.season!!)

            // If no files are available in this season, don't consider it for deletion, we only care about entries that represent real files
            if (episodeResponses.none(EpisodeResponse::hasFile)) {
                return@map null
            }

            val episodeResponse = episodeResponses.firstOrNull { ep -> ep.hasFile && ep.episodeFileId != null }
            if (episodeResponse == null) {
                return@map it
            }

            val fileResponse = sonarrClient.getEpisodeFile(episodeResponse.episodeFileId!!)

            it.copy(filePath = fileResponse.path!!)
        }.filterNotNull()
    }

    private fun mapItem(it: HistoryResponse, series: SeriesPayload, allTags: List<Tag>, season: Season? = null): LibraryItem {
        return LibraryItem(
            series.id,
            parseDate(it.date),
            it.data.droppedPath!!,
            it.data.importedPath!!,
            series.path,
            series.rootFolderPath,
            it.data.importedPath, //points to the file
            season = season?.seasonNumber,
            tvdbId = series.tvdbId,
            imdbId = series.imdbId,
            tags = allTags.filter { tag -> series.tags.contains(tag.id) }.map { tag -> tag.label }
        )
    }

    private fun parseDate(date: String): LocalDateTime {
        return LocalDateTime.parse(date.substring(0, date.length - 1))
    }

    override fun removeEntries(items: List<LibraryItem>) {

        if (applicationProperties.wholeTvShow) {

            // if tv shows are treated as a whole, we don't need to delete every season, we can just delete the whole show
            val affectedShows = items.filter {
                !(applicationProperties.wholeShowSeedingCheck &&
                        fileSystemProperties.access &&
                        fileSystemProperties.validateSeeding &&
                        Path.of(it.originalPath).exists())
            }
            .map { it.id }
            .distinct()
            .map { sonarrClient.getSeries(it) }

            log.info("Treating TV shows as a whole - preparing to delete ${affectedShows.size} shows")
            for (show in affectedShows) {
                // no seeding check, we just delete everything - checking every single file for seeding isn't feasible
                if (!applicationProperties.dryRun) {
                    deleteShow(show)
                }
                log.info("Deleting ${show.title} [${show.id}}]")
            }
        }
        else {
            removeBySeason(items)
        }
    }

    /**
     * Deletes entire TV show (or its associated files)
     */
    private fun deleteShow(show: SeriesPayload) {
        if (sonarrProperties.deleteEmptyShows) {
            sonarrClient.deleteSeries(show.id, true)
            return
        }

        // Unmonitor everything
        unmonitorSeasons(show.id, *show.seasons.map(Season::seasonNumber).toIntArray())

        // then delete each season's episode files
        for (season in show.seasons) {
            val episodes = sonarrClient.getAllEpisodes(show.id, season.seasonNumber)
            for (episode in episodes) {
                if (episode.episodeFileId != null && episode.episodeFileId != 0) {
                    sonarrClient.deleteEpisodeFile(episode.episodeFileId)
                    log.info("Deleting {} - episode {} ({}) of season {}", show.path, episode.episodeNumber, episode.episodeFileId, episode.seasonNumber)
                }
            }
        }
    }

    /**
     * Removes a season's files if that season is in the relevant items.
     */
    private fun removeBySeason(items: List<LibraryItem>) {
        // we are always treating seasons as a whole, even if technically episodes could be handled individually
        for (item in items) {

            if (fileSystemProperties.access && fileSystemProperties.validateSeeding && Path.of(item.originalPath).exists()) {
                log.info("Can't delete season [still seeding - file exists] ({}), id: {}, imdb: {}", item.originalPath, item.id, item.imdbId)
                item.seeding = true
                continue
            }

            val episodes = sonarrClient.getAllEpisodes(item.id, item.season!!)
            for (episode in episodes) {
                if (episode.episodeFileId != null && episode.episodeFileId != 0) {
                    if (!applicationProperties.dryRun) {
                        sonarrClient.deleteEpisodeFile(episode.episodeFileId)
                    }
                    log.info("Deleting {} - episode {} ({}) of season {}", item.parentPath, episode.episodeNumber, episode.episodeFileId, episode.seasonNumber)
                }
            }

            if (!applicationProperties.dryRun) {
                unmonitorSeasons(item.id, item.season)
            }
        }

        // We could be more efficient and do this when grabbing the series in unmonitorSeason anyway, but more clear cut code should be better here
        val affectedShows = items.map { it.id }.distinct().map { sonarrClient.getSeries(it) }
        deleteEmptyShows(affectedShows)
    }

    private fun unmonitorSeasons(seriesId: Int, vararg seasonNumbers: Int) {
        val series = sonarrClient.getSeries(seriesId)
        series.seasons
                .filter { seasonNumbers.contains(it.seasonNumber) }
                .forEach { seasonToEdit ->
                    val isMonitored = seasonToEdit.monitored
                    seasonToEdit.monitored = false

                    if (isMonitored) {
                        log.info("Unmonitoring ${series.title} - season ${seasonToEdit.seasonNumber}")
                    }
                }

        sonarrClient.updateSeries(seriesId, series)
    }

    // If all seasons are unmonitored or don't have any files, we delete them
    // If the latest season is monitored, we have to assume the intention is to grab future episodes on release and not delete anything
    private fun deleteEmptyShows(affectedShows: List<SeriesPayload>) {

        if (!sonarrProperties.deleteEmptyShows) {
            log.info("Feature turned off - not deleting any TV shows without files or monitoring")
            return
        }

        // Do nothing during dry-run
        if (applicationProperties.dryRun) {
            log.info("Dry run - not deleting any TV shows without files or monitoring")
            return
        }

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
                log.info("Deleting ${show.title} [${show.id}] - All seasons were unused")
            }
        }
    }
}