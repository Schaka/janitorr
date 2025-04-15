package com.github.schaka.janitorr.mediaserver

import com.github.schaka.janitorr.stats.streamystats.StreamystatsClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import kotlin.test.Ignore

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = arrayOf(
        """
    spring.profiles.active=test
    clients.streamystats.enabled=true
    clients.streamystats.url=http://streamystats:3000
    clients.streamystats.username=Janitorr
    clients.streamystats.password=janitorr
  """
    )
)
/**
 * For local testing only, can't be run using CI
 */
class StreamystatsLocalTest {

    @Autowired
    private lateinit var streamystatsClient: StreamystatsClient

    @Test
    @Ignore
    fun testClient() {
        streamystatsClient.getRequests("383b570dc0e3e878ad30102414dfeaed")
    }
}