# Multi-Tenancy Quick Start Guide

This guide will help you quickly enable and test the multi-tenancy features in Janitorr.

## Prerequisites

- Janitorr installed and working
- Access to modify `application.yml`
- curl or similar tool for API testing

## Step 1: Enable Multi-Tenancy

Edit your `application.yml` and add:

```yaml
multitenancy:
  enabled: true
  default-admin:
    create-on-startup: true
    email: "admin@janitorr.local"
    password: "MySecurePassword123!"  # CHANGE THIS!
```

## Step 2: Restart Janitorr

```bash
docker-compose restart janitorr
# or
docker restart janitorr
```

Check the logs for the admin user creation message:

```bash
docker logs janitorr | grep "DEFAULT ADMIN"
```

You should see:
```
================================================================================
DEFAULT ADMIN USER CREATED
Email: admin@janitorr.local
Password: MySecurePassword123!
PLEASE CHANGE THIS PASSWORD IMMEDIATELY!
================================================================================
```

## Step 3: Verify Multi-Tenancy is Enabled

Test the user endpoints:

```bash
curl http://localhost:8978/api/users
```

You should see a JSON array with one user (the default admin).

## Step 4: Create Your First User

```bash
curl -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePassword456",
    "role": "POWER_USER"
  }'
```

Response:
```json
{
  "id": "generated-uuid",
  "email": "john@example.com",
  "role": "POWER_USER",
  "tenantId": null,
  "enabled": true,
  "createdAt": "2025-01-15T10:30:00",
  "lastLogin": null
}
```

## Step 5: Create a Tenant (Optional)

For multi-organization support:

```bash
curl -X POST http://localhost:8978/api/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Family",
    "domain": "family.example.com"
  }'
```

## Step 6: Add User to Tenant

```bash
# Get tenant ID from previous response, then:
curl -X POST http://localhost:8978/api/tenants/{tenantId}/users \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{userId}",
    "role": "POWER_USER"
  }'
```

## Common Operations

### List All Users

```bash
curl http://localhost:8978/api/users
```

### Get User Profile

```bash
curl http://localhost:8978/api/users/{userId}/profile
```

### Update User Profile

```bash
curl -X PUT http://localhost:8978/api/users/{userId}/profile \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "{userId}",
    "displayName": "John Doe",
    "preferences": {
      "theme": "dark",
      "language": "en",
      "timezone": "America/New_York"
    }
  }'
```

### Change User Password

```bash
curl -X POST http://localhost:8978/api/users/{userId}/password \
  -H "Content-Type: application/json" \
  -d '{
    "newPassword": "NewSecurePassword789"
  }'
```

### Update User Role

```bash
curl -X PATCH http://localhost:8978/api/users/{userId}/role \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ADMIN"
  }'
```

### Disable User

```bash
curl -X PATCH http://localhost:8978/api/users/{userId}/enabled \
  -H "Content-Type: application/json" \
  -d '{
    "enabled": false
  }'
```

### Delete User

```bash
curl -X DELETE http://localhost:8978/api/users/{userId}
```

## Family Setup Example

Here's a complete example for setting up a family:

```bash
# 1. Create tenant
TENANT_RESPONSE=$(curl -s -X POST http://localhost:8978/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"name": "Smith Family"}')

TENANT_ID=$(echo $TENANT_RESPONSE | jq -r '.id')

# 2. Create family members
# Parent 1 (Admin)
USER1=$(curl -s -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "parent1@smith.family",
    "password": "SecurePass1",
    "role": "ADMIN"
  }')
USER1_ID=$(echo $USER1 | jq -r '.id')

# Parent 2 (Power User)
USER2=$(curl -s -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "parent2@smith.family",
    "password": "SecurePass2",
    "role": "POWER_USER"
  }')
USER2_ID=$(echo $USER2 | jq -r '.id')

# Teenager (Standard User)
USER3=$(curl -s -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teen@smith.family",
    "password": "SecurePass3",
    "role": "STANDARD_USER"
  }')
USER3_ID=$(echo $USER3 | jq -r '.id')

# Child (Read Only)
USER4=$(curl -s -X POST http://localhost:8978/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "child@smith.family",
    "password": "SecurePass4",
    "role": "READ_ONLY"
  }')
USER4_ID=$(echo $USER4 | jq -r '.id')

# 3. Add all to tenant
for USER_ID in $USER1_ID $USER2_ID $USER3_ID $USER4_ID; do
  curl -s -X POST http://localhost:8978/api/tenants/$TENANT_ID/users \
    -H "Content-Type: application/json" \
    -d "{\"userId\": \"$USER_ID\", \"role\": \"STANDARD_USER\"}"
done

echo "Family setup complete!"
echo "Tenant ID: $TENANT_ID"
echo "Created 4 users with different roles"
```

## Security Reminders

⚠️ **IMPORTANT SECURITY NOTES:**

1. **No Authentication**: The API endpoints are not secured. Anyone with network access can manage users.
   
2. **Use Reverse Proxy**: Add authentication via Nginx, Traefik, or Caddy:
   ```nginx
   location /api/users {
       auth_basic "Restricted";
       auth_basic_user_file /etc/nginx/.htpasswd;
       proxy_pass http://janitorr:8978;
   }
   ```

3. **Network Isolation**: Restrict access via Docker networks or firewall rules.

4. **Change Default Password**: Immediately change the default admin password.

## Troubleshooting

### Endpoints return 404

Multi-tenancy is not enabled. Check `application.yml`:
```yaml
multitenancy:
  enabled: true
```

### Default admin not created

Check logs for errors. Verify configuration:
```yaml
multitenancy:
  default-admin:
    create-on-startup: true
```

### Can't connect to API

Ensure Janitorr is running and the port is accessible:
```bash
docker ps | grep janitorr
curl http://localhost:8978/api/management/status
```

## Next Steps

- Read the full [Multi-Tenancy Guide](Multi-Tenancy-Guide.md)
- Set up authentication (reverse proxy or Spring Security)
- Configure user profiles and preferences
- Integrate with your existing Janitorr workflows

## Resources

- [English Documentation](Multi-Tenancy-Guide.md)
- [Documentación en Español](../es/Guia-Multi-Tenancy.md)
- [GitHub Issues](https://github.com/carcheky/janitorr/issues)
