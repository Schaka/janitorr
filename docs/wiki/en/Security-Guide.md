# Security Guide

## Overview

Janitorr provides built-in HTTP Basic Authentication to protect API endpoints from unauthorized access. This guide explains how to enable and configure authentication for your Janitorr instance.

## ⚠️ Security Warning

By default, all API endpoints are **publicly accessible without authentication**. This creates a security risk in production environments. You **MUST** secure your Janitorr instance using one of the methods below.

## Security Options

You have three options to secure your Janitorr instance:

### 1. Built-in HTTP Basic Authentication (Recommended)

Enable Janitorr's built-in authentication by configuring `application.yml`:

```yaml
security:
  enabled: true
  username: your-username
  password: your-secure-password
```

**Advantages:**
- Easy to set up
- No additional infrastructure required
- Passwords are securely hashed using BCrypt
- Works with all HTTP clients and browsers

**Disadvantages:**
- Single user authentication only
- Basic authentication over HTTP is not secure (use HTTPS!)
- No advanced features like 2FA or OAuth

### 2. Reverse Proxy Authentication

Use a reverse proxy (Nginx, Traefik, Caddy) with authentication:

**Nginx Example:**
```nginx
location / {
    auth_basic "Janitorr";
    auth_basic_user_file /etc/nginx/.htpasswd;
    proxy_pass http://janitorr:8978;
}
```

**Traefik Example:**
```yaml
http:
  middlewares:
    janitorr-auth:
      basicAuth:
        users:
          - "user:$apr1$..."
  routers:
    janitorr:
      middlewares:
        - janitorr-auth
```

**Advantages:**
- More flexible authentication options
- Can integrate with existing authentication systems
- Better for multiple services
- Can enforce HTTPS

**Disadvantages:**
- Requires additional infrastructure
- More complex setup

### 3. Network-Level Restrictions

Use firewall rules or Docker network isolation to restrict access:

**Docker Compose Example:**
```yaml
services:
  janitorr:
    networks:
      - internal
    # No port mapping to host - only accessible within Docker network

networks:
  internal:
    internal: true
```

**Advantages:**
- Simple and effective
- No authentication overhead
- Works for private/internal deployments

**Disadvantages:**
- Only works for isolated environments
- Not suitable for remote access
- No user-level access control

## Enabling Built-in Authentication

### Step 1: Configure Credentials

Edit your `application.yml`:

```yaml
security:
  enabled: true
  username: admin           # Change this!
  password: super-secret-password  # Change this!
```

**IMPORTANT:** 
- Do NOT use the default credentials (`admin`/`admin`) in production
- Use a strong, unique password
- Janitorr will log a warning if default credentials are detected

### Step 2: Restart Janitorr

Restart the Janitorr container or service:

```bash
docker compose restart janitorr
```

### Step 3: Verify Authentication

Try accessing the API without credentials:

```bash
curl http://localhost:8978/api/management/status
# Should return 401 Unauthorized
```

Try with credentials:

```bash
curl -u admin:super-secret-password http://localhost:8978/api/management/status
# Should return status information
```

## Using Authenticated Endpoints

### Web Browser

When accessing the Management UI (`http://localhost:8978/`), your browser will prompt for username and password.

### curl

Use the `-u` flag:

```bash
curl -u username:password http://localhost:8978/api/management/status
```

Or use the `Authorization` header:

```bash
curl -H "Authorization: Basic $(echo -n 'username:password' | base64)" \
  http://localhost:8978/api/management/status
```

### Python

```python
import requests
from requests.auth import HTTPBasicAuth

response = requests.get(
    'http://localhost:8978/api/management/status',
    auth=HTTPBasicAuth('username', 'password')
)
```

### JavaScript/Node.js

```javascript
const axios = require('axios');

axios.get('http://localhost:8978/api/management/status', {
    auth: {
        username: 'username',
        password: 'password'
    }
})
.then(response => console.log(response.data));
```

## Public Endpoints

The following endpoints remain publicly accessible even when authentication is enabled:

- `/health` - Health check endpoint
- `/actuator/health` - Spring Boot health endpoint
- `/actuator/info` - Application information
- `/` - Management UI static files (HTML, CSS, JS)
- `/index.html` - Main UI page

## Docker Environment Variables

You can override security settings using environment variables:

```yaml
services:
  janitorr:
    environment:
      - SECURITY_ENABLED=true
      - SECURITY_USERNAME=myuser
      - SECURITY_PASSWORD=mypassword
```

## Security Best Practices

### 1. Use HTTPS

HTTP Basic Authentication transmits credentials in base64 encoding (not encrypted). Always use HTTPS in production:

- Use a reverse proxy with SSL/TLS certificates
- Get free certificates from [Let's Encrypt](https://letsencrypt.org/)
- Consider using Cloudflare for SSL termination

### 2. Use Strong Passwords

- Minimum 12 characters
- Mix of uppercase, lowercase, numbers, and symbols
- Don't reuse passwords from other services
- Use a password manager

### 3. Change Default Credentials

Never use the default `admin`/`admin` credentials in production. Janitorr will log warnings if defaults are detected.

### 4. Limit Network Exposure

- Don't expose Janitorr directly to the internet
- Use VPN or SSH tunnels for remote access
- Use firewall rules to restrict access

### 5. Regular Updates

Keep Janitorr updated to receive security patches:

```bash
docker compose pull janitorr
docker compose up -d janitorr
```

### 6. Monitor Access Logs

Check logs for unauthorized access attempts:

```bash
docker compose logs janitorr | grep -i "401\|unauthorized"
```

## Troubleshooting

### "401 Unauthorized" Errors

**Problem:** Getting 401 errors even with correct credentials

**Solutions:**
1. Check that `security.enabled: true` is set in `application.yml`
2. Verify credentials are correct (case-sensitive)
3. Ensure no extra spaces in configuration
4. Check Docker environment variables aren't overriding config
5. Restart Janitorr after configuration changes

### Management UI Login Loop

**Problem:** Browser keeps prompting for credentials

**Solutions:**
1. Clear browser cache and cookies
2. Check browser console for errors
3. Verify static resources are served without authentication
4. Try a different browser

### Health Checks Failing

**Problem:** Docker health checks failing after enabling security

**Solutions:**
Health check endpoints (`/health`, `/actuator/health`) should remain public. If they're being blocked:

1. Check SecurityConfig.kt permits these endpoints
2. Update Docker health check if using custom endpoint
3. Verify `@ConditionalOnProperty` is working correctly

### Configuration Not Applied

**Problem:** Changes to `application.yml` not taking effect

**Solutions:**
1. Restart Janitorr container/service
2. Check YAML syntax (indentation matters!)
3. Verify file is mounted correctly in Docker
4. Check for environment variables overriding config
5. Look for Spring Boot errors in logs

## Multi-Tenancy Integration

If you're using Janitorr's multi-tenancy feature, the built-in authentication provides basic security for the user management endpoints. However, for production use, you should:

1. Enable built-in authentication
2. Implement additional role-based access control (future feature)
3. Use a reverse proxy for advanced authentication

See the [Multi-Tenancy Guide](Multi-Tenancy-Guide.md) for more information.

## Migration from Unsecured Setup

If you're adding authentication to an existing Janitorr instance:

1. **Backup your configuration:**
   ```bash
   cp application.yml application.yml.backup
   ```

2. **Add security configuration:**
   ```yaml
   security:
     enabled: true
     username: your-username
     password: your-password
   ```

3. **Update scripts and automations:**
   - Add authentication to curl commands
   - Update monitoring scripts
   - Configure API clients with credentials

4. **Test before production:**
   - Test with `security.enabled: false` first
   - Verify all functionality works
   - Enable security and test again

5. **Update documentation:**
   - Document credentials securely
   - Update runbooks and procedures
   - Train users on login process

## Future Enhancements

The following security features are planned for future releases:

- **JWT Token Authentication** - For API integrations
- **OAuth 2.0** - Google, GitHub, Discord integration
- **Two-Factor Authentication (2FA)** - Enhanced security
- **Role-Based Access Control** - Fine-grained permissions
- **API Keys** - For automation and integrations
- **Rate Limiting** - Prevent brute force attacks
- **Audit Logging** - Track security events
- **Session Management** - Control active sessions

## Support

If you encounter security issues or have questions:

- Check the [FAQ](FAQ.md)
- Review [Troubleshooting Guide](Troubleshooting.md)
- Open a [GitHub Discussion](https://github.com/carcheky/janitorr/discussions)
- Report security vulnerabilities privately (see SECURITY.md)

## Related Documentation

- [Configuration Guide](Configuration-Guide.md)
- [Docker Compose Setup](Docker-Compose-Setup.md)
- [Multi-Tenancy Guide](Multi-Tenancy-Guide.md)
- [Troubleshooting](Troubleshooting.md)
