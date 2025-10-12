# 📋 Instrucciones para GitHub Copilot - Proyecto Janitorr

## 🚨 REGLAS CRÍTICAS - LECTURA OBLIGATORIA

### ✅ Conventional Commits - SIN EXCEPCIONES

**TODOS los commits DEBEN seguir este formato:**
```
<type>[(<scope>)]: <subject>
```

**Tipos válidos:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`

**Ejemplos correctos:**
- `feat: agregar nueva funcionalidad`
- `fix(cleanup): resolver error de sintaxis`
- `docs: actualizar documentación`

**JAMÁS uses:** "Update", "WIP", "Initial plan", "Merge" - Estos commits serán rechazados automáticamente.

### 🔧 Herramientas MCP Disponibles

Cuando necesites estas capacidades, úsalas:
- `@activate_filesystem_management_tools` - Para operaciones con archivos
- `@activate_mcp_shell_tools` - Para comandos de terminal
- `@mcp_upstash_conte_get-library-docs` - Para documentación de librerías
- `@activate_knowledge_graph_tools` - Para gestión de memoria y contexto

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
