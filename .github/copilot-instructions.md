# 📋 GitHub Copilot Instructions - Janitorr Project

## 🚨 CRITICAL RULES - MANDATORY READING

### ✅ Conventional Commits - NO EXCEPTIONS

**ALL commits MUST follow this format:**
```
<type>[(<scope>)]: <subject>
```

**Valid types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`

**Correct examples:**
- `feat: add new functionality`
- `fix(cleanup): resolve syntax error`
- `docs: update documentation`

**NEVER use:** "Update", "WIP", "Initial plan", "Merge" - These commits will be automatically rejected.

### 🔧 Available MCP Tools

**For file operations:**
- `@activate_filesystem_management_tools` - Create/read/edit/move files and directories
  - When to use: Manipulate code files, configuration, documentation
  - Examples: Create new Kotlin files, edit YAML configurations, reorganize structure

**For terminal commands:**
- `@activate_mcp_shell_tools` - Execute shell commands, manage processes
  - When to use: Builds, tests, git operations, dependency installation
  - Examples: `gradle build`, `git commit`, `docker run`, process management

**For external documentation:**
- `@mcp_upstash_conte_get-library-docs` - Get updated library documentation
  - When to use: Need API/syntax reference for Spring Boot, Kotlin, etc.
  - Examples: Spring Security configuration, new Kotlin features, testing APIs

**For context management:**
- `@activate_knowledge_graph_tools` - Persistent memory between sessions
  - When to use: Remember architectural decisions, used patterns, resolved issues
  - Examples: Save solutions to complex problems, successful integration patterns

**For web analysis:**
- `@mcp_fetch_fetch_url` - Get content from web pages and APIs
  - When to use: Analyze external documentation, REST APIs, verify endpoints
  - Examples: Verify Spring Boot docs, analyze Jellyfin/Sonarr APIs

**For web automation:**
- `@activate_browser_interaction_tools` - Browser control with Playwright
  - When to use: Web UI testing, web task automation, screenshots
  - Examples: Management UI testing, web interface validation

**For structured thinking:**
- `@mcp_sequential-th_sequentialthinking` - Step-by-step analysis of complex problems
  - When to use: Complex debugging, architectural analysis, issue resolution
  - Examples: Diagnose integration failures, plan refactorings

**For GitHub management:**
- `@activate_github_tools_issue_management` - Complete issues and PRs management
  - When to use: Create/update issues, manage PRs, automated reviews
  - Examples: Create issues for found bugs, manage development workflow

### 🐳 Docker Usage for Tools

**ALWAYS use Docker for:**
- Java/Gradle executions (JDK 25)
- Application builds
- Unit and integration tests
- Any project-specific tools

**Recommended image:** `gradle:8-jdk25`

---

## 📁 PROJECT CONTEXT

### What is Janitorr
Automation tool for media cleanup on Jellyfin/Emby servers. Integrates with Sonarr/Radarr (*arr) and Jellyseerr to automatically remove unwatched or old content according to configurable rules.

### Technology Stack
- **Language:** Kotlin 2.2.20
- **Framework:** Spring Boot 3.5.6  
- **Build:** Gradle 8.x with Kotlin DSL
- **Java:** JDK 25 (Adoptium)
- **Testing:** JUnit 5 + MockK (NO Mockito)
- **Docker:** JVM images (native deprecated since v1.9.0)

### Code Structure
```
src/main/kotlin/com/github/schaka/janitorr/
├── mediaserver/     # Media server integration
├── servarr/         # *arr integration (Sonarr/Radarr)  
├── cleanup/         # Main cleanup logic
├── jellyseerr/      # Jellyseerr integration
├── notifications/   # Notification system
├── multitenancy/    # Multi-tenant support
└── config/          # Configurations
```

---

## 💻 CODE RULES

### Kotlin Style
- Use data classes for DTOs
- Constructor injection preferred over field injection
- Use `@ConfigurationProperties` for configurations
- Avoid `@Autowired` in fields, use constructor injection
- If there are circular dependencies, use `@Lazy` in constructor

### Testing
- **ALWAYS** use MockK, NEVER Mockito for Kotlin code
- Test names in backticks: `` `should do something when condition` ``
- One test per behavior, not per method

### Spring Boot
- Use `@Component`, `@Service`, `@RestController` appropriately
- Prefer `@ConfigurationProperties` over `@Value`
- Use profiles to separate build (`leyden`) vs runtime configurations

---

## 🐳 DOCKER CONFIGURATION

### Image Types
- **JVM** (recommended): `ghcr.io/carcheky/janitorr:latest`
- **Native** (deprecated v1.9.0+): Don't use for new developments

### Important Environment Variables
- `THC_PATH=/health` - Health check path
- `THC_PORT=8081` - Health check port  
- `SPRING_CONFIG_ADDITIONAL_LOCATION` - Additional config location

---

## 📚 DOCUMENTATION

### Bilingual Rule
**ALWAYS update both languages simultaneously:**
- `docs/wiki/en/` - English version
- `docs/wiki/es/` - Spanish version

Maintain the same file structure and links between both versions.

---

## ⚙️ KEY CONCEPTS

### Dry-Run Mode
- **ENABLED by default**
- Only shows what it would do, doesn't execute deletions
- ALWAYS test in dry-run before disabling
- Code must respect this flag in all destructive operations

### Path Mapping
**CRITICAL:** Paths must be identical between:
- Janitorr
- Media server (Jellyfin/Emby)  
- *arr services (Sonarr/Radarr)

If Jellyfin sees `/library/movies`, Janitorr MUST see exactly `/library/movies`.

### Cleanup Flow
1. Analyze media according to retention rules
2. Mark elements for deletion  
3. If NOT dry-run, execute deletion
4. Send result notifications
5. Update metrics and statistics

---

## 🚧 LOCAL DEVELOPMENT

### Build Commands
```bash
# USE DOCKER for Java/Gradle executions
docker run --rm -v $(pwd):/workspace -w /workspace gradle:8-jdk25 gradle build # Build
docker run --rm -v $(pwd):/workspace -w /workspace gradle:8-jdk25 gradle test # Tests
docker run --rm -v $(pwd):/workspace -w /workspace gradle:8-jdk25 gradle bootRun # Run local
docker run --rm -v $(pwd):/workspace -w /workspace gradle:8-jdk25 gradle bootBuildImage # Docker image
```

### Troubleshooting
- **"JVM runtime version 24"**: Use `docker run` with JDK 25 (Temurin)
- **MockK tests fail**: Verify you're NOT using Mockito
- **Native build fails**: Use JVM, native is deprecated
- **Missing Java/Gradle**: ALWAYS use `docker run` for consistency

---

## 🎯 WHEN DEVELOPING

### New Features
- Create branch `feat/short-description`
- Add tests for new functionality
- Update EN and ES documentation if necessary
- Respect dry-run mode in destructive operations
- Use conventional commits

### Bug Fixes  
- Create branch `fix/bug-description`
- Include regression test
- Verify you don't break existing tests
- Document the fix in commit message

### Documentation Changes
- Update BOTH versions (EN/ES) simultaneously
- Verify internal links work
- Maintain consistent structure between languages

---

## ❌ NEVER DO THIS

- ❌ Commits that don't follow conventional format
- ❌ Use Mockito instead of MockK  
- ❌ Break dry-run functionality
- ❌ Update only one documentation version
- ❌ Assume different paths between services
- ❌ Field injection with `@Autowired` without `@Lazy`
- ❌ Ignore failing tests

---

## ✅ ALWAYS DO THIS

- ✅ Conventional commits in ALL commits
- ✅ Tests with MockK for Kotlin code
- ✅ Constructor injection in Spring classes
- ✅ Respect dry-run mode in destructive operations
- ✅ Consistent paths between all services
- ✅ Updated bilingual documentation
- ✅ Verify tests pass before commit

---

*📅 Last updated: October 12, 2025*
*🤖 These instructions are MANDATORY for GitHub Copilot agents. Read them completely before any changes.*

### 🔧 Herramientas MCP Disponibles

**Para operaciones con archivos:**
- `@activate_filesystem_management_tools` - Crear/leer/editar/mover archivos y directorios
  - Cuándo usar: Manipular archivos de código, configuración, documentación
  - Ejemplos: Crear nuevos archivos Kotlin, editar configuraciones YAML, reorganizar estructura

**Para comandos de terminal:**
- `@activate_mcp_shell_tools` - Ejecutar comandos shell, gestionar procesos
  - Cuándo usar: Builds, tests, git operations, instalación dependencias
  - Ejemplos: `gradle build`, `git commit`, `docker run`, gestión de procesos

**Para documentación externa:**
- `@mcp_upstash_conte_get-library-docs` - Obtener docs actualizadas de librerías
  - Cuándo usar: Necesitas referencia API/sintaxis de Spring Boot, Kotlin, etc.
  - Ejemplos: Configuración Spring Security, nuevas features Kotlin, APIs de testing

**Para gestión de contexto:**
- `@activate_knowledge_graph_tools` - Memoria persistente entre sesiones
  - Cuándo usar: Recordar decisiones arquitecturales, patrones utilizados, issues resueltos
  - Ejemplos: Guardar soluciones a problemas complejos, patrones de integración exitosos

**Para análisis web:**
- `@mcp_fetch_fetch_url` - Obtener contenido de páginas web y APIs
  - Cuándo usar: Analizar documentación externa, APIs REST, verificar endpoints
  - Ejemplos: Verificar docs Spring Boot, analizar APIs de Jellyfin/Sonarr

**Para automatización web:**
- `@activate_browser_interaction_tools` - Control de navegador con Playwright
  - Cuándo usar: Testing de UI web, automatización de tareas web, capturas
  - Ejemplos: Testing del Management UI, validación de interfaces web

**Para pensamiento estructurado:**
- `@mcp_sequential-th_sequentialthinking` - Análisis paso a paso de problemas complejos
  - Cuándo usar: Debugging complejo, análisis arquitectural, resolución de issues
  - Ejemplos: Diagnosticar fallos de integración, planificar refactorizaciones

**Para gestión GitHub:**
- `@activate_github_tools_issue_management` - Gestión completa de issues y PRs
  - Cuándo usar: Crear/actualizar issues, gestionar PRs, reviews automatizadas
  - Ejemplos: Crear issues por bugs encontrados, gestionar workflow de desarrollo

### 🐳 Uso de Docker para Herramientas

**SIEMPRE usa Docker para:**
- Ejecuciones Java/Gradle (JDK 25)
- Builds de aplicación
- Tests unitarios e integración
- Cualquier herramienta específica del proyecto

**Imagen recomendada:** `gradle:8-jdk25`

---

## 📁 CONTEXTO DEL PROYECTO

### Qué es Janitorr
Herramienta de automatización para limpieza de medios en servidores Jellyfin/Emby. Integra con Sonarr/Radarr (*arr) y Jellyseerr para eliminar automáticamente contenido no visto o antiguo según reglas configurables.

### Stack Tecnológico
- **Lenguaje:** Kotlin 2.2.20
- **Framework:** Spring Boot 3.5.6  
- **Build:** Gradle 8.x con Kotlin DSL
- **Java:** JDK 25 (Adoptium)
- **Testing:** JUnit 5 + MockK (NO Mockito)
- **Docker:** Imágenes JVM (nativas deprecated desde v1.9.0)

### Estructura de Código
```
src/main/kotlin/com/github/schaka/janitorr/
├── mediaserver/     # Integración con servidores de medios
├── servarr/         # Integración con *arr (Sonarr/Radarr)  
├── cleanup/         # Lógica principal de limpieza
├── jellyseerr/      # Integración con Jellyseerr
├── notifications/   # Sistema de notificaciones
├── multitenancy/    # Soporte multi-inquilino
└── config/          # Configuraciones
```

---

## 💻 REGLAS DE CÓDIGO

### Estilo Kotlin
- Usa data classes para DTOs
- Constructor injection preferido sobre field injection
- Usa `@ConfigurationProperties` para configuraciones
- Evita `@Autowired` en campos, usa constructor injection
- Si hay dependencias circulares, usa `@Lazy` en constructor

### Testing
- **SIEMPRE** usa MockK, NUNCA Mockito para código Kotlin
- Nombres de test en backticks: `` `should do something when condition` ``
- Un test por comportamiento, no por método

### Spring Boot
- Usa `@Component`, `@Service`, `@RestController` apropiadamente
- Prefiere `@ConfigurationProperties` sobre `@Value`
- Usa perfiles para separar configuraciones de build (`leyden`) vs runtime

---

## 🐳 CONFIGURACIÓN DOCKER

### Tipos de Imagen
- **JVM** (recomendada): `ghcr.io/carcheky/janitorr:latest`
- **Nativa** (deprecated v1.9.0+): No usar para nuevos desarrollos

### Variables de Entorno Importantes
- `THC_PATH=/health` - Health check path
- `THC_PORT=8081` - Health check port  
- `SPRING_CONFIG_ADDITIONAL_LOCATION` - Ubicación de config adicional

---

## � DOCUMENTACIÓN

### Regla Bilingüe
**SIEMPRE actualiza ambos idiomas simultáneamente:**
- `docs/wiki/en/` - Versión en inglés
- `docs/wiki/es/` - Versión en español

Mantén la misma estructura de archivos y enlaces entre ambas versiones.

---

## ⚙️ CONCEPTOS CLAVE

### Modo Dry-Run
- **Por defecto está HABILITADO**
- Solo muestra lo que haría, no ejecuta eliminaciones
- SIEMPRE probar en dry-run antes de deshabilitar
- Código debe respetar este flag en todas las operaciones destructivas

### Path Mapping
**CRÍTICO:** Los paths deben ser idénticos entre:
- Janitorr
- Servidor de medios (Jellyfin/Emby)  
- Servicios *arr (Sonarr/Radarr)

Si Jellyfin ve `/library/movies`, Janitorr DEBE ver exactamente `/library/movies`.

### Flujo de Limpieza
1. Analizar medios según reglas de retención
2. Marcar elementos para eliminación  
3. Si NO es dry-run, ejecutar eliminación
4. Enviar notificaciones de resultados
5. Actualizar métricas y estadísticas

---

## 🚧 DESARROLLO LOCAL

### Comandos de Build
```bash
# USAR DOCKER para ejecuciones Java/Gradle
docker run --rm -v $(pwd):/workspace -w /workspace gradle:8-jdk25 gradle build # Construir
docker run --rm -v $(pwd):/workspace -w /workspace gradle:8-jdk25 gradle test # Tests
docker run --rm -v $(pwd):/workspace -w /workspace gradle:8-jdk25 gradle bootRun # Ejecutar local
docker run --rm -v $(pwd):/workspace -w /workspace gradle:8-jdk25 gradle bootBuildImage # Docker image
```

### Solución de Problemas
- **"JVM runtime version 24"**: Usar `docker run` con JDK 25 (Temurin)
- **Tests fallan MockK**: Verificar que NO uses Mockito
- **Build nativo falla**: Usar JVM, nativo está deprecated
- **Missing Java/Gradle**: SIEMPRE usar `docker run` para consistencia

---

## 🎯 CUANDO DESARROLLES

### Nuevas Funcionalidades
- Crea branch `feat/descripcion-corta`
- Añade tests para nueva funcionalidad
- Actualiza documentación EN y ES si es necesario
- Respeta modo dry-run en operaciones destructivas
- Usa conventional commits

### Corrección de Bugs  
- Crea branch `fix/descripcion-bug`
- Incluye test de regresión
- Verifica que no rompes tests existentes
- Documenta el fix en commit message

### Cambios de Documentación
- Actualiza AMBAS versiones (EN/ES) simultáneamente
- Verifica que enlaces internos funcionen
- Mantén estructura consistente entre idiomas

---

## ❌ NUNCA HAGAS ESTO

- ❌ Commits que no sigan conventional format
- ❌ Usar Mockito en lugar de MockK  
- ❌ Romper la funcionalidad de dry-run
- ❌ Actualizar solo una versión de documentación
- ❌ Asumir paths diferentes entre servicios
- ❌ Field injection con `@Autowired` sin `@Lazy`
- ❌ Ignorar tests fallidos

---

## ✅ SIEMPRE HAZ ESTO

- ✅ Conventional commits en TODOS los commits
- ✅ Tests con MockK para código Kotlin
- ✅ Constructor injection en clases Spring
- ✅ Respetar modo dry-run en operaciones destructivas
- ✅ Paths consistentes entre todos los servicios
- ✅ Documentación bilingüe actualizada
- ✅ Verificar que tests pasan antes de commit

---

*📅 Última actualización: 12 de octubre, 2025*
*🤖 Estas instrucciones son OBLIGATORIAS para agentes GitHub Copilot. Léelas completamente antes de cualquier cambio.*
