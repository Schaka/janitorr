package com.github.schaka.janitorr.cleanup

import com.github.schaka.janitorr.config.ApplicationProperties
import com.github.schaka.janitorr.servarr.bazarr.BazarrRestService
import com.github.schaka.janitorr.servarr.radarr.RadarrRestService
import com.github.schaka.janitorr.servarr.sonarr.SonarrRestService
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.system.exitProcess

@Service
class CleanupRunner(
    val applicationProperties: ApplicationProperties,
    val cacheManager: CacheManager,
    val cleanupSchedules: List<Schedule>
) {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @CacheEvict(cacheNames = [SonarrRestService.CACHE_NAME, RadarrRestService.CACHE_NAME, BazarrRestService.CACHE_NAME_TV, BazarrRestService.CACHE_NAME_MOVIES], allEntries = true, beforeInvocation = false)
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun runSchedules() {

        for (schedule in cleanupSchedules) {
            cleanCachesByName(SonarrRestService.CACHE_NAME, RadarrRestService.CACHE_NAME)
            schedule.runSchedule()
        }

        if (applicationProperties.runOnce) {
            log.info("Run once enabled, all cleanups run. Terminating process.")
            exitProcess(0)
        }
    }

    private fun cleanCachesByName(vararg names: String) {
        for (name in names) {
            val cache = cacheManager.getCache(name)
            cache?.clear()
        }
    }
}