package com.github.schaka.janitorr.multitenancy.config

import com.github.schaka.janitorr.multitenancy.model.UserRole
import com.github.schaka.janitorr.multitenancy.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Initializes the multi-tenancy system on application startup.
 * 
 * Creates default admin user if configured to do so.
 */
@Component
@ConditionalOnProperty(
    prefix = "multitenancy.default-admin",
    name = ["create-on-startup"],
    havingValue = "true"
)
class MultiTenancyInitializer(
    private val userService: UserService,
    private val properties: MultiTenancyProperties
) : ApplicationRunner {
    
    companion object {
        private val log = LoggerFactory.getLogger(MultiTenancyInitializer::class.java)
    }
    
    override fun run(args: ApplicationArguments?) {
        log.info("Initializing multi-tenancy system...")
        
        val adminConfig = properties.defaultAdmin
        
        // Check if default admin already exists
        val existingAdmin = userService.findByEmail(adminConfig.email)
        if (existingAdmin != null) {
            log.info("Default admin user already exists: ${adminConfig.email}")
            return
        }
        
        // Create default admin user
        try {
            val admin = userService.createUser(
                email = adminConfig.email,
                password = adminConfig.password,
                role = UserRole.ADMIN
            )
            log.warn("=" * 80)
            log.warn("DEFAULT ADMIN USER CREATED")
            log.warn("Email: ${admin.email}")
            log.warn("Password: ${adminConfig.password}")
            log.warn("PLEASE CHANGE THIS PASSWORD IMMEDIATELY!")
            log.warn("=" * 80)
        } catch (e: Exception) {
            log.error("Failed to create default admin user", e)
        }
    }
    
    private operator fun String.times(n: Int): String {
        return this.repeat(n)
    }
}
