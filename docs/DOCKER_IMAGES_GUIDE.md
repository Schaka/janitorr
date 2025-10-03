# Guía de Imágenes Docker / Docker Images Guide

## 🐳 Imágenes Disponibles / Available Images

Este fork (`carcheky/janitorr`) publica sus propias imágenes Docker en GitHub Container Registry (GHCR).

This fork (`carcheky/janitorr`) publishes its own Docker images to GitHub Container Registry (GHCR).

---

## 📦 Imágenes Recomendadas / Recommended Images

### Imagen JVM (Recomendada) / JVM Image (Recommended)

```yaml
image: ghcr.io/carcheky/janitorr:jvm-stable
```

**Características / Features:**
- ✅ Incluye Management UI completa / Includes complete Management UI
- ✅ Soporte multi-plataforma (amd64 + arm64) / Multi-platform support
- ✅ Optimizada con Spring AOT / Optimized with Spring AOT
- ✅ Memoria mínima: 200MB (recomendado 256MB) / Minimum memory: 200MB (256MB recommended)
- ✅ Java 25 con CDS y AOT cache / Java 25 with CDS and AOT cache

### Imagen Nativa (Obsoleta desde v1.9.0) / Native Image (Deprecated since v1.9.0)

```yaml
image: ghcr.io/carcheky/janitorr:native-stable
```

**Nota / Note:** La imagen nativa está obsoleta. Se recomienda usar la imagen JVM. / The native image is deprecated. JVM image is recommended.

---

## 🏷️ Etiquetas de Imagen / Image Tags

### Estables / Stable

| Etiqueta / Tag | Descripción / Description | Cuándo usar / When to use |
|----------------|---------------------------|---------------------------|
| `jvm-stable` | Última versión estable JVM / Latest stable JVM release | ✅ Producción / Production |
| `jvm-v1.x.x` | Versión específica JVM / Specific JVM version | Reproducibilidad / Reproducibility |
| `native-stable` | Última versión nativa (obsoleta) / Latest native (deprecated) | ⚠️ No recomendada / Not recommended |
| `native-v1.x.x` | Versión nativa específica / Specific native version | ⚠️ No recomendada / Not recommended |

### Desarrollo / Development

| Etiqueta / Tag | Descripción / Description | Cuándo usar / When to use |
|----------------|---------------------------|---------------------------|
| `jvm-main` | Última construcción desde main / Latest build from main | Características recientes / Recent features |
| `jvm-develop` | Última construcción de desarrollo / Latest development build | ⚠️ Pruebas únicamente / Testing only |
| `native-main` | Imagen nativa desde main / Native image from main | ⚠️ No recomendada / Not recommended |
| `native-develop` | Imagen nativa de desarrollo / Development native image | ⚠️ No recomendada / Not recommended |

---

## 🆚 Diferencias con Upstream / Differences from Upstream

Este fork añade las siguientes características sobre el upstream original (`schaka/janitorr`):

This fork adds the following features over the original upstream (`schaka/janitorr`):

### 🎨 Management UI

- **Interfaz web completa** para gestión y monitoreo / **Complete web interface** for management and monitoring
- Accesible en `http://<host>:<port>/`
- Permite ejecución manual de limpiezas / Allows manual cleanup execution
- Visualización del estado del sistema / System status visualization

### 📚 Documentación Completa / Complete Documentation

- Documentación en inglés y español / English and Spanish documentation
- Guías de configuración detalladas / Detailed configuration guides
- FAQ y solución de problemas / FAQ and troubleshooting

### 🔒 Configuración Más Segura / Safer Configuration

- Valores predeterminados: `enabled: false` / Default values: `enabled: false`
- Previene eliminaciones accidentales / Prevents accidental deletions
- Modo dry-run activado por defecto / Dry-run mode enabled by default

---

## 🚀 Inicio Rápido / Quick Start

### Ejemplo Docker Compose Básico / Basic Docker Compose Example

```yaml
version: "3"

services:
  janitorr:
    container_name: janitorr
    image: ghcr.io/carcheky/janitorr:jvm-stable
    user: 1000:1000  # Tu ID de usuario / Your user ID
    mem_limit: 256M
    mem_swappiness: 0
    volumes:
      - /appdata/janitorr/config/application.yml:/config/application.yml
      - /appdata/janitorr/logs:/logs
      - /share_media:/data
    environment:
      - THC_PATH=/health
      - THC_PORT=8081
    ports:
      - "8978:8978"  # Puerto para Management UI / Port for Management UI
    healthcheck:
      test: ["CMD", "/workspace/health-check"]
      start_period: 30s
      interval: 5s
      retries: 3
    restart: unless-stopped
```

### Acceder a Management UI / Access Management UI

Después de iniciar el contenedor / After starting the container:

```
http://localhost:8978/
```

---

## 📖 Más Información / More Information

### Documentación Completa / Complete Documentation

- 🇬🇧 **English**: [docs/wiki/en/Docker-Compose-Setup.md](../wiki/en/Docker-Compose-Setup.md)
- 🇪🇸 **Español**: [docs/wiki/es/Configuracion-Docker-Compose.md](../wiki/es/Configuracion-Docker-Compose.md)

### Management UI

- [MANAGEMENT_UI.md](../../MANAGEMENT_UI.md)

### Guías de Configuración / Configuration Guides

- 🇬🇧 [Configuration Guide](../wiki/en/Configuration-Guide.md)
- 🇪🇸 [Guía de Configuración](../wiki/es/Guia-Configuracion.md)

### Solución de Problemas / Troubleshooting

- 🇬🇧 [Troubleshooting](../wiki/en/Troubleshooting.md)
- 🇪🇸 [Solución de Problemas](../wiki/es/Solucion-Problemas.md)

---

## 🔗 Enlaces / Links

- **Repositorio / Repository**: https://github.com/carcheky/janitorr
- **Container Registry**: https://github.com/carcheky/janitorr/pkgs/container/janitorr
- **Upstream Original**: https://github.com/schaka/janitorr
- **Issues**: https://github.com/carcheky/janitorr/issues
- **Discussions**: https://github.com/carcheky/janitorr/discussions

---

## ⚙️ Construcción de Imágenes / Image Building

Las imágenes se construyen automáticamente mediante GitHub Actions cuando:

Images are automatically built via GitHub Actions when:

- ✅ Se hace push a `main` o `develop` / Code is pushed to `main` or `develop`
- ✅ Se crea una etiqueta de versión (v*) / A version tag (v*) is created
- ✅ Se abre un pull request / A pull request is opened
- ✅ Se ejecuta manualmente el workflow / The workflow is manually triggered

### Proceso de Construcción / Build Process

1. **Compilación multi-plataforma** / **Multi-platform compilation**
   - Linux AMD64 (x86_64)
   - Linux ARM64 (aarch64)

2. **Empaquetado** / **Packaging**
   - Spring Boot con Paketo Buildpacks
   - Incluye Management UI / Includes Management UI
   - Optimización AOT habilitada / AOT optimization enabled

3. **Publicación** / **Publishing**
   - GitHub Container Registry (GHCR)
   - Manifiestos multi-plataforma / Multi-platform manifests
   - Etiquetado automático / Automatic tagging

---

## ❓ FAQ

### ¿Por qué usar este fork en lugar del upstream?

**Why use this fork instead of upstream?**

- ✅ Management UI web incluida / Web Management UI included
- ✅ Documentación bilingüe completa / Complete bilingual documentation
- ✅ Configuración más segura por defecto / Safer default configuration
- ✅ Activamente mantenido / Actively maintained

### ¿Son compatibles las configuraciones?

**Are configurations compatible?**

Sí, este fork es totalmente compatible con las configuraciones del upstream. Puedes migrar fácilmente.

Yes, this fork is fully compatible with upstream configurations. You can migrate easily.

### ¿Cuál es la diferencia entre JVM y Native?

**What's the difference between JVM and Native?**

| Característica / Feature | JVM | Native |
|--------------------------|-----|--------|
| Memoria / Memory | 256MB recomendado / recommended | ~100MB |
| Tiempo de inicio / Startup time | ~15-30 segundos / seconds | ~2-5 segundos / seconds |
| Management UI | ✅ Incluida / Included | ❌ Excluida / Excluded |
| Estado / Status | ✅ Recomendada / Recommended | ⚠️ Obsoleta / Deprecated |
| Soporte / Support | ✅ Completo / Full | ⚠️ Limitado / Limited |

**Recomendación / Recommendation**: Usa siempre la imagen JVM / Always use the JVM image

---

**Última actualización / Last updated**: Octubre 3, 2025  
**Versión del documento / Document version**: 1.0
