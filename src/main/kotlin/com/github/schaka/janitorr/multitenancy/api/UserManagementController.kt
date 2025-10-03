package com.github.schaka.janitorr.multitenancy.api

import com.github.schaka.janitorr.multitenancy.model.User
import com.github.schaka.janitorr.multitenancy.model.UserProfile
import com.github.schaka.janitorr.multitenancy.model.UserRole
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
     */
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
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
     * TODO: Add pagination and filtering
     */
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users.map { it.toResponse() })
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{userId}")
    fun getUserById(@PathVariable userId: String): ResponseEntity<UserResponse> {
        val user = userService.findById(userId)
        return user?.let { ResponseEntity.ok(it.toResponse()) }
            ?: ResponseEntity.notFound().build()
    }
    
    /**
     * Get user profile
     */
    @GetMapping("/{userId}/profile")
    fun getUserProfile(@PathVariable userId: String): ResponseEntity<UserProfile> {
        val profile = userService.getUserProfile(userId)
        return profile?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/{userId}/profile")
    fun updateUserProfile(
        @PathVariable userId: String,
        @RequestBody profile: UserProfile
    ): ResponseEntity<UserProfile> {
        if (profile.userId != userId) {
            return ResponseEntity.badRequest().build()
        }
        val updated = userService.updateUserProfile(profile)
        return ResponseEntity.ok(updated)
    }
    
    /**
     * Update user role
     */
    @PatchMapping("/{userId}/role")
    fun updateUserRole(
        @PathVariable userId: String,
        @RequestBody request: UpdateRoleRequest
    ): ResponseEntity<UserResponse> {
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
     */
    @PatchMapping("/{userId}/enabled")
    fun setUserEnabled(
        @PathVariable userId: String,
        @RequestBody request: SetEnabledRequest
    ): ResponseEntity<UserResponse> {
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
     */
    @PostMapping("/{userId}/password")
    fun changePassword(
        @PathVariable userId: String,
        @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Map<String, String>> {
        val success = userService.updatePassword(userId, request.newPassword)
        return if (success) {
            ResponseEntity.ok(mapOf("message" to "Password updated successfully"))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: String): ResponseEntity<Void> {
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
