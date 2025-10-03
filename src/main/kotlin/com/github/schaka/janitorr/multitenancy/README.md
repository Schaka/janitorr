# Multi-Tenancy Module

This module provides multi-user and multi-tenancy support for Janitorr.

## Package Structure

```
multitenancy/
├── api/                          # REST API controllers
│   ├── UserManagementController.kt
│   └── TenantManagementController.kt
├── config/                       # Configuration
│   ├── MultiTenancyConfig.kt
│   ├── MultiTenancyProperties.kt
│   └── MultiTenancyInitializer.kt
├── model/                        # Data models
│   ├── User.kt
│   ├── UserProfile.kt
│   └── Tenant.kt
├── repository/                   # Data access layer
│   ├── UserRepository.kt
│   └── TenantRepository.kt
└── service/                      # Business logic
    ├── UserService.kt
    ├── TenantService.kt
    └── UserContext.kt
```

## Features

### Implemented ✅
- User CRUD operations
- Role-based access control (ADMIN, POWER_USER, STANDARD_USER, READ_ONLY)
- User profiles with preferences
- Tenant management
- User-tenant associations
- In-memory storage (development)
- REST API endpoints
- Default admin user creation
- **HTTP Basic Authentication** for API endpoints
- **Role-based authorization** (ADMIN for management, users can access own data)
- Configurable authentication (can be disabled for backward compatibility)

### Not Implemented ⚠️
- Spring Security integration
- JWT token authentication
- BCrypt password hashing (uses simple Base64)
- Database persistence (uses in-memory)
- OAuth integration
- Session management
- Rate limiting
- Audit logging
- Resource quota enforcement
- Email notifications

## Quick Start

### 1. Enable Multi-Tenancy

Add to `application.yml`:

```yaml
multitenancy:
  enabled: true
  
  # Enable authentication (recommended for production)
  auth:
    require-authentication: true
  
  default-admin:
    create-on-startup: true
    email: "admin@janitorr.local"
    password: "change-me-please"
```

### 2. Start Janitorr

The default admin user will be created automatically on startup.

### 3. Create Users (with authentication)

```bash
curl -X POST http://localhost:8978/api/users \
  -u "admin@janitorr.local:change-me-please" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password",
    "role": "STANDARD_USER"
  }'
```

**Note**: When `require-authentication: false` (default for backward compatibility), omit the `-u` flag. However, this is **NOT RECOMMENDED** for production.

## Authentication & Authorization

### HTTP Basic Authentication

The module supports HTTP Basic Authentication to protect sensitive API endpoints:

- **Configuration**: Set `multitenancy.auth.require-authentication: true`
- **Endpoints Protected**: `/api/users/**` and `/api/tenants/**`
- **How it Works**: Uses HTTP Basic Auth with credentials validated against user database

### Authorization Rules

- **Tenant Operations**: All require ADMIN role
- **User Management**:
  - Create/delete users, change roles: ADMIN only
  - View/update own profile: User or ADMIN
  - Change own password: User or ADMIN
  - List all users: ADMIN only

### Backward Compatibility

Authentication is **disabled by default** (`require-authentication: false`) for backward compatibility. Existing deployments continue to work without changes, but **enabling authentication is strongly recommended** for production.

## Security Warning

⚠️ **IMPORTANT**: HTTP Basic Authentication is now available for API endpoint protection.

**Production Recommendation:**
Enable authentication by setting `multitenancy.auth.require-authentication: true` in your configuration.

**Additional Security Options:**
1. **Enable Built-in Authentication** (Recommended): Configure `multitenancy.auth.require-authentication: true` - See [Multi-Tenancy Guide](../../../docs/wiki/en/Multi-Tenancy-Guide.md)
2. Use a reverse proxy with authentication (Nginx, Traefik)
3. Restrict network access via firewall

## Future Enhancements

See the [Multi-Tenancy Guide](../../../docs/wiki/en/Multi-Tenancy-Guide.md) for detailed roadmap.

### Priority 1: Security
- Spring Security integration
- JWT tokens
- BCrypt password hashing
- CSRF protection

### Priority 2: Persistence
- JPA entities
- H2/PostgreSQL support
- Migration scripts

### Priority 3: Advanced Features
- OAuth (Google, GitHub, Discord)
- 2FA
- API keys
- Rate limiting
- Audit logging

## Contributing

Areas that need work:
- [ ] Spring Security configuration
- [ ] JPA entity mapping
- [ ] Database migration scripts
- [ ] OAuth providers
- [ ] UI components
- [ ] Unit tests
- [ ] Integration tests
- [ ] Performance testing

## Testing

Currently limited testing due to no build environment with Java 25.

To test manually:
1. Enable multi-tenancy in config
2. Start Janitorr
3. Use curl/Postman to test API endpoints
4. Verify CRUD operations work correctly
5. Test role-based access (when authentication is added)

## Documentation

- [English Guide](../../../docs/wiki/en/Multi-Tenancy-Guide.md)
- [Guía en Español](../../../docs/wiki/es/Guia-Multi-Tenancy.md)

## License

Same as parent project (see LICENSE.txt)
