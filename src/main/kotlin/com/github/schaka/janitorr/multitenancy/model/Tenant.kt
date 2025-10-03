package com.github.schaka.janitorr.multitenancy.model

import java.time.LocalDateTime

/**
 * Represents a tenant (organization/family) in the multi-tenancy system.
 */
data class Tenant(
    val id: String,
    val name: String,
    val domain: String? = null,
    val settings: TenantSettings = TenantSettings(),
    val enabled: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Tenant-specific settings and configurations
 */
data class TenantSettings(
    val isolationLevel: IsolationLevel = IsolationLevel.FULL,
    val sharedServices: Set<String> = emptySet(),
    val dataPath: String? = null,
    val resourceQuotas: TenantResourceQuotas = TenantResourceQuotas()
)

enum class IsolationLevel {
    /**
     * Complete isolation - separate configurations, paths, and resources
     */
    FULL,
    
    /**
     * Shared services - same *arr/Jellyfin instances with filtering
     */
    SHARED_SERVICES,
    
    /**
     * Logical only - same underlying resources, organizational separation
     */
    LOGICAL
}

/**
 * Resource quotas at the tenant level
 */
data class TenantResourceQuotas(
    val maxUsers: Int = -1, // -1 means unlimited
    val maxStorageBytes: Long = -1,
    val maxApiCallsPerDay: Int = -1,
    val maxCleanupOperationsPerDay: Int = -1
)

/**
 * Associates a user with a tenant and their role within that tenant
 */
data class TenantUser(
    val tenantId: String,
    val userId: String,
    val role: UserRole,
    val joinedAt: LocalDateTime = LocalDateTime.now()
)
