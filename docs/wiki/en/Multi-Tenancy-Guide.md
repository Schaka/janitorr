# Multi-Tenancy and User Profile System

## Overview

Janitorr now supports multi-tenancy and user profiles, allowing multiple users with different roles and isolated configurations. This feature transforms Janitorr from a single-user application into a multi-user system suitable for families, shared hosting, or enterprise deployments.

## ⚠️ Important Notes

- **This feature is DISABLED by default** - You must explicitly enable it in your configuration
- **In-memory storage** - Current implementation uses in-memory storage (data lost on restart)
- **No authentication by default** - API endpoints are not secured unless you add authentication
- **Development stage** - This is a foundational implementation that can be extended

## Features

### User Management
- Create, read, update, and delete users
- Role-based access control (ADMIN, POWER_USER, STANDARD_USER, READ_ONLY)
- User profiles with personalized settings
- Password management

### Multi-Tenancy
- Create and manage tenants (organizations/families)
- Associate users with tenants
- Tenant-level isolation and configuration
- Support for shared services with filtering

### User Roles

| Role | Permissions |
|------|------------|
| **ADMIN** | Full system access, user management, global configuration, all cleanup operations |
| **POWER_USER** | Own profile management, advanced rules creation, manual cleanup execution, view all statistics |
| **STANDARD_USER** | Basic profile access, view own statistics, request cleanup operations, limited configuration |
| **READ_ONLY** | View dashboards, check system status, no cleanup permissions |

## Configuration

### Enable Multi-Tenancy

Add to your `application.yml`:

```yaml
multitenancy:
  enabled: true
  
  # Authentication configuration
  auth:
    # Enable HTTP Basic Auth for multi-tenancy endpoints
    # IMPORTANT: Set to true in production to protect APIs
    require-authentication: true
    
    # JWT authentication (not yet implemented)
    jwt-enabled: false
    jwt-secret: "your-secret-key-here"
    jwt-expiration-seconds: 86400
  
  # Create default admin user on startup
  default-admin:
    create-on-startup: true
    email: "admin@janitorr.local"
    password: "change-me-please"  # CHANGE THIS IMMEDIATELY!
```

### Authentication & Authorization

**IMPORTANT**: Multi-tenancy endpoints (`/api/users/**` and `/api/tenants/**`) support HTTP Basic Authentication to protect sensitive operations.

#### Enable Authentication

Set `multitenancy.auth.require-authentication: true` in your configuration:

```yaml
multitenancy:
  enabled: true
  auth:
    require-authentication: true  # Enable authentication
  default-admin:
    create-on-startup: true
    email: "admin@janitorr.local"
    password: "your-secure-password"
```

#### Using Authenticated Endpoints

Once authentication is enabled, all API requests must include HTTP Basic Auth credentials:

```bash
# Example: Create a user with authentication
curl -X POST http://localhost:8978/api/users \
  -u "admin@janitorr.local:your-secure-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "user-password",
    "role": "STANDARD_USER"
  }'
```

#### Authorization Rules

- **Tenant Management**: Requires ADMIN role for all operations
- **User Management**: 
  - Creating, deleting users, changing roles: ADMIN only
  - Users can view and update their own profile
  - Users can change their own password
  - Listing all users: ADMIN only

#### Backward Compatibility

For backward compatibility, authentication is **disabled by default** (`require-authentication: false`). This allows existing deployments to continue working, but is **NOT RECOMMENDED** for production use.

**⚠️ WARNING**: Running with authentication disabled allows anyone with network access to create, modify, or delete users and tenants.

### Additional Security Options

1. **Reverse Proxy Authentication**: Use Nginx, Traefik, or Caddy with authentication in front of Janitorr

2. **Network Isolation**: Restrict access via firewall rules (iptables, cloud security groups)

3. **Built-in Security Module**: See the [Security Guide](Security-Guide.md) for additional security features.

For comprehensive security setup instructions, see the [Security Guide](Security-Guide.md).

## API Endpoints

### User Management

#### Create User
```bash
POST /api/users
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secure-password",
  "role": "STANDARD_USER",
  "tenantId": "optional-tenant-id"
}
```

#### Get All Users
```bash
GET /api/users
```

#### Get User by ID
```bash
GET /api/users/{userId}
```

#### Get User Profile
```bash
GET /api/users/{userId}/profile
```

#### Update User Profile
```bash
PUT /api/users/{userId}/profile
Content-Type: application/json

{
  "userId": "user-id",
  "displayName": "John Doe",
  "preferences": {
    "theme": "dark",
    "language": "en",
    "timezone": "UTC"
  }
}
```

#### Update User Role
```bash
PATCH /api/users/{userId}/role
Content-Type: application/json

{
  "role": "POWER_USER"
}
```

#### Enable/Disable User
```bash
PATCH /api/users/{userId}/enabled
Content-Type: application/json

{
  "enabled": false
}
```

#### Change Password
```bash
POST /api/users/{userId}/password
Content-Type: application/json

{
  "newPassword": "new-secure-password"
}
```

#### Delete User
```bash
DELETE /api/users/{userId}
```

### Tenant Management

#### Create Tenant
```bash
POST /api/tenants
Content-Type: application/json

{
  "name": "Smith Family",
  "domain": "smith.example.com"
}
```

#### Get All Tenants
```bash
GET /api/tenants
```

#### Get Tenant by ID
```bash
GET /api/tenants/{tenantId}
```

#### Get Tenant Users
```bash
GET /api/tenants/{tenantId}/users
```

#### Add User to Tenant
```bash
POST /api/tenants/{tenantId}/users
Content-Type: application/json

{
  "userId": "user-id",
  "role": "STANDARD_USER"
}
```

#### Remove User from Tenant
```bash
DELETE /api/tenants/{tenantId}/users/{userId}
```

#### Delete Tenant
```bash
DELETE /api/tenants/{tenantId}
```

## Usage Examples

### Family Setup

**Note**: These examples assume authentication is enabled. Add `-u "admin@janitorr.local:password"` to each curl command when `require-authentication: true`.

1. **Create tenant for the family:**
```bash
curl -X POST http://localhost:8978/api/tenants \
  -u "admin@janitorr.local:your-password" \
  -H "Content-Type: application/json" \
  -d '{"name": "Smith Family"}'
```

2. **Create users with different roles:**
```bash
# Dad (Admin)
curl -X POST http://localhost:8978/api/users \
  -u "admin@janitorr.local:your-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "dad@smith.family",
    "password": "secure-password-1",
    "role": "ADMIN"
  }'

# Mom (Power User)
curl -X POST http://localhost:8978/api/users \
  -u "admin@janitorr.local:your-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "mom@smith.family",
    "password": "secure-password-2",
    "role": "POWER_USER"
  }'

# Teenager (Standard User)
curl -X POST http://localhost:8978/api/users \
  -u "admin@janitorr.local:your-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teen@smith.family",
    "password": "secure-password-3",
    "role": "STANDARD_USER"
  }'

# Child (Read-Only)
curl -X POST http://localhost:8978/api/users \
  -u "admin@janitorr.local:your-password" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "kid@smith.family",
    "password": "secure-password-4",
    "role": "READ_ONLY"
  }'
```

3. **Add users to tenant:**
```bash
curl -X POST http://localhost:8978/api/tenants/{tenantId}/users \
  -u "admin@janitorr.local:your-password" \
  -H "Content-Type: application/json" \
  -d '{"userId": "{userId}", "role": "ADMIN"}'
```

## Architecture

### Data Model

```
User
├── id: String
├── email: String
├── passwordHash: String
├── role: UserRole
├── tenantId: String?
├── enabled: Boolean
└── createdAt: LocalDateTime

UserProfile
├── userId: String
├── displayName: String
├── preferences: UserPreferences
├── cleanupSettings: UserCleanupSettings
├── notifications: NotificationSettings
├── mediaAccess: MediaAccessRules
└── quotas: ResourceQuotas

Tenant
├── id: String
├── name: String
├── domain: String?
├── settings: TenantSettings
└── enabled: Boolean
```

### Storage

**Current**: In-memory (ConcurrentHashMap)
- Data is lost on application restart
- Suitable for testing and development
- Fast performance

**Future**: Database persistence
- H2, PostgreSQL, MySQL support
- JPA/Hibernate entities
- Migration scripts
- Production-ready

## Limitations

1. **No Persistence**: Data is stored in-memory only
2. **No Authentication**: API endpoints are not secured
3. **No JWT Tokens**: Token-based authentication not implemented
4. **No OAuth**: Third-party authentication not implemented
5. **No Session Management**: No active session tracking
6. **Basic Password Hashing**: Uses simple Base64 (should use BCrypt in production)
7. **No Audit Logging**: User actions are not logged
8. **No Rate Limiting**: No API rate limiting per user
9. **No Resource Quotas Enforcement**: Quotas are tracked but not enforced

## Future Enhancements

The following enhancements are under consideration and may be implemented in future releases:

### Phase 1: Security (Priority - Under Consideration)
- [ ] Spring Security integration (may be added)
- [ ] JWT token generation and validation (under consideration)
- [ ] BCrypt password hashing (may be implemented)
- [ ] Session management (under consideration)
- [ ] CSRF protection (may be added)

### Phase 2: Persistence (Under Consideration)
- [ ] JPA entities (may be implemented)
- [ ] H2 database for development (under consideration)
- [ ] PostgreSQL support for production (may be added)
- [ ] Database migration scripts (under consideration)
- [ ] Backup and restore (may be implemented)

### Phase 3: Advanced Features (Under Consideration)
- [ ] OAuth integration (Google, GitHub, Discord) (may be added)
- [ ] Two-factor authentication (under consideration)
- [ ] API key management (may be implemented)
- [ ] Rate limiting (under consideration)
- [ ] Audit logging (may be added)
- [ ] Email notifications (under consideration)
- [ ] Password recovery (may be implemented)
- [ ] User invitation system (under consideration)

### Phase 4: UI Integration (Under Consideration)
- [ ] Login/logout pages (may be added)
- [ ] User management dashboard (under consideration)
- [ ] Profile settings page (may be implemented)
- [ ] Tenant switcher (under consideration)
- [ ] Role-based UI components

### Phase 5: Multi-Tenancy
- [ ] Tenant-specific configurations
- [ ] Path isolation (`/data/tenants/{tenantId}/`)
- [ ] Service filtering (per-tenant *arr instances)
- [ ] Resource quota enforcement
- [ ] Billing integration

## Migration Guide

### Existing Deployments

If you're upgrading from a single-user Janitorr instance:

1. **Multi-tenancy is disabled by default** - Your existing setup will work unchanged
2. **Optional migration** - You can choose to enable multi-tenancy later
3. **No breaking changes** - Existing configuration remains valid

### Enabling Multi-Tenancy

1. Add configuration to `application.yml`
2. Restart Janitorr
3. Create admin user via API or enable default admin
4. Create additional users as needed

## Security Best Practices

1. **Change default admin password immediately**
2. **Use strong, unique passwords** for all users
3. **Enable HTTPS** via reverse proxy
4. **Restrict network access** to trusted IP ranges
5. **Implement authentication** before production use
6. **Rotate passwords regularly**
7. **Monitor user activity** via logs
8. **Disable unused features**
9. **Keep Janitorr updated**
10. **Back up configuration** regularly

## Troubleshooting

### Multi-tenancy endpoints return 404

Check that multi-tenancy is enabled in your configuration:
```yaml
multitenancy:
  enabled: true
```

### Default admin not created

Verify the configuration:
```yaml
multitenancy:
  enabled: true
  default-admin:
    create-on-startup: true
```

Check logs for errors during startup.

### Can't delete user

Ensure the user ID is correct and the user exists. Users associated with tenants should be removed from tenants first (or will be automatically removed on deletion).

## Support

For questions, issues, or feature requests:
- GitHub Issues: https://github.com/carcheky/janitorr/issues
- GitHub Discussions: https://github.com/carcheky/janitorr/discussions

## Contributing

Contributions are welcome! Areas that need help:
- Spring Security integration
- Database persistence (JPA)
- OAuth integration
- UI components for user management
- Testing and documentation

See [CONTRIBUTING.md](../../CONTRIBUTING.md) for guidelines.
