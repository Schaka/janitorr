# 📋 Janitorr - Guía de Desarrollo Esquematizada

## 🚨 REGLAS CRÍTICAS OBLIGATORIAS

### ✅ Conventional Commits - REQUERIDO

**Formato:**

```text
<type>[(<scope>)]: <subject>
```

**Ejemplos válidos:**

- ✅ `feat: add new feature`
- ✅ `fix(cleanup): resolve issue`
- ✅ `docs: update documentation`

### 🔧 MCP Servers Configurados

**Servidores disponibles:**

- **GitHub**: `github/github-mcp-server`
- **Memory**: `@modelcontextprotocol/server-memory`
- **Shell**: `@mako10k/mcp-shell-server`
- **Filesystem**: custom wrapper (WSL support)
- **Context7**: `upstash/context7`
- **Sequential Thinking**: `@modelcontextprotocol/server-sequential-thinking`
- **Fetch**: `fetch-mcp`
- **Playwright**: `microsoft/playwright-mcp`

**Comandos de activación:**

```bash
@activate_filesystem_management_tools  # Operaciones de archivos
@activate_mcp_shell_tools             # Comandos de terminal
@mcp_upstash_conte_get-library-docs   # Documentación
@activate_knowledge_graph_tools       # Memoria y contexto
```

---

## 📁 ESTRUCTURA DEL PROYECTO

### 🏗️ Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| Lenguaje | Kotlin 2.2.20 |
| Framework | Spring Boot 3.5.6 |
| Build | Gradle 8.x (Kotlin DSL) |
| Java | JDK 25 (Adoptium) |
| Testing | JUnit 5 + MockK |
| Docker | JVM + Native GraalVM |
| HTTP | OpenFeign |
| Cache | Caffeine |

### 📂 Directorios Principales

```text
src/main/kotlin/com/github/schaka/janitorr/  # Código principal
src/test/kotlin/com/github/schaka/janitorr/  # Tests
docs/wiki/en/                               # Documentación inglés
docs/wiki/es/                               # Documentación español
```

### 🔨 Comandos Básicos

```bash
./gradlew build              # Construir proyecto
./gradlew test               # Ejecutar tests
./gradlew bootRun            # Ejecutar localmente
./gradlew bootBuildImage     # Crear imagen Docker
```

**Docker Images:**

```bash
# JVM Image (recomendada)
IMAGE_TYPE=jvm ./gradlew bootBuildImage

# Native Image (deprecated v1.9.0+)
IMAGE_TYPE=native ./gradlew bootBuildImage
```

---

## 🎯 PROPÓSITO Y FUNCIONALIDAD

### Janitorr - Automatización de Limpieza de Medios

**Función principal:**

Herramienta de automatización para servidores de medios Jellyfin/Emby que integra con servicios *arr (Sonarr/Radarr) y Jellyseerr para limpiar automáticamente medios no vistos o antiguos.

**Integraciones:**

1. **Servidores de Medios**: Jellyfin, Emby
2. **Servicios *arr**: Sonarr (TV), Radarr (películas)
3. **Gestión de Requests**: Jellyseerr, Overseerr
4. **Estadísticas**: Tautulli, Streamystats (opcional)

---

## 💻 DESARROLLO

### 🎨 Estilo de Código Kotlin

```kotlin
// Usar estilo idiomático de Kotlin
data class Example(val prop: String)  // Data classes para DTOs

// Spring Boot annotations
@Component, @Service, @RestController

// Constructor injection preferido
class Service(private val dependency: Dependency)

// Configuración con @ConfigurationProperties
@ConfigurationProperties("app")
data class Properties(val setting: String)
```

### 📁 Organización por Características

```text
├── mediaserver/     # Integración servidores
├── servarr/         # Integración *arr
├── cleanup/         # Lógica de limpieza
├── jellyseerr/      # Integración Jellyseerr
├── notifications/   # Sistema notificaciones
└── multitenancy/    # Soporte multi-tenancy
```

### 🧪 Testing con MockK

```kotlin
// Framework: JUnit 5 + MockK (NO Mockito)
class ServiceTest {
    private val mock = mockk<Dependency>()
    
    @Test
    fun `should do something`() {
        // Test implementation
    }
}
```

---

## 📚 DOCUMENTACIÓN BILINGÜE

### 🌍 Mantenimiento EN/ES

**SIEMPRE actualizar ambos idiomas:**

- `docs/wiki/en/` - Inglés
- `docs/wiki/es/` - Español

**Mantener:**

- Estructura consistente
- Referencias cruzadas
- Enlaces internos funcionando

### 📄 Archivos Documentación Clave

| Tipo | Inglés | Español |
|------|--------|---------|
| Docker Setup | `Docker-Compose-Setup.md` | `Configuracion-Docker-Compose.md` |
| Configuration | `Configuration-Guide.md` | `Guia-Configuracion.md` |
| FAQ | `FAQ.md` | `Preguntas-Frecuentes.md` |
| Troubleshooting | `Troubleshooting.md` | `Solucion-Problemas.md` |

---

## 🐳 DOCKER Y DEPLOYMENT

### 🏷️ Tipos de Imagen

```bash
# JVM Image (RECOMENDADA)
ghcr.io/carcheky/janitorr:latest

# Native Image (DEPRECATED v1.9.0+)
ghcr.io/carcheky/janitorr-native:latest
```

### 🔧 Configuración Docker

```yaml
# Montar configuración en:
/config/application.yml

# Variables de entorno clave:
THC_PATH: /health                        # Health check path
THC_PORT: 8081                          # Health check port
SPRING_CONFIG_ADDITIONAL_LOCATION: ...  # Config adicional
```

### 🚀 Tags Docker Disponibles

| Tag | Descripción |
|-----|-------------|
| `latest` | Última versión estable JVM |
| `main` | Último build de main branch |
| `develop` | Build de desarrollo |
| `1.x.x` | Versión específica |

---

## ⚙️ CONCEPTOS FUNDAMENTALES

### 🔒 Modo Dry-Run

**Configuración por defecto:**

- DRY-RUN HABILITADO por defecto
- Debe ser deshabilitado explícitamente
- SIEMPRE probar en dry-run primero
- No realiza eliminaciones hasta confirmar

### 🗺️ Path Mapping - CRÍTICO

**Requisito:** Paths consistentes entre:

- Janitorr
- Servidores de medios
- Servicios *arr

**Ejemplo:**

```text
Jellyfin ve: /library/movies
Janitorr ve: /library/movies (mismo path)
```

### 🔄 Flujo de Trabajo

1. Analizar medios no vistos/antiguos
2. Aplicar reglas de retención
3. Marcar para eliminación
4. Ejecutar limpieza (si no dry-run)
5. Notificar resultados
6. Actualizar estadísticas

---

## 🚧 DESARROLLO LOCAL

### 🔍 Solución Issues MCP Comunes

**Filesystem Server "No valid root directories":**

✅ **RESUELTO**: Usar wrapper en `/home/user/filesystem-wrapper.js`

**WSL Path Issues:**

✅ Usar paths Linux: `/home/user/...` (NO Windows: `C:\Users\...`)

**Server Not Starting:**

1. Check VS Code Output panel → "MCP"
2. Restart VS Code completamente
3. Verificar Node.js en WSL: `wsl node --version`

### 📍 Configuración MCP

```json
// Global Config: %APPDATA%\Code\User\mcp.json (Windows)
// Custom Wrapper: /home/user/filesystem-wrapper.js
// NO usar: .vscode/mcp.json (workspace-specific)
```

---

## 🐛 DEBUGGING Y TROUBLESHOOTING

### 🔧 Modo Debug

```bash
# Logging detallado
./gradlew bootRun --args='--logging.level.com.github.schaka.janitorr=DEBUG'

# Tests con output detallado
./gradlew test --info
```

### ❌ Issues Comunes y Soluciones

| Error | Solución |
|-------|----------|
| "Dependency requires JVM runtime version 24" | Usar JDK 25 (Temurin/Adoptium) |
| Tests fail en MockK | Usar MockK (NO Mockito) para Kotlin |
| Native image build fails | v1.9.0+ deprecated native images, usar JVM |

### 🏠 Management UI

**Acceso:** `http://<host>:<port>/`

**Características:**

- UI web para triggers manuales
- Estado del sistema y configuración
- Sin autenticación por defecto
- Excluido de builds native (perfil leyden)

---

## 📋 COMMITS Y CI/CD

### 📝 Tipos Conventional Commits

| Tipo | Descripción | Version Bump |
|------|-------------|--------------|
| `feat` | Nueva característica | minor |
| `fix` | Bug fix | patch |
| `docs` | Cambios documentación | - |
| `style` | Cambios formato código | - |
| `refactor` | Refactoring código | - |
| `perf` | Mejoras performance | - |
| `test` | Tests | - |
| `build` | Sistema build | - |
| `ci` | CI/CD changes | - |
| `chore` | Tareas mantenimiento | - |
| `revert` | Revertir commit | - |

**Breaking changes:**

```text
feat!: change API format
BREAKING CHANGE: API structure changed
```

**Con scope (opcional):**

```text
feat(media): add Plex support
fix(cleanup): resolve symlink deletion
```

### 🔄 Estrategia de Release

| Branch | Propósito |
|--------|-----------|
| `main` | Releases producción (v1.0.0, v1.1.0) |
| `develop` | Pre-releases (v1.1.0-develop.1) |
| `feature/*` | No releases, solo validación PR |

### ⚡ Workflows Automatizados

1. **Commit Validation** (`commit-lint.yml`)
   - Valida commits vs conventional format
   - Ejecuta en cada PR
   - Debe pasar antes de merge

2. **Build and Test** (`gradle.yml`)
   - `./gradlew build && ./gradlew test`
   - JDK 25

3. **Semantic Release** (`.releaserc.json`)
   - Releases automáticos en main/develop
   - Changelog desde commit messages
   - Publica Docker images a GHCR

---

## ✅ CHECKLISTS DE DESARROLLO

### 🚀 Nueva Característica

```bash
# 1. Crear branch feature
git checkout -b feat/my-feature

# 2. Estructura seguir:
src/main/kotlin/com/github/schaka/janitorr/<feature>/
src/test/kotlin/com/github/schaka/janitorr/<feature>/
docs/wiki/en/ Y docs/wiki/es/

# 3. Build y test
./gradlew build && ./gradlew test

# 4. Commit conventional
git commit -m "feat(feature): add description"

# 5. Push y PR
git push origin feat/my-feature
```

### 🔧 Bug Fix

```bash
# 1. Branch fix
git checkout -b fix/bug-description

# 2. Fix + regression test
# 3. Verify no rompe tests existentes
./gradlew test

# 4. Commit conventional
git commit -m "fix(component): resolve specific issue

Fixes #issue-number"
```

### 📖 Actualizar Documentación

```bash
# 1. Actualizar AMBAS versiones:
docs/wiki/en/File-Name.md
docs/wiki/es/Archivo-Nombre.md

# 2. Verificar enlaces funcionan
# 3. Commit
git commit -m "docs: update documentation topic"
```

---

## 🎯 MEJORES PRÁCTICAS

### ✅ HACER

- ✅ Usar conventional commits SIEMPRE
- ✅ Probar en dry-run primero
- ✅ Actualizar documentación bilingüe
- ✅ Constructor injection en Spring
- ✅ Usar MockK para tests Kotlin
- ✅ Paths absolutos consistentes
- ✅ Docker para missing local tools

### ❌ NO HACER

- ❌ Romper modo dry-run
- ❌ Solo actualizar un idioma en docs
- ❌ Usar Mockito con Kotlin
- ❌ Commits genéricos ("Update", "WIP")
- ❌ Asumir paths inconsistentes
- ❌ Force push sin permisos

---

## 📞 RECURSOS Y AYUDA

### 🔗 Enlaces Importantes

| Recurso | Ubicación |
|---------|-----------|
| Main README | `/README.md` |
| Wiki Documentation | `/WIKI_DOCUMENTATION.md` |
| Management UI Guide | `/MANAGEMENT_UI.md` |
| Docker Compose | `/examples/example-compose.yml` |
| GitHub Discussions | Para soporte comunidad |
| Docker Images | `ghcr.io/carcheky/janitorr` |

### 🆘 Cuando No Estés Seguro

1. Revisar patterns existentes en misma área
2. Consultar documentación Spring Boot
3. Considerar impacto en deployment Docker
4. Testear con dry-run habilitado Y deshabilitado
5. Verificar docs actualizadas en ambos idiomas
6. ASEGURAR commits siguen formato conventional
7. Verificar impacto en builds JVM y native

---

**📅 Última actualización:** October 12, 2025  
**🤖 Para agentes Copilot:** LEER Y SEGUIR estas instrucciones. Sin excepciones.
