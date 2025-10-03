package com.github.schaka.janitorr.multitenancy.api

import com.github.schaka.janitorr.multitenancy.model.User
import com.github.schaka.janitorr.multitenancy.model.UserProfile
import com.github.schaka.janitorr.multitenancy.model.UserRole
import com.github.schaka.janitorr.multitenancy.security.AuthorizationUtils
import com.github.schaka.janitorr.multitenancy.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for user management.
 * 
 * Provides endpoints for:
 * - User CRUD operations
 * - Profile management
 * - Password management
 * 
 * Only available when multi-tenancy is enabled.
 * 
 * Authorization:
 * - Creating users: ADMIN only
 * - Listing all users: ADMIN only
 * - Getting user details: ADMIN or the user themselves
 * - Updating profile: ADMIN or the user themselves
 * - Changing role: ADMIN only
 * - Enabling/disabling: ADMIN only
 * - Deleting users: ADMIN only
 */
@ConditionalOnProperty(prefix = "multitenancy", name = ["enabled"], havingValue = "true")
@RestController
@RequestMapping("/api/users")
class UserManagementController(
    private val userService: UserService
) {
    
    companion object {
        private val log = LoggerFactory.getLogger(UserManagementController::class.java)
    }
    
    /**
     * Create a new user
     * Requires: ADMIN role
     */
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        AuthorizationUtils.requireUserManagementAccess()
        
        return try {
            val user = userService.createUser(
                email = request.email,
                password = request.password,
                role = request.role,
                tenantId = request.tenantId
            )
            ResponseEntity.status(HttpStatus.CREATED).body(user.toResponse())
        } catch (e: IllegalArgumentException) {
            log.error("Failed to create user: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            log.error("Error creating user", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * Get all users
     * Requires: ADMIN role
     * TODO: Add pagination and filtering
     */
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        AuthorizationUtils.requireAdmin()
        
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users.map { it.toResponse() })
    }
    
    /**
     * Get user by ID
     * Requires: ADMIN role or the user themselves
     */
    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: String): ResponseEntity<UserResponse> {
        AuthorizationUtils.requireUserAccess(userId)
        
        val user = userService.findById(userId)
        return user?.let { ResponseEntity.ok(it.toResponse()) }
            ?: ResponseEntity.notFound().build()
    }
    
    /**
     * Get user profile
     * Requires: ADMIN role or the user themselves
     */
    @GetMapping("/{userId}/profile")
    fun getUserProfile(@PathVariable userId: String): ResponseEntity<UserProfile> {
        AuthorizationUtils.requireUserAccess(userId)
        
        val profile = userService.getUserProfile(userId)
        return profile?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
    
    /**
     * Update user profile
     * Requires: ADMIN role or the user themselves
     */
    @PutMapping("/{userId}/profile")
    fun updateUserProfile(
        @PathVariable userId: String,
        @RequestBody profile: UserProfile
    ): ResponseEntity<UserProfile> {
        AuthorizationUtils.requireUserAccess(userId)
        
        if (profile.userId != userId) {
            return ResponseEntity.badRequest().build()
        }
        val updated = userService.updateUserProfile(profile)
        return ResponseEntity.ok(updated)
    }
    
    /**
     * Update user role
     * Requires: ADMIN role
     */
    @PatchMapping("/{userId}/role")
    fun updateUserRole(
        @PathVariable userId: String,
        @RequestBody request: UpdateRoleRequest
    ): ResponseEntity<UserResponse> {
        AuthorizationUtils.requireUserManagementAccess()
        
        val success = userService.updateRole(userId, request.role)
        if (!success) {
            return ResponseEntity.notFound().build()
        }
        val user = userService.findById(userId)
        return user?.let { ResponseEntity.ok(it.toResponse()) }
            ?: ResponseEntity.notFound().build()
    }
    
    /**
     * Enable/disable user
     * Requires: ADMIN role
     */
    @PatchMapping("/{userId}/enabled")
    fun setUserEnabled(
        @PathVariable userId: String,
        @RequestBody request: SetEnabledRequest
    ): ResponseEntity<UserResponse> {
        AuthorizationUtils.requireUserManagementAccess()
        
        val success = userService.setUserEnabled(userId, request.enabled)
        if (!success) {
            return ResponseEntity.notFound().build()
        }
        val user = userService.findById(userId)
        return user?.let { ResponseEntity.ok(it.toResponse()) }
            ?: ResponseEntity.notFound().build()
    }
    
    /**
     * Change user password
     * Requires: ADMIN role or the user themselves
     */
    @PostMapping("/{userId}/password")
    fun changePassword(
        @PathVariable userId: String,
        @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Map<String, String>> {
        AuthorizationUtils.requireUserAccess(userId)
        
        val success = userService.updatePassword(userId, request.newPassword)
        return if (success) {
            ResponseEntity.ok(mapOf("message" to "Password updated successfully"))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Delete user
     * Requires: ADMIN role
     */
    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: String): ResponseEntity<Void> {
        AuthorizationUtils.requireUserManagementAccess()
        
        val success = userService.deleteUser(userId)
        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

// DTOs for API requests/responses

data class CreateUserRequest(
    val email: String,
    val password: String,
    val role: UserRole,
    val tenantId: String? = null
)

data class UpdateRoleRequest(
    val role: UserRole
)

data class SetEnabledRequest(
    val enabled: Boolean
)

data class ChangePasswordRequest(
    val newPassword: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val role: UserRole,
    val tenantId: String?,
    val enabled: Boolean,
    val createdAt: String,
    val lastLogin: String?
)

fun User.toResponse() = UserResponse(
    id = id,
    email = email,
    role = role,
    tenantId = tenantId,
    enabled = enabled,
    createdAt = createdAt.toString(),
    lastLogin = lastLogin?.toString()
)
