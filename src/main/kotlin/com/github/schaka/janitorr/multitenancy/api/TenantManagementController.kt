package com.github.schaka.janitorr.multitenancy.api

import com.github.schaka.janitorr.multitenancy.model.Tenant
import com.github.schaka.janitorr.multitenancy.model.TenantUser
import com.github.schaka.janitorr.multitenancy.model.UserRole
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
     */
    @PostMapping
    fun createTenant(@RequestBody request: CreateTenantRequest): ResponseEntity<Tenant> {
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
     */
    @GetMapping
    fun getAllTenants(): ResponseEntity<List<Tenant>> {
        val tenants = tenantService.getAllTenants()
        return ResponseEntity.ok(tenants)
    }
    
    /**
     * Get tenant by ID
     */
    @GetMapping("/{tenantId}")
    fun getTenantById(@PathVariable tenantId: String): ResponseEntity<Tenant> {
        val tenant = tenantService.findById(tenantId)
        return tenant?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
    
    /**
     * Get all users in a tenant
     */
    @GetMapping("/{tenantId}/users")
    fun getTenantUsers(@PathVariable tenantId: String): ResponseEntity<List<TenantUser>> {
        val users = tenantService.getTenantUsers(tenantId)
        return ResponseEntity.ok(users)
    }
    
    /**
     * Add user to tenant
     */
    @PostMapping("/{tenantId}/users")
    fun addUserToTenant(
        @PathVariable tenantId: String,
        @RequestBody request: AddUserToTenantRequest
    ): ResponseEntity<TenantUser> {
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
     */
    @DeleteMapping("/{tenantId}/users/{userId}")
    fun removeUserFromTenant(
        @PathVariable tenantId: String,
        @PathVariable userId: String
    ): ResponseEntity<Void> {
        val success = tenantService.removeUserFromTenant(tenantId, userId)
        return if (success) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * Delete tenant
     */
    @DeleteMapping("/{tenantId}")
    fun deleteTenant(@PathVariable tenantId: String): ResponseEntity<Void> {
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
