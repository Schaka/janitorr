package com.github.schaka.janitorr.multitenancy.repository

import com.github.schaka.janitorr.multitenancy.model.User
import com.github.schaka.janitorr.multitenancy.model.UserProfile
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository for user management.
 * 
 * This is an in-memory implementation that can be replaced with
 * JPA/JDBC implementation for production use.
 */
interface UserRepository {
    fun findById(id: String): User?
    fun findByEmail(email: String): User?
    fun findAll(): List<User>
    fun findByTenantId(tenantId: String): List<User>
    fun save(user: User): User
    fun delete(id: String): Boolean
    fun existsByEmail(email: String): Boolean
}

/**
 * In-memory implementation of UserRepository
 */
class InMemoryUserRepository : UserRepository {
    
    private val users = ConcurrentHashMap<String, User>()
    private val emailIndex = ConcurrentHashMap<String, String>() // email -> userId
    
    override fun findById(id: String): User? {
        return users[id]
    }
    
    override fun findByEmail(email: String): User? {
        val userId = emailIndex[email.lowercase()] ?: return null
        return users[userId]
    }
    
    override fun findAll(): List<User> {
        return users.values.toList()
    }
    
    override fun findByTenantId(tenantId: String): List<User> {
        return users.values.filter { it.tenantId == tenantId }
    }
    
    override fun save(user: User): User {
        users[user.id] = user
        emailIndex[user.email.lowercase()] = user.id
        return user
    }
    
    override fun delete(id: String): Boolean {
        val user = users.remove(id)
        user?.let { emailIndex.remove(it.email.lowercase()) }
        return user != null
    }
    
    override fun existsByEmail(email: String): Boolean {
        return emailIndex.containsKey(email.lowercase())
    }
}

/**
 * Repository for user profiles.
 */
interface UserProfileRepository {
    fun findByUserId(userId: String): UserProfile?
    fun save(profile: UserProfile): UserProfile
    fun delete(userId: String): Boolean
}

/**
 * In-memory implementation of UserProfileRepository
 */
class InMemoryUserProfileRepository : UserProfileRepository {
    
    private val profiles = ConcurrentHashMap<String, UserProfile>()
    
    override fun findByUserId(userId: String): UserProfile? {
        return profiles[userId]
    }
    
    override fun save(profile: UserProfile): UserProfile {
        profiles[profile.userId] = profile
        return profile
    }
    
    override fun delete(userId: String): Boolean {
        return profiles.remove(userId) != null
    }
}
