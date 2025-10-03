package com.github.schaka.janitorr.multitenancy.model

import java.time.LocalDateTime

/**
 * Represents a user in the multi-tenancy system.
 * 
 * This is a lightweight data class that can be persisted to a database
 * or used in-memory for initial implementation.
 */
data class User(
    val id: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val tenantId: String? = null,
    val enabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastLogin: LocalDateTime? = null
)

/**
 * User roles with hierarchical permissions.
 */
enum class UserRole(val level: Int) {
    /**
     * Full system access, user management, global configuration, all cleanup operations
     */
    ADMIN(4),
    
    /**
     * Own profile management, advanced rules creation, manual cleanup execution, view all statistics
     */
    POWER_USER(3),
    
    /**
     * Basic profile access, view own statistics, request cleanup operations, limited configuration
     */
    STANDARD_USER(2),
    
    /**
     * View dashboards, check system status, no cleanup permissions
     */
    READ_ONLY(1);
    
    /**
     * Check if this role has at least the permission level of another role
     */
    fun hasPermission(requiredRole: UserRole): Boolean {
        return this.level >= requiredRole.level
    }
}
