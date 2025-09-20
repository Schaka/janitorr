package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.servarr.bazarr.BazarrRestService
import com.github.schaka.janitorr.servarr.data_structures.Tag
import com.github.schaka.janitorr.servarr.history.HistoryResponse
import com.github.schaka.janitorr.servarr.radarr.RadarrRestService
import com.github.schaka.janitorr.servarr.sonarr.SonarrClient
import com.github.schaka.janitorr.servarr.sonarr.SonarrProperties
import com.github.schaka.janitorr.servarr.sonarr.SonarrRestService
import com.github.schaka.janitorr.servarr.sonarr.episodes.EpisodeResponse
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * This class works differently than the other schedules because it covers one special use case.
 * TV shows only (mostly daily episodes), regarding only the latest season or x amount of latest episodes.
 */
@Profile("!leyden")
@Service
@RegisterReflectionForBinding(classes = [Tag::class,HistoryResponse::class, EpisodeResponse::class])
class WeeklyEpisodeCleanupSchedule(
        val applicationProperties: ApplicationProperties,
        val sonarrProperties: SonarrProperties,
        val sonarrClient: SonarrClient,
        val runOnce: RunOnce,

        var episodeTag: Tag = Tag(Integer.MIN_VALUE, "Not_Set")
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    init {
        if (sonarrProperties.enabled && !applicationProperties.trainingRun) {
            episodeTag = sonarrClient.getAllTags().firstOrNull { it.label == applicationProperties.episodeDeletion.tag } ?: episodeTag
        }
    }

    // run every hour
    @CacheEvict(cacheNames = [SonarrRestService.CACHE_NAME, RadarrRestService.CACHE_NAME, BazarrRestService.CACHE_NAME_TV, BazarrRestService.CACHE_NAME_MOVIES])
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun runSchedule() {

        if (!applicationProperties.episodeDeletion.enabled) {
            log.info("Episode based cleanup disabled, do nothing")
            runOnce.hasWeeklyEpisodeCleanupRun = true
            return
        }

        val today = LocalDateTime.now()
        val series = sonarrClient.getAllSeries().filter { it.tags.contains(episodeTag.id) }

        for (show in series) {
            val latestSeason = show.seasons.maxBy { season -> season.seasonNumber }
            val episodes = sonarrClient.getAllEpisodes(show.id, latestSeason.seasonNumber)
                .filter { it.airDate != null }
                .filter { LocalDate.parse(it.airDate!!) <= today.toLocalDate() }
                .toMutableList()

            val episodesHistory = sonarrClient.getHistory(show.id, latestSeason.seasonNumber)
                    .sortedBy { parseDate(it.date)}
                    .distinctBy { it.episodeId }

            log.info("Deleting single episodes of ${show.title}")

            // Delete by age
            for (episodeHistory in episodesHistory) {
                val episode = episodes.first{ it.seriesId == episodeHistory.seriesId && it.id == episodeHistory.episodeId }
                val grabDate = parseDate(episodeHistory.date)
                if (grabDate + applicationProperties.episodeDeletion.maxAge <= today) {
                    log.trace("Deleting episode ${episode.episodeNumber} of ${show.title} S${latestSeason.seasonNumber} because of its age")

                    if (episode.episodeFileId != null && episode.episodeFileId != 0) {
                        if (!applicationProperties.dryRun) {
                            sonarrClient.deleteEpisodeFile(episode.episodeFileId)
                            episodes.remove(episode)
                        }
                    }
                }
            }

            // Delete by count
            if (episodes.size > applicationProperties.episodeDeletion.maxEpisodes) {
                val leftoverEpisodes = episodes.sortedByDescending { it.episodeNumber }.take(applicationProperties.episodeDeletion.maxEpisodes)
                episodes.removeAll(leftoverEpisodes) // remove the most recent episodes from the list, as we want to keep those

                for (episode in episodes) {
                    log.trace("Deleting episode ${episode.episodeNumber} of ${show.title} S${latestSeason.seasonNumber} because there are too many episodes")

                    if (episode.episodeFileId != null && episode.episodeFileId != 0) {
                        if (!applicationProperties.dryRun) {
                            sonarrClient.deleteEpisodeFile(episode.episodeFileId)
                        }
                    }
                }
            }
        }

        runOnce.hasWeeklyEpisodeCleanupRun = true
    }

    private fun parseDate(date: String): LocalDateTime {
        return LocalDateTime.parse(date.substring(0, date.length - 1))
    }

}