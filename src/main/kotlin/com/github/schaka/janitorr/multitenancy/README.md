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
  default-admin:
    create-on-startup: true
    email: "admin@janitorr.local"
    password: "change-me-please"
```

### 2. Start Janitorr

The default admin user will be created automatically on startup.

### 3. Create Users

```bash
curl -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password",
    "role": "STANDARD_USER"
  }'
```

## Security Warning

⚠️ **IMPORTANT**: The API endpoints require authentication to prevent unauthorized access.

**Recommended Options:**
1. **Enable Built-in Authentication** (Easiest): Configure `security.enabled: true` in application.yml - See [Security Guide](../../../docs/wiki/en/Security-Guide.md)
2. Use a reverse proxy with authentication (Nginx, Traefik)
3. Restrict network access via firewall

## Future Enhancements

The following enhancements are under consideration and may be implemented in future releases. See the [Multi-Tenancy Guide](../../../docs/wiki/en/Multi-Tenancy-Guide.md) for detailed roadmap.

### Priority 1: Security (Under Consideration)
- Spring Security integration (may be added)
- JWT tokens (under consideration)
- BCrypt password hashing (may be implemented)
- CSRF protection (under consideration)

### Priority 2: Persistence (Under Consideration)
- JPA entities (may be added)
- H2/PostgreSQL support (under consideration)
- Migration scripts (may be implemented)

### Priority 3: Advanced Features (Under Consideration)
- OAuth (Google, GitHub, Discord) (may be added)
- 2FA (under consideration)
- API keys (may be implemented)
- Rate limiting (under consideration)
- Audit logging (may be added)

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
