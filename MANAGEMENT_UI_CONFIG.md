# Management UI Configuration - Quick Reference

This document provides a quick reference for the Management UI configuration feature added to Janitorr.

## What's New

Janitorr now supports explicit configuration to enable/disable the Management UI through environment variables and configuration files.

## Quick Start

### Enable the UI (Default)

```yaml
# docker-compose.yml
services:
  janitorr:
    environment:
      - JANITORR_UI_ENABLED=true
    ports:
      - "8080:8080"
```

### Disable the UI

```yaml
# docker-compose.yml
services:
  janitorr:
    environment:
      - JANITORR_UI_ENABLED=false
    # No need to expose port 8080
```

## Configuration Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JANITORR_UI_ENABLED` | `true` | Enable/disable the Management UI |
| `SERVER_PORT` | `8080` | Internal application port |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | `health,info,management` | Control which API endpoints are exposed |
| `SPRING_PROFILES_ACTIVE` | - | Spring Boot profiles (UI disabled on `leyden` profile) |

## Configuration Methods

### Method 1: Environment Variables (Docker - Recommended)

```yaml
environment:
  - JANITORR_UI_ENABLED=true
  - SERVER_PORT=8080
  - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,management
```

### Method 2: Application Configuration File

```yaml
# application.yml
management:
  ui:
    enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,management
```

### Method 3: Command Line

```bash
java -jar janitorr.jar --management.ui.enabled=true
```

## Startup Logging

Janitorr now logs the UI status on startup:

### When Enabled:
```
INFO - Management UI is ENABLED and available at http://localhost:8080/
INFO - Management API endpoints available at http://localhost:8080/api/management/
```

### When Disabled:
```
INFO - Management UI is DISABLED by configuration (management.ui.enabled=false)
```

### When Using Leyden Profile:
```
INFO - Management UI is DISABLED (leyden profile active - native image compilation)
```

## Troubleshooting

### UI Not Accessible

1. Check logs: `docker logs janitorr | grep "Management UI"`
2. Verify environment: `docker exec janitorr printenv | grep JANITORR_UI_ENABLED`
3. Check port mapping in `docker-compose.yml`
4. Verify configuration: `docker exec janitorr cat /config/application.yml | grep -A 3 "management:"`

### UI Shows 404

1. Enable UI: Set `JANITORR_UI_ENABLED=true`
2. Check profiles: Ensure `leyden` profile is not active
3. Restart container: `docker-compose restart janitorr`

## Use Cases

### When to Enable the UI:
- You want to manually trigger cleanups
- You need to monitor system status visually
- You're testing or debugging configurations
- You prefer a web interface over logs

### When to Disable the UI:
- Headless server environments with scheduled cleanups only
- Security concerns about web interface exposure
- CI/CD pipelines or automated scripts
- Minimal resource usage requirements

## Files Changed

This feature adds/modifies:
- Configuration: `ManagementUiProperties.kt`, `ManagementUiLogger.kt`
- Controller: `ManagementController.kt` (conditional annotation added)
- Config Files: `application.yml`, `application-template.yml`
- Examples: `docker-compose.example.ui.yml`, `example-compose.yml`
- Documentation: MANAGEMENT_UI.md, Configuration Guide, Troubleshooting (EN & ES)
- Tests: `ManagementUiPropertiesTest.kt`, `ManagementUiLoggerTest.kt`

## Backwards Compatibility

✅ **Fully backwards compatible**
- Default value is `true` (UI enabled)
- Existing deployments continue to work without changes
- Configuration is optional - not required

## Documentation

For detailed documentation, see:
- [MANAGEMENT_UI.md](MANAGEMENT_UI.md) - Complete UI documentation
- [Configuration Guide](docs/wiki/en/Configuration-Guide.md) - Full configuration reference
- [Troubleshooting](docs/wiki/en/Troubleshooting.md) - Common issues and solutions
- [Guía de Configuración](docs/wiki/es/Guia-Configuracion.md) - Spanish configuration guide
- [Solución de Problemas](docs/wiki/es/Solucion-Problemas.md) - Spanish troubleshooting

## Examples

### Full Stack with UI
See [example-compose.yml](examples/example-compose.yml) for a complete example.

### UI-Only Example
See [docker-compose.example.ui.yml](examples/docker-compose.example.ui.yml) for UI-focused setup.

## Support

For issues or questions:
1. Check the troubleshooting guides
2. Review startup logs
3. Visit GitHub Discussions
4. Report bugs via GitHub Issues
