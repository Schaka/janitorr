# GitHub Copilot Instructions for Janitorr

## Quick Reference

**Common Commands:**
```bash
./gradlew build              # Build project
./gradlew test               # Run tests
./gradlew bootRun            # Run locally
./gradlew bootBuildImage     # Build Docker image
```

**Key Directories:**
- Code: `src/main/kotlin/com/github/schaka/janitorr/`
- Tests: `src/test/kotlin/com/github/schaka/janitorr/`
- Docs EN: `docs/wiki/en/`
- Docs ES: `docs/wiki/es/`

**Commit Format:** `<type>[(<scope>)]: <subject>` - **ALWAYS use conventional commits** (See [Commit Conventions](#commit-message-conventions))

---

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

## Debugging and Troubleshooting

### Running in Debug Mode
```bash
# Run with debug logging
./gradlew bootRun --args='--logging.level.com.github.schaka.janitorr=DEBUG'

# Run tests with detailed output
./gradlew test --info
```

### Common Build Issues
**Issue**: "Dependency requires at least JVM runtime version 24"
- **Solution**: Ensure you're using JDK 25 (Temurin/Adoptium distribution)
- **Check**: `java -version` should show version 25+

**Issue**: Tests fail in MockK
- **Solution**: Ensure you're using MockK (not Mockito) for Kotlin tests
- **Example**: `mockk<ServiceClass>()` instead of `mock(ServiceClass::class.java)`

**Issue**: Native image build fails
- **Solution**: As of v1.9.0, native images are deprecated. Use JVM image instead
- **Note**: Some Spring Boot features (like Management UI) don't work in native builds

### Debugging Cleanup Logic
```kotlin
// Enable dry-run to see what would be deleted
cleanup.dryRun = true

// Check logs for deletion candidates
log.info("Would delete: ${media.title} (${media.id})")

// Verify rules are applied correctly
log.debug("Expiration rule: $percentWatched% -> $daysOld days")
```

### Container Debugging
```bash
# Check container logs
docker logs janitorr

# Execute shell in running container
docker exec -it janitorr /bin/sh

# Verify configuration is loaded
docker exec janitorr cat /config/application.yml

# Check disk space calculations
docker exec janitorr df -h
```

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

## Commit Message Conventions

**CRITICAL**: This project follows [Conventional Commits](https://www.conventionalcommits.org/) specification. 

**ALL commits MUST follow this format** - including progress reports, internal commits, and any automated commits:

```
<type>[(<scope>)]: <subject>

[optional body]

[optional footer]
```

**Note**: Scope (the part in parentheses) is **optional**. Both formats are valid:
- `feat: add new feature` ✅
- `feat(media): add new feature` ✅

### Valid Types
- `feat`: New feature (triggers minor version bump)
- `fix`: Bug fix (triggers patch version bump)
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Test additions or updates
- `build`: Build system changes
- `ci`: CI/CD changes
- `chore`: Other maintenance tasks
- `revert`: Revert previous commit

### Breaking Changes
Use `!` after type/scope or add `BREAKING CHANGE:` in footer (triggers major version bump)

### Examples
```bash
# With scope
feat(media): add Plex support
fix(cleanup): resolve symlink deletion issue

# Without scope (also valid)
feat: add new feature
fix: resolve bug
docs: update Docker setup guide
chore: update dependencies

# Breaking change
feat(api)!: change response format

BREAKING CHANGE: API structure has changed
```

**CRITICAL REQUIREMENTS**:
1. **NEVER** create commits with messages like "Initial plan", "WIP", "Update", etc.
2. **ALWAYS** use a valid conventional commit type (`feat`, `fix`, `docs`, `chore`, etc.)
3. **ALWAYS** include a meaningful subject after the colon
4. All commits are validated in CI - non-compliant commits will fail the build
5. Use `chore:` for maintenance tasks, `docs:` for documentation updates
6. Scope is optional but recommended for clarity

See [CONTRIBUTING.md](/CONTRIBUTING.md) for complete details.

## Common Development Tasks

### Adding a New Feature
```bash
# 1. Create feature branch
git checkout -b feat/my-feature

# 2. Make changes following project structure
# - Add code in src/main/kotlin/com/github/schaka/janitorr/<feature>/
# - Add tests in src/test/kotlin/com/github/schaka/janitorr/<feature>/
# - Update documentation in both docs/wiki/en/ and docs/wiki/es/

# 3. Build and test
./gradlew build
./gradlew test

# 4. Commit with conventional format
git commit -m "feat(feature): add new feature description"

# 5. Push and create PR
git push origin feat/my-feature
```

### Fixing a Bug
```bash
# 1. Create fix branch
git checkout -b fix/bug-description

# 2. Fix the issue and add regression test
# 3. Verify fix doesn't break existing tests
./gradlew test

# 4. Commit with conventional format
git commit -m "fix(component): resolve specific issue

Fixes #issue-number"
```

### Updating Documentation
```bash
# Update BOTH language versions
# 1. Update docs/wiki/en/File-Name.md
# 2. Update docs/wiki/es/Archivo-Nombre.md
# 3. Verify all links work
# 4. Commit
git commit -m "docs: update documentation topic"
```

### Testing Docker Image Changes
```bash
# Build JVM image
IMAGE_TYPE=jvm ./gradlew bootBuildImage

# Test the image
docker run -p 8080:8080 -v ./application.yml:/config/application.yml \
  ghcr.io/carcheky/janitorr:jvm-latest

# Check logs
docker logs <container-id>
```

## CI/CD Integration

### Automated Workflows
The project uses GitHub Actions for automation:

1. **Commit Validation** (`commit-lint.yml`)
   - Validates all PR commits against conventional commit format
   - Runs on every PR
   - Must pass before merge

2. **Build and Test** (`gradle.yml`)
   - Runs on push and PR
   - Executes `./gradlew build` and `./gradlew test`
   - Tests against JDK 25

3. **Semantic Release** (`.releaserc.json`)
   - Automatically creates releases on main/develop
   - Generates changelog from commit messages
   - Publishes Docker images to GHCR
   - Version determined by commit types

### Release Strategy
- **main branch**: Production releases (v1.0.0, v1.1.0)
- **develop branch**: Pre-releases (v1.1.0-develop.1)
- **Feature branches**: No releases, PR validation only

### Docker Image Tags
After successful release:
- `jvm-stable`: Latest stable JVM image
- `jvm-latest`: Latest build (may be pre-release)
- `jvm-v1.2.3`: Specific version tag
- Native images deprecated as of v1.9.0

## When Unsure

1. Check existing code patterns in the same area
2. Review the Spring Boot documentation
3. Consider the impact on Docker deployment
4. Test with both dry-run enabled and disabled
5. Verify documentation is updated in both languages
6. **ALWAYS ensure commit messages follow conventional format** (even for progress reports)
7. Check if changes affect both JVM and native image builds

## Commit Guidelines for Copilot Agents

**MANDATORY**: When using the `report_progress` tool or any other commit operation:

1. **ALWAYS** use conventional commit format for the commit message
2. **NEVER** use generic messages like "Initial plan", "Update", "WIP", "Progress", etc.
3. Choose the appropriate type based on the work being done:
   - `feat:` for new features
   - `fix:` for bug fixes
   - `docs:` for documentation changes
   - `refactor:` for code refactoring
   - `test:` for test additions/updates
   - `chore:` for maintenance tasks
   - `style:` for formatting changes
   - `perf:` for performance improvements
   - `ci:` for CI/CD changes

4. **Examples of valid progress commit messages**:
   ```bash
   chore: initialize project setup
   docs: add initial implementation plan
   feat: implement basic authentication
   refactor: restructure database layer
   test: add unit tests for cleanup service
   ```

5. **Examples of INVALID commit messages** (will fail CI):
   ```bash
   ❌ Initial plan
   ❌ WIP
   ❌ Update files
   ❌ Progress report
   ❌ Commit changes
   ```

**Remember**: Every single commit in this repository is validated by commitlint in CI. Non-compliant commits will cause PR checks to fail.
