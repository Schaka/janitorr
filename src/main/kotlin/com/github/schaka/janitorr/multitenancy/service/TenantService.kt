package com.github.schaka.janitorr.multitenancy.service

import com.github.schaka.janitorr.multitenancy.model.Tenant
import com.github.schaka.janitorr.multitenancy.model.TenantUser
import com.github.schaka.janitorr.multitenancy.model.UserRole
import com.github.schaka.janitorr.multitenancy.repository.TenantRepository
import com.github.schaka.janitorr.multitenancy.repository.TenantUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * Service for tenant management operations.
 */
@Service
class TenantService(
    private val tenantRepository: TenantRepository,
    private val tenantUserRepository: TenantUserRepository
) {
    
    companion object {
        private val log = LoggerFactory.getLogger(TenantService::class.java)
    }
    
    /**
     * Create a new tenant
     */
    fun createTenant(name: String, domain: String? = null): Tenant {
        val tenant = Tenant(
            id = UUID.randomUUID().toString(),
            name = name,
            domain = domain,
            enabled = true,
            createdAt = LocalDateTime.now()
        )
        
        val saved = tenantRepository.save(tenant)
        log.info("Created tenant: ${saved.name} (${saved.id})")
        return saved
    }
    
    /**
     * Find tenant by ID
     */
    fun findById(id: String): Tenant? {
        return tenantRepository.findById(id)
    }
    
    /**
     * Find tenant by domain
     */
    fun findByDomain(domain: String): Tenant? {
        return tenantRepository.findByDomain(domain)
    }
    
    /**
     * Get all tenants
     */
    fun getAllTenants(): List<Tenant> {
        return tenantRepository.findAll()
    }
    
    /**
     * Add user to tenant
     */
    fun addUserToTenant(tenantId: String, userId: String, role: UserRole): TenantUser {
        // Verify tenant exists
        tenantRepository.findById(tenantId) 
            ?: throw IllegalArgumentException("Tenant not found: $tenantId")
        
        val tenantUser = TenantUser(
            tenantId = tenantId,
            userId = userId,
            role = role,
            joinedAt = LocalDateTime.now()
        )
        
        val saved = tenantUserRepository.save(tenantUser)
        log.info("Added user $userId to tenant $tenantId with role $role")
        return saved
    }
    
    /**
     * Remove user from tenant
     */
    fun removeUserFromTenant(tenantId: String, userId: String): Boolean {
        val removed = tenantUserRepository.delete(tenantId, userId)
        if (removed) {
            log.info("Removed user $userId from tenant $tenantId")
        }
        return removed
    }
    
    /**
     * Get all users in a tenant
     */
    fun getTenantUsers(tenantId: String): List<TenantUser> {
        return tenantUserRepository.findByTenantId(tenantId)
    }
    
    /**
     * Get all tenants a user belongs to
     */
    fun getUserTenants(userId: String): List<TenantUser> {
        return tenantUserRepository.findByUserId(userId)
    }
    
    /**
     * Check if user belongs to tenant
     */
    fun isUserInTenant(tenantId: String, userId: String): Boolean {
        return tenantUserRepository.findByTenantAndUser(tenantId, userId) != null
    }
    
    /**
     * Get user's role in tenant
     */
    fun getUserRoleInTenant(tenantId: String, userId: String): UserRole? {
        return tenantUserRepository.findByTenantAndUser(tenantId, userId)?.role
    }
    
    /**
     * Delete tenant
     */
    fun deleteTenant(tenantId: String): Boolean {
        val tenant = tenantRepository.findById(tenantId)
        if (tenant != null) {
            // Remove all user associations
            val users = tenantUserRepository.findByTenantId(tenantId)
            users.forEach { tenantUserRepository.delete(it.tenantId, it.userId) }
            
            // Delete tenant
            tenantRepository.delete(tenantId)
            log.info("Deleted tenant: ${tenant.name}")
            return true
        }
        return false
    }
}
