package com.github.schaka.janitorr.multitenancy.service

import com.github.schaka.janitorr.multitenancy.model.UserRole

/**
 * Thread-local context for the current user making requests.
 * 
 * This allows services to access the authenticated user without
 * passing it through every method call.
 */
object UserContext {
    
    private val threadLocalUserId = ThreadLocal<String?>()
    private val threadLocalUserRole = ThreadLocal<UserRole?>()
    private val threadLocalTenantId = ThreadLocal<String?>()
    
    /**
     * Set the current user context
     */
    fun setContext(userId: String, userRole: UserRole, tenantId: String? = null) {
        threadLocalUserId.set(userId)
        threadLocalUserRole.set(userRole)
        threadLocalTenantId.set(tenantId)
    }
    
    /**
     * Clear the current user context
     */
    fun clearContext() {
        threadLocalUserId.remove()
        threadLocalUserRole.remove()
        threadLocalTenantId.remove()
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return threadLocalUserId.get()
    }
    
    /**
     * Get current user role
     */
    fun getCurrentUserRole(): UserRole? {
        return threadLocalUserRole.get()
    }
    
    /**
     * Get current tenant ID
     */
    fun getCurrentTenantId(): String? {
        return threadLocalTenantId.get()
    }
    
    /**
     * Check if a user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return getCurrentUserId() != null
    }
    
    /**
     * Check if current user has the required role
     */
    fun hasRole(requiredRole: UserRole): Boolean {
        val currentRole = getCurrentUserRole() ?: return false
        return currentRole.hasPermission(requiredRole)
    }
    
    /**
     * Check if current user is an admin
     */
    fun isAdmin(): Boolean {
        return getCurrentUserRole() == UserRole.ADMIN
    }
}

/**
 * Tenant context for multi-tenancy support.
 * 
 * This maintains the current tenant ID for filtering data
 * and configuration.
 */
object TenantContext {
    
    private val threadLocalTenantId = ThreadLocal<String?>()
    
    /**
     * Set the current tenant
     */
    fun setTenantId(tenantId: String?) {
        threadLocalTenantId.set(tenantId)
    }
    
    /**
     * Get the current tenant ID
     */
    fun getTenantId(): String? {
        return threadLocalTenantId.get()
    }
    
    /**
     * Clear the tenant context
     */
    fun clear() {
        threadLocalTenantId.remove()
    }
    
    /**
     * Check if tenant context is set
     */
    fun isSet(): Boolean {
        return getTenantId() != null
    }
}
