# GitHub Copilot Instructions for Janitorr

## Project Overview

Janitorr is a media library cleanup automation tool for Jellyfin and Emby media servers. It integrates with Sonarr/Radarr (*arr services) and Jellyseerr to automatically manage and clean up unwatched or old media based on configurable rules.

## Tech Stack

- **Language**: Kotlin 2.2.20
- **Framework**: Spring Boot 3.5.6
- **Build Tool**: Gradle 8.x with Kotlin DSL
- **Java Version**: JDK 25 (Adoptium)
- **Testing**: JUnit 5 + MockK
- **Containerization**: Docker (JVM and Native GraalVM images)
- **HTTP Client**: OpenFeign
- **Caching**: Caffeine

## Build and Test Commands

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew test
```

### Running Locally
```bash
./gradlew bootRun
```

### Docker Image Building
```bash
# JVM Image
IMAGE_TYPE=jvm ./gradlew bootBuildImage

# Native Image (deprecated as of v1.9.0)
IMAGE_TYPE=native ./gradlew bootBuildImage
```

## Code Style and Conventions

### Kotlin Code Style
- Use Kotlin idiomatic style
- Prefer data classes for DTOs and data structures
- Use Spring Boot annotations (@Component, @Service, @RestController, etc.)
- Follow Spring Boot configuration patterns with `@ConfigurationProperties`
- Use constructor injection over field injection
- Leverage Kotlin null-safety features

### File Organization
- Main source: `src/main/kotlin/com/github/schaka/janitorr/`
- Tests: `src/test/kotlin/com/github/schaka/janitorr/`
- Resources: `src/main/resources/`
- Package structure by feature (e.g., `mediaserver/`, `servarr/`, `cleanup/`, `jellyseerr/`)

### Testing
- Use JUnit 5 for test framework
- Use MockK for mocking (not Mockito)
- Test file naming: `*Test.kt`
- Place tests in the same package structure as the code they test

## Documentation

### Bilingual Documentation
The project maintains documentation in **both English and Spanish**:
- English: `docs/wiki/en/`
- Spanish: `docs/wiki/es/`

When updating documentation:
1. **Always update both language versions**
2. Maintain consistent structure across languages
3. Update cross-references in both versions
4. Test all internal links

### Documentation Files
- **Docker Setup**: `docs/wiki/en/Docker-Compose-Setup.md` and `docs/wiki/es/Configuracion-Docker-Compose.md`
- **Configuration**: `docs/wiki/en/Configuration-Guide.md` and `docs/wiki/es/Guia-Configuracion.md`
- **FAQ**: `docs/wiki/en/FAQ.md` and `docs/wiki/es/Preguntas-Frecuentes.md`
- **Troubleshooting**: `docs/wiki/en/Troubleshooting.md` and `docs/wiki/es/Solucion-Problemas.md`

## Docker and Deployment

### Image Types
- **JVM Image** (recommended): `ghcr.io/carcheky/janitorr:jvm-stable`
- **Native Image** (deprecated v1.9.0+): `ghcr.io/carcheky/janitorr:native-stable`

### Configuration
- Application configuration: `application.yml`
- Must be mounted at `/config/application.yml` in container
- Template: `src/main/resources/application-template.yml`
- Supports Spring Boot AOT for faster startup

### Key Environment Variables
- `THC_PATH`: Health check path (default: `/health`)
- `THC_PORT`: Health check port (default: `8081`)
- `SPRING_CONFIG_ADDITIONAL_LOCATION`: Additional config locations

## Important Concepts

### Dry-Run Mode
- Default mode: **dry-run enabled** (no deletions)
- Must be explicitly disabled in configuration to perform actual deletions
- Always test in dry-run mode first

### Integration Points
1. **Media Servers**: Jellyfin or Emby
2. ***arr Services**: Sonarr (TV shows) and Radarr (movies)
3. **Request Management**: Jellyseerr or Overseerr
4. **Statistics**: Tautulli or Streamystats (optional)

### Path Mapping
- Paths must be consistent between Janitorr, media servers, and *arr services
- Use Docker volume mapping to ensure path consistency
- Example: If Jellyfin sees `/library/movies`, Janitorr must see the same path

## Development Guidelines

### When Adding Features
1. Consider impact on both JVM and native images
2. Update relevant documentation (both languages)
3. Add tests using MockK
4. Follow Spring Boot best practices
5. Consider dry-run mode behavior

### Configuration Properties
- Use `@ConfigurationProperties` for configuration classes
- Provide sensible defaults
- Document all properties in `application-template.yml`
- Use nested configuration objects for organization

### API Development
- REST endpoints under `/api/`
- Management UI endpoints under `/api/management/`
- Follow RESTful conventions
- Return appropriate HTTP status codes

### Logging
- Use SLF4J for logging
- Provide meaningful log messages
- Include context in log messages (media IDs, titles, etc.)
- Use appropriate log levels (DEBUG, INFO, WARN, ERROR)

## Common Pitfalls to Avoid

1. **Don't break dry-run mode** - Always ensure dry-run checks are respected
2. **Path consistency** - Verify paths work across container boundaries
3. **Native image compatibility** - Some Spring features don't work in native images
4. **Memory constraints** - JVM image needs minimum 200MB, recommended 256MB
5. **Bilingual docs** - Never update only one language version

## Management UI

- Web-based UI at `http://<host>:<port>/`
- Provides manual cleanup triggers
- Shows system status and configuration
- No authentication by default (use reverse proxy if needed)
- Excluded from native image builds (leyden profile)

## Testing Strategy

### Unit Tests
- Test business logic in isolation
- Mock external dependencies (media servers, *arr services)
- Use MockK for Kotlin-friendly mocking

### Integration Tests
- Test Spring Boot application context
- Verify configuration binding
- Test REST endpoints

### What to Test
- Cleanup logic and rules
- Configuration validation
- API endpoints
- Error handling
- Path resolution

## Project Structure

```
janitorr/
├── src/
│   ├── main/
│   │   ├── kotlin/com/github/schaka/janitorr/
│   │   │   ├── api/              # REST controllers
│   │   │   ├── cleanup/          # Cleanup logic
│   │   │   ├── config/           # Configuration
│   │   │   ├── jellyseerr/       # Jellyseerr integration
│   │   │   ├── mediaserver/      # Jellyfin/Emby integration
│   │   │   ├── servarr/          # Sonarr/Radarr integration
│   │   │   └── stats/            # Statistics integration
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-template.yml
│   │       └── static/           # Management UI files
│   └── test/
├── docs/
│   └── wiki/
│       ├── en/                   # English documentation
│       └── es/                   # Spanish documentation
├── examples/
│   └── example-compose.yml       # Full stack example
├── buildpacks/                   # Custom buildpacks
└── images/                       # Project images/logos
```

## Additional Resources

- **Main README**: `/README.md`
- **Wiki Documentation Guide**: `/WIKI_DOCUMENTATION.md`
- **Management UI Guide**: `/MANAGEMENT_UI.md`
- **Example Docker Compose**: `/examples/example-compose.yml`
- **GitHub Discussions**: For community support
- **Docker Images**: `ghcr.io/carcheky/janitorr`

## When Unsure

1. Check existing code patterns in the same area
2. Review the Spring Boot documentation
3. Consider the impact on Docker deployment
4. Test with both dry-run enabled and disabled
5. Verify documentation is updated in both languages
