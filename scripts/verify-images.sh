#!/bin/bash
# Script to verify Docker images are available in GitHub Container Registry
# Usage: ./verify-images.sh

set -e

OWNER="carcheky"
PACKAGE="janitorr"
PACKAGE_NATIVE="janitorr-native"
REGISTRY="ghcr.io"

echo "🔍 Verificando imágenes Docker del fork carcheky/janitorr"
echo "============================================================"
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check image without docker (using curl)
check_image_curl() {
    local package=$1
    local tag=$2
    local image="${OWNER}/${package}:${tag}"
    
    echo -n "Verificando ${REGISTRY}/${OWNER}/${package}:${tag}... "
    
    # Try to get manifest using curl
    local response=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Accept: application/vnd.oci.image.manifest.v1+json" \
        "https://${REGISTRY}/v2/${OWNER}/${package}/manifests/${tag}")
    
    if [ "$response" = "200" ] || [ "$response" = "307" ]; then
        echo -e "${GREEN}✅ Disponible${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  No disponible o requiere autenticación (código: ${response})${NC}"
        return 1
    fi
}

echo "🔍 Verificando imágenes estables (recomendadas para producción):"
echo "================================================================"
check_image_curl "${PACKAGE}" "latest"
check_image_curl "${PACKAGE}" "1.0.0"
check_image_curl "${PACKAGE_NATIVE}" "latest"
echo ""

echo "🔍 Verificando imágenes de desarrollo:"
echo "======================================"
check_image_curl "${PACKAGE}" "main"
check_image_curl "${PACKAGE}" "develop"
check_image_curl "${PACKAGE_NATIVE}" "main"
check_image_curl "${PACKAGE_NATIVE}" "develop"
echo ""

echo "🔍 Verificando imágenes específicas de plataforma:"
echo "=================================================="
check_image_curl "${PACKAGE}" "amd64-main"
check_image_curl "${PACKAGE}" "arm64-main"
echo ""

echo ""
echo "📝 Notas:"
echo "========="
echo "- ✅ = Imagen disponible públicamente"
echo "- ⚠️  = Imagen puede requerir autenticación o no existe aún"
echo "- ❌ = Imagen no disponible"
echo ""
echo "💡 Las imágenes 'latest' se crean cuando se publica una versión (tag v*)"
echo "💡 Las imágenes 'main' y 'develop' se crean en cada push a esas ramas"
echo ""
echo "🔗 Para ver todas las versiones disponibles:"
echo "   https://github.com/${OWNER}/${PACKAGE}/pkgs/container/${PACKAGE}"
echo "   https://github.com/${OWNER}/${PACKAGE_NATIVE}/pkgs/container/${PACKAGE_NATIVE}"
echo ""
echo "📦 Para descargar una imagen:"
echo "   docker pull ${REGISTRY}/${OWNER}/${PACKAGE}:latest"
echo "   docker pull ${REGISTRY}/${OWNER}/${PACKAGE_NATIVE}:latest"
echo ""
