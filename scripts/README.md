# Scripts Directory

This directory contains utility scripts for the Janitorr fork.

## Available Scripts

### `verify-images.sh`

Verifies that Docker images are available in GitHub Container Registry (GHCR).

**Usage:**
```bash
./scripts/verify-images.sh
```

**What it checks:**
- ✅ Stable images (`jvm-stable`, `native-stable`)
- ✅ Version-tagged images (e.g., `jvm-v1.0.0`)
- ✅ Branch images (`jvm-main`, `jvm-develop`)
- ✅ Platform-specific images (`jvm-amd64-main`, `jvm-arm64-main`)

**Requirements:**
- `curl` (usually pre-installed)
- Internet connection to GHCR

**Example output:**
```
🔍 Verificando imágenes Docker del fork carcheky/janitorr
============================================================

🔍 Verificando imágenes estables (recomendadas para producción):
================================================================
Verificando ghcr.io/carcheky/janitorr:jvm-stable... ✅ Disponible
Verificando ghcr.io/carcheky/janitorr:jvm-v1.0.0... ⚠️  No disponible o requiere autenticación (código: 404)
Verificando ghcr.io/carcheky/janitorr:native-stable... ✅ Disponible
```

**Note:** Some images may show as "not available" if they haven't been published yet (e.g., no version tags exist yet, or the workflow hasn't run).

## Adding New Scripts

When adding new scripts to this directory:

1. Make them executable: `chmod +x scripts/your-script.sh`
2. Add documentation to this README
3. Use clear, descriptive names
4. Include usage examples and requirements
5. Add comments in the script itself

## Related Documentation

- [Docker Images Guide](../docs/DOCKER_IMAGES_GUIDE.md) - User guide for Docker images
- [Docker Image Verification](../docs/DOCKER_IMAGE_VERIFICATION.md) - Detailed verification report
- [CI/CD Documentation](../docs/CI-CD.md) - Workflow information
