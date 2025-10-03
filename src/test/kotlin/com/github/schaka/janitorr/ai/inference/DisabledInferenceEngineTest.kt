package com.github.schaka.janitorr.ai.inference

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse

/**
 * Tests for DisabledInferenceEngine to ensure it properly rejects operations
 * when AI features are disabled.
 */
internal class DisabledInferenceEngineTest {

    private val engine = DisabledInferenceEngine()

    @Test
    fun testIsReadyReturnsFalse() {
        assertFalse(engine.isReady(), "Disabled inference engine should never be ready")
    }

    @Test
    fun testGetRecommendationThrowsException() {
        assertThrows<UnsupportedOperationException> {
            engine.getRecommendation("test-media-id")
        }
    }

    @Test
    fun testBatchScoreThrowsException() {
        assertThrows<UnsupportedOperationException> {
            runBlocking {
                engine.batchScore(listOf("id1", "id2", "id3"))
            }
        }
    }

    @Test
    fun testGetConfidenceThrowsException() {
        assertThrows<UnsupportedOperationException> {
            engine.getConfidence("test-media-id")
        }
    }
}
