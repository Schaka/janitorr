package com.github.schaka.janitorr.multitenancy.config

import com.github.schaka.janitorr.multitenancy.security.BasicAuthInterceptor
import com.github.schaka.janitorr.multitenancy.service.TenantService
import com.github.schaka.janitorr.multitenancy.service.UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Integration tests to verify multitenancy beans are conditionally loaded
 * based on the multitenancy.enabled property.
 */
class MultiTenancyConditionalLoadingTest {

    /**
     * Test that multitenancy beans are NOT loaded when disabled (default)
     */
    @SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
    )
    @TestPropertySource(
        properties = [
            "multitenancy.enabled=false"
        ]
    )
    class WhenMultitenancyDisabled(
        private val applicationContext: ApplicationContext
    ) {

        @Test
        fun `UserService bean should not exist`() {
            assertFailsWith<NoSuchBeanDefinitionException> {
                applicationContext.getBean(UserService::class.java)
            }
        }

        @Test
        fun `TenantService bean should not exist`() {
            assertFailsWith<NoSuchBeanDefinitionException> {
                applicationContext.getBean(TenantService::class.java)
            }
        }

        @Test
        fun `BasicAuthInterceptor bean should not exist`() {
            assertFailsWith<NoSuchBeanDefinitionException> {
                applicationContext.getBean(BasicAuthInterceptor::class.java)
            }
        }
    }

    /**
     * Test that multitenancy beans ARE loaded when enabled
     */
    @SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
    )
    @TestPropertySource(
        properties = [
            "multitenancy.enabled=true",
            "multitenancy.auth.require-authentication=false"
        ]
    )
    class WhenMultitenancyEnabled(
        private val applicationContext: ApplicationContext
    ) {

        @Test
        fun `UserService bean should exist`() {
            val userService = applicationContext.getBean(UserService::class.java)
            assertNotNull(userService)
        }

        @Test
        fun `TenantService bean should exist`() {
            val tenantService = applicationContext.getBean(TenantService::class.java)
            assertNotNull(tenantService)
        }

        @Test
        fun `BasicAuthInterceptor bean should exist`() {
            val interceptor = applicationContext.getBean(BasicAuthInterceptor::class.java)
            assertNotNull(interceptor)
        }
    }
}
