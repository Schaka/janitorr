package com.github.schaka.janitorr.multitenancy.repository

import com.github.schaka.janitorr.multitenancy.model.Tenant
import com.github.schaka.janitorr.multitenancy.model.TenantUser
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository for tenant management.
 */
interface TenantRepository {
    fun findById(id: String): Tenant?
    fun findByDomain(domain: String): Tenant?
    fun findAll(): List<Tenant>
    fun save(tenant: Tenant): Tenant
    fun delete(id: String): Boolean
}

/**
 * In-memory implementation of TenantRepository
 */
class InMemoryTenantRepository : TenantRepository {
    
    private val tenants = ConcurrentHashMap<String, Tenant>()
    private val domainIndex = ConcurrentHashMap<String, String>() // domain -> tenantId
    
    override fun findById(id: String): Tenant? {
        return tenants[id]
    }
    
    override fun findByDomain(domain: String): Tenant? {
        val tenantId = domainIndex[domain.lowercase()] ?: return null
        return tenants[tenantId]
    }
    
    override fun findAll(): List<Tenant> {
        return tenants.values.toList()
    }
    
    override fun save(tenant: Tenant): Tenant {
        tenants[tenant.id] = tenant
        tenant.domain?.let { domainIndex[it.lowercase()] = tenant.id }
        return tenant
    }
    
    override fun delete(id: String): Boolean {
        val tenant = tenants.remove(id)
        tenant?.domain?.let { domainIndex.remove(it.lowercase()) }
        return tenant != null
    }
}

/**
 * Repository for tenant-user associations.
 */
interface TenantUserRepository {
    fun findByTenantId(tenantId: String): List<TenantUser>
    fun findByUserId(userId: String): List<TenantUser>
    fun findByTenantAndUser(tenantId: String, userId: String): TenantUser?
    fun save(tenantUser: TenantUser): TenantUser
    fun delete(tenantId: String, userId: String): Boolean
}

/**
 * In-memory implementation of TenantUserRepository
 */
class InMemoryTenantUserRepository : TenantUserRepository {
    
    private val tenantUsers = ConcurrentHashMap<String, TenantUser>() // "tenantId:userId" -> TenantUser
    
    private fun makeKey(tenantId: String, userId: String) = "$tenantId:$userId"
    
    override fun findByTenantId(tenantId: String): List<TenantUser> {
        return tenantUsers.values.filter { it.tenantId == tenantId }
    }
    
    override fun findByUserId(userId: String): List<TenantUser> {
        return tenantUsers.values.filter { it.userId == userId }
    }
    
    override fun findByTenantAndUser(tenantId: String, userId: String): TenantUser? {
        return tenantUsers[makeKey(tenantId, userId)]
    }
    
    override fun save(tenantUser: TenantUser): TenantUser {
        tenantUsers[makeKey(tenantUser.tenantId, tenantUser.userId)] = tenantUser
        return tenantUser
    }
    
    override fun delete(tenantId: String, userId: String): Boolean {
        return tenantUsers.remove(makeKey(tenantId, userId)) != null
    }
}
