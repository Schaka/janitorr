package com.github.schaka.janitorr.multitenancy.service

import com.github.schaka.janitorr.multitenancy.model.User
import com.github.schaka.janitorr.multitenancy.model.UserProfile
import com.github.schaka.janitorr.multitenancy.model.UserRole
import com.github.schaka.janitorr.multitenancy.repository.UserProfileRepository
import com.github.schaka.janitorr.multitenancy.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * Service for user management operations.
 * 
 * Handles user creation, authentication, and profile management.
 */
@Service
@ConditionalOnProperty(
    prefix = "multitenancy",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class UserService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository
) {
    
    companion object {
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }
    
    /**
     * Create a new user with the given details.
     * Password will be hashed before storage.
     */
    fun createUser(email: String, password: String, role: UserRole, tenantId: String? = null): User {
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("User with email $email already exists")
        }
        
        val user = User(
            id = UUID.randomUUID().toString(),
            email = email.lowercase(),
            passwordHash = hashPassword(password),
            role = role,
            tenantId = tenantId,
            enabled = true,
            createdAt = LocalDateTime.now()
        )
        
        val savedUser = userRepository.save(user)
        log.info("Created user: ${savedUser.email} with role ${savedUser.role}")
        
        // Create default profile
        val defaultProfile = UserProfile(
            userId = savedUser.id,
            displayName = email.substringBefore("@")
        )
        userProfileRepository.save(defaultProfile)
        
        return savedUser
    }
    
    /**
     * Find user by ID
     */
    fun findById(id: String): User? {
        return userRepository.findById(id)
    }
    
    /**
     * Find user by email
     */
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    /**
     * Get all users
     */
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
    
    /**
     * Get users by tenant
     */
    fun getUsersByTenant(tenantId: String): List<User> {
        return userRepository.findByTenantId(tenantId)
    }
    
    /**
     * Update user last login time
     */
    fun updateLastLogin(userId: String) {
        val user = userRepository.findById(userId) ?: return
        val updated = user.copy(lastLogin = LocalDateTime.now())
        userRepository.save(updated)
    }
    
    /**
     * Verify user password
     */
    fun verifyPassword(user: User, password: String): Boolean {
        return verifyPasswordHash(password, user.passwordHash)
    }
    
    /**
     * Update user password
     */
    fun updatePassword(userId: String, newPassword: String): Boolean {
        val user = userRepository.findById(userId) ?: return false
        val updated = user.copy(passwordHash = hashPassword(newPassword))
        userRepository.save(updated)
        log.info("Updated password for user: ${user.email}")
        return true
    }
    
    /**
     * Update user role
     */
    fun updateRole(userId: String, newRole: UserRole): Boolean {
        val user = userRepository.findById(userId) ?: return false
        val updated = user.copy(role = newRole)
        userRepository.save(updated)
        log.info("Updated role for user: ${user.email} to $newRole")
        return true
    }
    
    /**
     * Enable/disable user
     */
    fun setUserEnabled(userId: String, enabled: Boolean): Boolean {
        val user = userRepository.findById(userId) ?: return false
        val updated = user.copy(enabled = enabled)
        userRepository.save(updated)
        log.info("Set user ${user.email} enabled=$enabled")
        return true
    }
    
    /**
     * Delete user
     */
    fun deleteUser(userId: String): Boolean {
        val user = userRepository.findById(userId)
        if (user != null) {
            userRepository.delete(userId)
            userProfileRepository.delete(userId)
            log.info("Deleted user: ${user.email}")
            return true
        }
        return false
    }
    
    /**
     * Get user profile
     */
    fun getUserProfile(userId: String): UserProfile? {
        return userProfileRepository.findByUserId(userId)
    }
    
    /**
     * Update user profile
     */
    fun updateUserProfile(profile: UserProfile): UserProfile {
        return userProfileRepository.save(profile)
    }
    
    /**
     * Hash password using a simple approach.
     * TODO: Replace with BCrypt for production use
     */
    private fun hashPassword(password: String): String {
        // For now, use a simple hash. In production, use BCrypt:
        // return BCryptPasswordEncoder().encode(password)
        return "HASH:" + Base64.getEncoder().encodeToString(password.toByteArray())
    }
    
    /**
     * Verify password hash.
     * TODO: Replace with BCrypt verification for production use
     */
    private fun verifyPasswordHash(password: String, hash: String): Boolean {
        // For now, use simple comparison. In production, use BCrypt:
        // return BCryptPasswordEncoder().matches(password, hash)
        val expectedHash = hashPassword(password)
        return expectedHash == hash
    }
}
