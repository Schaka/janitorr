package com.github.schaka.janitorr.rules.engine

import com.github.schaka.janitorr.rules.model.*
import com.github.schaka.janitorr.servarr.LibraryItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for RuleEngineService
 */
internal class RuleEngineServiceTest {

    private lateinit var conditionEvaluator: ConditionEvaluator
    private lateinit var actionExecutor: ActionExecutor
    private lateinit var ruleEngineService: RuleEngineService
    private lateinit var mediaItem: LibraryItem

    @BeforeEach
    fun setup() {
        conditionEvaluator = mockk()
        actionExecutor = mockk(relaxed = true)
        ruleEngineService = RuleEngineService(conditionEvaluator, actionExecutor)
        
        mediaItem = LibraryItem(
            id = 1,
            importedDate = LocalDateTime.now().minusDays(60),
            originalPath = "/media/movies/test.mkv",
            libraryPath = "/media/movies/test.mkv",
            parentPath = "/media/movies",
            rootFolderPath = "/media",
            filePath = "/media/movies/test.mkv",
            imdbId = "tt1234567",
            tags = listOf("test")
        )
    }

    @Test
    fun testEvaluateRuleWithAndOperator() {
        val condition1 = AgeCondition(ComparisonOperator.GREATER_THAN, 30)
        val condition2 = TagCondition(ComparisonOperator.CONTAINS, "test")
        
        val rule = CustomRule(
            id = "test-rule",
            name = "Test Rule",
            enabled = true,
            conditions = listOf(condition1, condition2),
            actions = listOf(LogAction(message = "Test")),
            logicOperator = LogicOperator.AND
        )

        every { conditionEvaluator.evaluate(condition1, mediaItem) } returns true
        every { conditionEvaluator.evaluate(condition2, mediaItem) } returns true

        val result = ruleEngineService.evaluateRule(rule, mediaItem)
        assertTrue(result, "Rule with all conditions true should match")
    }

    @Test
    fun testEvaluateRuleWithAndOperatorOneFails() {
        val condition1 = AgeCondition(ComparisonOperator.GREATER_THAN, 30)
        val condition2 = TagCondition(ComparisonOperator.CONTAINS, "nonexistent")
        
        val rule = CustomRule(
            id = "test-rule",
            name = "Test Rule",
            enabled = true,
            conditions = listOf(condition1, condition2),
            actions = listOf(LogAction(message = "Test")),
            logicOperator = LogicOperator.AND
        )

        every { conditionEvaluator.evaluate(condition1, mediaItem) } returns true
        every { conditionEvaluator.evaluate(condition2, mediaItem) } returns false

        val result = ruleEngineService.evaluateRule(rule, mediaItem)
        assertFalse(result, "Rule with one failing condition and AND operator should not match")
    }

    @Test
    fun testEvaluateRuleWithOrOperator() {
        val condition1 = AgeCondition(ComparisonOperator.GREATER_THAN, 30)
        val condition2 = TagCondition(ComparisonOperator.CONTAINS, "nonexistent")
        
        val rule = CustomRule(
            id = "test-rule",
            name = "Test Rule",
            enabled = true,
            conditions = listOf(condition1, condition2),
            actions = listOf(LogAction(message = "Test")),
            logicOperator = LogicOperator.OR
        )

        every { conditionEvaluator.evaluate(condition1, mediaItem) } returns true
        every { conditionEvaluator.evaluate(condition2, mediaItem) } returns false

        val result = ruleEngineService.evaluateRule(rule, mediaItem)
        assertTrue(result, "Rule with one passing condition and OR operator should match")
    }

    @Test
    fun testEvaluateDisabledRule() {
        val condition = AgeCondition(ComparisonOperator.GREATER_THAN, 30)
        
        val rule = CustomRule(
            id = "test-rule",
            name = "Test Rule",
            enabled = false,
            conditions = listOf(condition),
            actions = listOf(LogAction(message = "Test")),
            logicOperator = LogicOperator.AND
        )

        val result = ruleEngineService.evaluateRule(rule, mediaItem)
        assertFalse(result, "Disabled rule should not match")
    }

    @Test
    fun testValidateRuleSuccess() {
        val rule = CustomRule(
            id = "test-rule",
            name = "Valid Rule",
            enabled = true,
            conditions = listOf(AgeCondition(ComparisonOperator.GREATER_THAN, 30)),
            actions = listOf(LogAction(message = "Test")),
            logicOperator = LogicOperator.AND
        )

        val result = ruleEngineService.validateRule(rule)
        assertTrue(result.isValid, "Valid rule should pass validation")
    }

    @Test
    fun testValidateRuleNoConditions() {
        val rule = CustomRule(
            id = "test-rule",
            name = "Invalid Rule",
            enabled = true,
            conditions = emptyList(),
            actions = listOf(LogAction(message = "Test")),
            logicOperator = LogicOperator.AND
        )

        val result = ruleEngineService.validateRule(rule)
        assertFalse(result.isValid, "Rule without conditions should fail validation")
    }

    @Test
    fun testValidateRuleNoActions() {
        val rule = CustomRule(
            id = "test-rule",
            name = "Invalid Rule",
            enabled = true,
            conditions = listOf(AgeCondition(ComparisonOperator.GREATER_THAN, 30)),
            actions = emptyList(),
            logicOperator = LogicOperator.AND
        )

        val result = ruleEngineService.validateRule(rule)
        assertFalse(result.isValid, "Rule without actions should fail validation")
    }

    @Test
    fun testValidateRuleBlankName() {
        val rule = CustomRule(
            id = "test-rule",
            name = "",
            enabled = true,
            conditions = listOf(AgeCondition(ComparisonOperator.GREATER_THAN, 30)),
            actions = listOf(LogAction(message = "Test")),
            logicOperator = LogicOperator.AND
        )

        val result = ruleEngineService.validateRule(rule)
        assertFalse(result.isValid, "Rule with blank name should fail validation")
    }

    @Test
    fun testPreviewRule() {
        val condition = AgeCondition(ComparisonOperator.GREATER_THAN, 30)
        
        val rule = CustomRule(
            id = "test-rule",
            name = "Test Rule",
            enabled = true,
            conditions = listOf(condition),
            actions = listOf(LogAction(message = "Test")),
            logicOperator = LogicOperator.AND
        )

        val mediaItems = listOf(mediaItem)
        every { conditionEvaluator.evaluate(condition, mediaItem) } returns true

        val result = ruleEngineService.previewRule(rule, mediaItems)
        assertEquals(1, result.size, "Preview should return matching media items")
    }

    @Test
    fun testExecuteActionsInDryRun() {
        val action = LogAction(message = "Test")
        
        val rule = CustomRule(
            id = "test-rule",
            name = "Test Rule",
            enabled = true,
            conditions = listOf(AgeCondition(ComparisonOperator.GREATER_THAN, 30)),
            actions = listOf(action),
            logicOperator = LogicOperator.AND
        )

        ruleEngineService.executeActions(rule, mediaItem, dryRun = true)
        
        // In dry run mode, actions should not be executed
        verify(exactly = 0) { actionExecutor.execute(any(), any()) }
    }
}
