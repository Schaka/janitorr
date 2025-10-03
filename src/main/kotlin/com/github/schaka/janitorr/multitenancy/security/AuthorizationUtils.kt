package com.github.schaka.janitorr.multitenancy.security

import com.github.schaka.janitorr.multitenancy.model.UserRole
import com.github.schaka.janitorr.multitenancy.service.UserContext
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * Authorization utilities for checking user permissions.
 * 
 * These functions check the UserContext and throw ResponseStatusException
 * if authorization fails.
 */
object AuthorizationUtils {
    
    /**
     * Require that a user is authenticated.
     * @throws ResponseStatusException if not authenticated
     */
    fun requireAuthenticated() {
        if (!UserContext.isAuthenticated()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        }
    }
    
    /**
     * Require that the current user has a specific role or higher.
     * @param role The minimum required role
     * @throws ResponseStatusException if user doesn't have required role
     */
    fun requireRole(role: UserRole) {
        requireAuthenticated()
        if (!UserContext.hasRole(role)) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Insufficient permissions. Required: $role, Current: ${UserContext.getCurrentUserRole()}"
            )
        }
    }
    
    /**
     * Require that the current user is an admin.
     * @throws ResponseStatusException if user is not an admin
     */
    fun requireAdmin() {
        requireRole(UserRole.ADMIN)
    }
    
    /**
     * Check if the current user can modify another user.
     * Rules:
     * - Admins can modify anyone
     * - Users can only modify themselves
     * 
     * @param targetUserId The ID of the user being modified
     * @throws ResponseStatusException if not authorized
     */
    fun requireUserAccess(targetUserId: String) {
        requireAuthenticated()
        
        val currentUserId = UserContext.getCurrentUserId()
        val isAdmin = UserContext.isAdmin()
        
        if (!isAdmin && currentUserId != targetUserId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only modify your own account"
            )
        }
    }
    
    /**
     * Check if the current user can perform administrative user operations.
     * Only admins can create, delete, or change roles.
     * 
     * @throws ResponseStatusException if not an admin
     */
    fun requireUserManagementAccess() {
        requireAdmin()
    }
}
