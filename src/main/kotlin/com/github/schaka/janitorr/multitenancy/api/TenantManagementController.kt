package com.github.schaka.janitorr.multitenancy.api

import com.github.schaka.janitorr.multitenancy.model.Tenant
import com.github.schaka.janitorr.multitenancy.model.TenantUser
import com.github.schaka.janitorr.multitenancy.model.UserRole
import com.github.schaka.janitorr.multitenancy.security.AuthorizationUtils
import com.github.schaka.janitorr.multitenancy.service.TenantService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for tenant management.
 * 
 * Provides endpoints for:
 * - Tenant CRUD operations
 * - User-tenant associations
 * - Tenant switching
 * 
 * Only available when multi-tenancy is enabled.
 * 
 * Authorization:
 * - All tenant operations require ADMIN role
 */
@ConditionalOnProperty(prefix = "multitenancy", name = ["enabled"], havingValue = "true")
@RestController
@RequestMapping("/api/tenants")
class TenantManagementController(
    private val tenantService: TenantService
) {
    
    companion object {
        private val log = LoggerFactory.getLogger(TenantManagementController::class.java)
    }
    
    /**
     * Create a new tenant
     * Requires: ADMIN role
     */
    @PostMapping
    fun createTenant(@RequestBody request: CreateTenantRequest): ResponseEntity<Tenant> {
        AuthorizationUtils.requireAdmin()
        
        return try {
            val tenant = tenantService.createTenant(
                name = request.name,
                domain = request.domain
            )
            ResponseEntity.status(HttpStatus.CREATED).body(tenant)
        } catch (e: Exception) {
            log.error("Error creating tenant", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * Get all tenants
     * Requires: ADMIN role
     */
    @GetMapping
    fun getAllTenants(): ResponseEntity<List<Tenant>> {
        AuthorizationUtils.requireAdmin()
        
        val tenants = tenantService.getAllTenants()
        return ResponseEntity.ok(tenants)
    }
    
    /**
     * Get tenant by ID
     * Requires: ADMIN role
     */
    @GetMapping("/{tenantId}")
    fun getTenantById(@PathVariable tenantId: String): ResponseEntity<Tenant> {
        AuthorizationUtils.requireAdmin()
        
        val tenant = tenantService.findById(tenantId)
        return tenant?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
    
    /**
     * Get all users in a tenant
     * Requires: ADMIN role
     */
    @GetMapping("/{tenantId}/users")
    fun getTenantUsers(@PathVariable tenantId: String): ResponseEntity<List<TenantUser>> {
        AuthorizationUtils.requireAdmin()
        
        val users = tenantService.getTenantUsers(tenantId)
        return ResponseEntity.ok(users)
    }
    
    /**
     * Add user to tenant
     * Requires: ADMIN role
     */
    @PostMapping("/{tenantId}/users")
    fun addUserToTenant(
        @PathVariable tenantId: String,
        @RequestBody request: AddUserToTenantRequest
    ): ResponseEntity<TenantUser> {
        AuthorizationUtils.requireAdmin()
        
        return try {
            val tenantUser = tenantService.addUserToTenant(
                tenantId = tenantId,
                userId = request.userId,
                role = request.role
            )
            ResponseEntity.status(HttpStatus.CREATED).body(tenantUser)
        } catch (e: IllegalArgumentException) {
            log.error("Failed to add user to tenant: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            log.error("Error adding user to tenant", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    /**
     * Remove user from tenant
     * Requires: ADMIN role
     */
    @DeleteMapping("/{tenantId}/users/{userId}")
    fun removeUserFromTenant(
        @PathVariable tenantId: String,
        @PathVariable userId: String
    ): ResponseEntity<Void> {
        AuthorizationUtils.requireAdmin()
        
        val success = tenantService.removeUserFromTenant(tenantId, userId)
        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Delete tenant
     * Requires: ADMIN role
     */
    @DeleteMapping("/{tenantId}")
    fun deleteTenant(@PathVariable tenantId: String): ResponseEntity<Void> {
        AuthorizationUtils.requireAdmin()
        
        val success = tenantService.deleteTenant(tenantId)
        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

// DTOs for API requests

data class CreateTenantRequest(
    val name: String,
    val domain: String? = null
)

data class AddUserToTenantRequest(
    val userId: String,
    val role: UserRole
)
