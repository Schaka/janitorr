package com.github.schaka.janitorr.stats

import com.github.schaka.janitorr.JanitorrApplication
import com.github.schaka.janitorr.stats.janitorrstats.JanitorrStats
import com.github.schaka.janitorr.stats.janitorrstats.JanitorrStatsNoOpService
import com.github.schaka.janitorr.stats.janitorrstats.JanitorrStatsRestService
import com.github.schaka.janitorr.stats.janitorrstats.JanitorrStatsService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

/**
 * Makes sure the 'weird' combinations of StatsServices don't conflcit with each other by creating overlapping beans of the same interface.
 */
@SpringBootTest(classes = [JanitorrApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class StatsContextTest {

    @Autowired
    @Stats
    lateinit var statsService: StatsService

    @Autowired
    @JanitorrStats
    lateinit var janitorrStatsService: JanitorrStatsService

    @Test
    fun `context loads with all stats disabled`() {
        assertThat(statsService).isInstanceOf(StatsNoOpService::class.java)
        assertThat(janitorrStatsService).isInstanceOf(JanitorrStatsNoOpService::class.java)
    }
}

@SpringBootTest(classes = [JanitorrApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = ["clients.janitorr-stats.enabled=true"])
class StatsContextJanitorrStatsOnlyTest {

    @Autowired
    @Stats
    lateinit var statsService: StatsService

    @Autowired
    @JanitorrStats
    lateinit var janitorrStatsService: JanitorrStatsService

    @Test
    fun `context loads with only janitorr-stats enabled`() {
        assertThat(janitorrStatsService).isInstanceOf(JanitorrStatsRestService::class.java)
        // when only janitorr-stats is enabled, statsService delegates to it
        assertThat(statsService).isSameAs(janitorrStatsService)
    }
}
