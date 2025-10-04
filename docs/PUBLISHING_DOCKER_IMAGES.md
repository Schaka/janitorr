# Pasos para Publicar Imágenes Docker / Steps to Publish Docker Images

## 🎯 Objetivo / Goal

Este documento explica cómo publicar las primeras imágenes Docker del fork `carcheky/janitorr` en GitHub Container Registry.

This document explains how to publish the first Docker images for the `carcheky/janitorr` fork to GitHub Container Registry.

---

## ✅ Estado Actual / Current Status

### Configuración Completada / Configuration Complete

- ✅ GitHub Actions workflows configurados / GitHub Actions workflows configured
- ✅ Build system listo para fork / Build system ready for fork
- ✅ Management UI incluida en fuentes / Management UI included in sources
- ✅ Multi-plataforma soportada (amd64 + arm64) / Multi-platform supported
- ✅ Documentación actualizada / Documentation updated

### Imágenes Pendientes de Publicación / Images Pending Publication

Las imágenes Docker se crearán automáticamente cuando:

Docker images will be automatically created when:

1. Se haga push a las ramas `main` o `develop` / Code is pushed to `main` or `develop`
2. Se cree una etiqueta de versión (ej: `v1.0.0`) / A version tag is created (e.g., `v1.0.0`)
3. Se ejecute manualmente el workflow / The workflow is manually triggered

---

## 🚀 Opción 1: Trigger Manual del Workflow (Más Rápido)

### Español

**Esta es la forma más rápida de generar las primeras imágenes.**

1. Ve a la página de GitHub Actions del repositorio:
   ```
   https://github.com/carcheky/janitorr/actions
   ```

2. Selecciona el workflow "JVM Image" en la barra lateral izquierda

3. Haz clic en el botón "Run workflow" (arriba a la derecha)

4. Selecciona la rama:
   - `main` para imagen estable de desarrollo
   - `develop` para imagen de desarrollo

5. Haz clic en "Run workflow" verde

6. El workflow:
   - Compilará el código
   - Construirá imágenes para amd64 y arm64
   - Las publicará en `ghcr.io/carcheky/janitorr:jvm-main` (o `jvm-develop`)

### English

**This is the fastest way to generate the first images.**

1. Go to the repository's GitHub Actions page:
   ```
   https://github.com/carcheky/janitorr/actions
   ```

2. Select the "JVM Image" workflow in the left sidebar

3. Click the "Run workflow" button (top right)

4. Select the branch:
   - `main` for stable development image
   - `develop` for development image

5. Click the green "Run workflow" button

6. The workflow will:
   - Compile the code
   - Build images for amd64 and arm64
   - Publish them to `ghcr.io/carcheky/janitorr:jvm-main` (or `jvm-develop`)

**Tiempo estimado / Estimated time:** 15-20 minutos / minutes

---

## 🏷️ Opción 2: Crear una Etiqueta de Versión (Recomendado para Producción)

### Español

**Esta opción crea las imágenes `:latest` (JVM) y `:native-latest` recomendadas para producción.**

### Paso 1: Preparar la rama main

Asegúrate de que la rama `main` tiene todos los cambios que quieres publicar.

### Paso 2: Usar Semantic Release (Recomendado)

El repositorio está configurado con semantic-release. Simplemente haz merge de un PR con commits convencionales a `main`:

```bash
# Los commits deben seguir el formato:
# feat: nueva característica (incrementa versión minor)
# fix: corrección de bug (incrementa versión patch)
# BREAKING CHANGE: cambio incompatible (incrementa versión major)
```

Semantic-release automáticamente:
- Determinará la nueva versión basándose en los commits
- Creará un tag (ej: `v1.0.0`)
- Generará el CHANGELOG
- Creará un GitHub Release
- Disparará los workflows de construcción de imágenes

### Paso 3: O Crear Tag Manualmente

Si prefieres crear el tag manualmente:

```bash
# Asegúrate de estar en main actualizado
git checkout main
git pull origin main

# Crea el tag (usa versionado semántico)
git tag v1.0.0

# Empuja el tag a GitHub
git push origin v1.0.0
```

### Paso 4: Verificar la Construcción

1. Ve a: https://github.com/carcheky/janitorr/actions
2. Verás workflows ejecutándose para "JVM Image" y "Native images"
3. Espera a que completen (~20-30 minutos)

### Paso 5: Verificar las Imágenes Publicadas

Una vez completados los workflows, las imágenes estarán disponibles:

```bash
# Imagen JVM estable (recomendada)
docker pull ghcr.io/carcheky/janitorr:latest

# Imagen JVM con versión específica
docker pull ghcr.io/carcheky/janitorr:1.0.0

# Imagen nativa (obsoleta)
docker pull ghcr.io/carcheky/janitorr:native-latest
docker pull ghcr.io/carcheky/janitorr:native-1.0.0
```

### English

**This option creates the `:latest` (JVM) and `:native-latest` images recommended for production.**

### Step 1: Prepare the main branch

Make sure the `main` branch has all changes you want to publish.

### Step 2: Use Semantic Release (Recommended)

The repository is configured with semantic-release. Simply merge a PR with conventional commits to `main`:

```bash
# Commits must follow the format:
# feat: new feature (bumps minor version)
# fix: bug fix (bumps patch version)
# BREAKING CHANGE: incompatible change (bumps major version)
```

Semantic-release will automatically:
- Determine the new version based on commits
- Create a tag (e.g., `v1.0.0`)
- Generate the CHANGELOG
- Create a GitHub Release
- Trigger image build workflows

### Step 3: Or Create Tag Manually

If you prefer to create the tag manually:

```bash
# Make sure you're on updated main
git checkout main
git pull origin main

# Create the tag (use semantic versioning)
git tag v1.0.0

# Push the tag to GitHub
git push origin v1.0.0
```

### Step 4: Verify the Build

1. Go to: https://github.com/carcheky/janitorr/actions
2. You'll see workflows running for "JVM Image" and "Native images"
3. Wait for them to complete (~20-30 minutes)

### Step 5: Verify Published Images

Once workflows complete, images will be available:

```bash
# Stable JVM image (recommended)
docker pull ghcr.io/carcheky/janitorr:latest

# JVM image with specific version
docker pull ghcr.io/carcheky/janitorr:1.0.0

# Native image (deprecated)
docker pull ghcr.io/carcheky/janitorr:native-latest
docker pull ghcr.io/carcheky/janitorr:native-1.0.0
```

---

## 🔍 Verificar Disponibilidad de Imágenes / Verify Image Availability

### Usando el Script de Verificación / Using the Verification Script

```bash
./scripts/verify-images.sh
```

### Manualmente con curl / Manually with curl

```bash
# Verificar imagen JVM estable
curl -I https://ghcr.io/v2/carcheky/janitorr/manifests/latest

# Código 200 = imagen disponible
# Código 401/404 = imagen no existe o requiere autenticación
```

### Ver Todas las Imágenes en GHCR / View All Images in GHCR

Visita / Visit:
```
https://github.com/carcheky/janitorr/pkgs/container/janitorr
```

---

## 📋 Checklist de Primera Publicación / First Publication Checklist

### Antes de Publicar / Before Publishing

- [ ] Todos los tests pasan / All tests pass
- [ ] Build local exitoso / Local build successful
- [ ] Documentación actualizada / Documentation updated
- [ ] CHANGELOG preparado / CHANGELOG prepared (si es manual)

### Publicar Imagen de Desarrollo / Publish Development Image

- [ ] Ejecutar workflow "JVM Image" manualmente / Run "JVM Image" workflow manually
- [ ] Verificar build exitoso / Verify successful build
- [ ] Probar imagen: `docker pull ghcr.io/carcheky/janitorr:jvm-main`
- [ ] Verificar Management UI funciona / Verify Management UI works

### Publicar Primera Versión Estable / Publish First Stable Release

- [ ] Merge PR a main con commits convencionales / Merge PR to main with conventional commits
- [ ] Esperar semantic-release o crear tag manualmente / Wait for semantic-release or create tag manually
- [ ] Verificar workflows completan / Verify workflows complete
- [ ] Probar imagen: `docker pull ghcr.io/carcheky/janitorr:latest`
- [ ] Verificar todas las características / Verify all features
- [ ] Actualizar documentación con versión / Update documentation with version

---

## 🔧 Troubleshooting

### El workflow falla al construir / Workflow fails to build

1. Verifica los logs en GitHub Actions
2. Asegúrate de que no hay errores de compilación
3. Revisa que los tests pasen
4. Verifica que Java 25 está configurado correctamente

**Si ves errores sobre "runner no encontrado" / "runner not found":**
- El workflow usa QEMU emulation para builds ARM64, no requiere runners ARM64 específicos
- Todos los builds se ejecutan en `ubuntu-latest` (AMD64 runners estándar de GitHub)
- QEMU permite compilar imágenes ARM64 en runners AMD64

### Las imágenes no aparecen en GHCR / Images don't appear in GHCR

1. Verifica que el workflow completó exitosamente
2. Revisa los permisos del paquete en GHCR
3. Asegúrate de que el paquete está configurado como público
4. Espera unos minutos, puede haber retraso

### Error 401 al verificar imágenes / 401 error when verifying images

- Esto es normal si las imágenes aún no se han publicado
- También ocurre si el paquete es privado
- Verifica la configuración de visibilidad en GHCR

### Management UI no aparece en la imagen / Management UI doesn't appear in image

- La UI debería estar incluida automáticamente
- Verifica que los archivos estén en `src/main/resources/static/`
- Revisa los logs de construcción para errores
- La UI nativa está excluida del profile "leyden" (solo para imagen nativa)

---

## 📚 Referencias / References

- [GitHub Actions Workflows](.github/workflows/)
- [Build Configuration](build.gradle.kts)
- [Docker Images Guide](docs/DOCKER_IMAGES_GUIDE.md)
- [Docker Image Verification](docs/DOCKER_IMAGE_VERIFICATION.md)
- [Management UI Documentation](MANAGEMENT_UI.md)

---

## ✅ Próximos Pasos Después de la Primera Publicación

### After First Publication Next Steps

1. **Probar la imagen localmente / Test image locally**
   ```bash
   docker-compose -f examples/docker-compose.example.ui.yml up
   ```

2. **Verificar Management UI / Verify Management UI**
   - Abre http://localhost:8978/
   - Prueba las funciones de cleanup manual
   - Verifica el estado del sistema

3. **Actualizar documentación / Update documentation**
   - Añade notas sobre la primera versión
   - Actualiza ejemplos si es necesario
   - Comparte con la comunidad

4. **Configurar releases automáticos / Configure automatic releases**
   - Los releases futuros serán automáticos con semantic-release
   - Solo necesitas hacer merge de PRs con commits convencionales
   - El sistema manejará versioning, changelog, y publicación de imágenes

---

**Última actualización / Last updated:** Octubre 2025 / October 2025  
**Versión del documento / Document version:** 1.0
