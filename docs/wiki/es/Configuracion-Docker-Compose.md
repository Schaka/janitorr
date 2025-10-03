# Configuración con Docker Compose

Esta guía te ayudará a implementar Janitorr usando Docker Compose.

## Tabla de Contenidos

- [Requisitos Previos](#requisitos-previos)
- [Inicio Rápido](#inicio-rápido)
- [Pasos de Configuración](#pasos-de-configuración)
- [Ejemplos de Docker Compose](#ejemplos-de-docker-compose)
- [Mapeo de Volúmenes](#mapeo-de-volúmenes)
- [Variables de Entorno](#variables-de-entorno)
- [Comprobaciones de Salud](#comprobaciones-de-salud)
- [Etiquetas de Imagen Disponibles](#etiquetas-de-imagen-disponibles)
- [Ejemplo de Stack Completo](#ejemplo-de-stack-completo)
- [Solución de Problemas](#solución-de-problemas)

## Requisitos Previos

Antes de configurar Janitorr, asegúrate de tener:

- Docker y Docker Compose instalados
- Un servidor multimedia (Jellyfin o Emby)
- Herramientas de gestión de medios (Sonarr y/o Radarr)
- Conocimientos básicos de volúmenes y redes de Docker

## Inicio Rápido

1. **Crear el directorio de configuración:**
   ```bash
   mkdir -p /appdata/janitorr/config
   mkdir -p /appdata/janitorr/logs
   ```

2. **Descargar la plantilla de configuración:**
   ```bash
   wget -O /appdata/janitorr/config/application.yml \
     https://raw.githubusercontent.com/carcheky/janitorr/main/src/main/resources/application-template.yml
   ```

3. **Editar el archivo de configuración:**
   ```bash
   nano /appdata/janitorr/config/application.yml
   ```
   Actualiza con tus claves API de *arr, Jellyfin/Emby y Jellyseerr.

4. **Crear un archivo `docker-compose.yml`** (ver ejemplos más abajo)

5. **Iniciar Janitorr:**
   ```bash
   docker-compose up -d
   ```

6. **Acceder a la Interfaz de Gestión:**
   Abre `http://<ip-de-tu-servidor>:8978/` en tu navegador

   **Nota:** La Interfaz de Gestión solo está disponible en la imagen JVM (`jvm-stable`), no en la imagen nativa.

## Pasos de Configuración

### 1. Preparar el Archivo application.yml

El archivo `application.yml` es **obligatorio** para que Janitorr inicie. Sin él, el contenedor fallará.

Descarga la plantilla:
```bash
wget -O /appdata/janitorr/config/application.yml \
  https://raw.githubusercontent.com/carcheky/janitorr/main/src/main/resources/application-template.yml
```

Configuraciones clave:
- **Claves API**: Sonarr, Radarr, Jellyfin/Emby, Jellyseerr
- **URLs de Servidores**: Apunta a tus servicios *arr y servidor multimedia
- **Puerto**: El puerto en el que Janitorr escuchará (predeterminado: 8978)
- **Modo de Prueba**: Comienza con `dry-run: true` para probar sin eliminar nada
- **Directorio "Leaving Soon"**: Donde se crearán los enlaces simbólicos para medios próximos a eliminar

### 2. Comprender el Mapeo de Volúmenes

**Crítico:** ¡Las rutas de volumen deben ser consistentes en todos los contenedores!

Si Radarr almacena películas en `/data/media/movies`, entonces:
- Janitorr también debe verlas en `/data/media/movies`
- Jellyfin también debe acceder a ellas en la misma ruta (o puedes usar `media-server-leaving-soon-dir`)

#### Ejemplo de Escenario

**Rutas del host:**
- Películas: `/share_media/media/movies`
- Series: `/share_media/media/tv`
- Próximas a Eliminar: `/share_media/media/leaving-soon`

**Mapeos Docker para todos los contenedores:**
```yaml
volumes:
  - /share_media:/data
```

**En application.yml:**
```yaml
leaving-soon-dir: "/data/media/leaving-soon"
```

#### Caso Especial: Rutas Diferentes para Jellyfin

Si Jellyfin ve el directorio leaving-soon en una ruta diferente:

**Mapeo de Janitorr:**
```yaml
volumes:
  - /share_media/media/leaving-soon:/data/media/leaving-soon
```

**Mapeo de Jellyfin:**
```yaml
volumes:
  - /share_media/media/leaving-soon:/library/leaving-soon
```

**En application.yml:**
```yaml
leaving-soon-dir: "/data/media/leaving-soon"           # Ruta como la ve Janitorr
media-server-leaving-soon-dir: "/library/leaving-soon" # Ruta como la ve Jellyfin
```

### 3. Configuración de Perfiles de Spring Boot

**Importante:** Janitorr usa perfiles de Spring Boot para propósitos específicos. Comprender esto es crucial para el funcionamiento correcto.

#### El Perfil `leyden`

El perfil `leyden` es **SOLO para generación de caché AOT en tiempo de compilación** y **NUNCA debe activarse en tiempo de ejecución**. Este perfil:
- Deshabilita la Interfaz de Gestión y los endpoints de API (`/api/management/*`)
- Se usa automáticamente durante la construcción de imágenes Docker
- **No debe configurarse** en tu variable de entorno `SPRING_PROFILES_ACTIVE`

#### Configurar Perfiles Personalizados (Opcional)

Si necesitas usar perfiles personalizados de Spring para tu propia configuración, puedes configurarlos vía variable de entorno:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod,custom  # Tus perfiles personalizados
```

**Advertencia:** Nunca incluyas `leyden` en `SPRING_PROFILES_ACTIVE`. Hacerlo deshabilitará la Interfaz de Gestión y causará errores 404 en los endpoints `/api/management/*`.

#### Comportamiento Predeterminado

Por defecto (cuando `SPRING_PROFILES_ACTIVE` no está configurado):
- ✅ La Interfaz de Gestión es accesible en `http://<host>:<puerto>/`
- ✅ Todos los endpoints de API funcionan correctamente
- ✅ Las limpiezas programadas se ejecutan según configuración

## Ejemplos de Docker Compose

### Configuración Básica (JVM - Recomendado)

```yaml
version: "3"

services:
  janitorr:
    container_name: janitorr
    image: ghcr.io/carcheky/janitorr:jvm-stable
    user: 1000:1000 # Reemplaza con tu ID de usuario
    mem_limit: 256M # Mínimo 200M, recomendado 256M
    mem_swappiness: 0
    volumes:
      - /appdata/janitorr/config/application.yml:/config/application.yml
      - /appdata/janitorr/logs:/logs
      - /share_media:/data
    environment:
      - THC_PATH=/health
      - THC_PORT=8081
      # IMPORTANTE: NO configurar SPRING_PROFILES_ACTIVE=leyden
      # La Interfaz de Gestión requiere que el perfil leyden esté inactivo
      # - SPRING_PROFILES_ACTIVE=prod  # Opcional: solo tus perfiles personalizados
    ports:
      - "8978:8978" # Opcional: Solo si necesitas acceso externo
    healthcheck:
      test: ["CMD", "/workspace/health-check"]
      start_period: 30s
      interval: 5s
      retries: 3
    restart: unless-stopped
```

### Configuración con Imagen Nativa (Menor Uso de Memoria)

> **Nota:** La imagen nativa está obsoleta desde v1.9.0. Usa la imagen JVM para mejor soporte.

```yaml
version: "3"

services:
  janitorr:
    container_name: janitorr
    image: ghcr.io/carcheky/janitorr:native-stable
    user: 1000:1000
    volumes:
      - /appdata/janitorr/config/application.yml:/config/application.yml
      - /appdata/janitorr/logs:/logs
      - /share_media:/data
    environment:
      - THC_PATH=/health
      - THC_PORT=8081
      - SPRING_CONFIG_ADDITIONAL_LOCATION=/config/application.yml
    ports:
      - "8978:8978"
    healthcheck:
      test: ["CMD", "/workspace/health-check"]
      start_period: 30s
      interval: 5s
      retries: 3
    restart: unless-stopped
```

## Mapeo de Volúmenes

### Volúmenes Requeridos

1. **Archivo de Configuración:**
   ```yaml
   - /appdata/janitorr/config/application.yml:/config/application.yml
   ```
   Mapea tu archivo de configuración en el contenedor.

2. **Directorio de Registros:**
   ```yaml
   - /appdata/janitorr/logs:/logs
   ```
   Almacena registros en el host (habilita el registro en archivo en application.yml).

3. **Directorio de Medios:**
   ```yaml
   - /share_media:/data
   ```
   Debe incluir todos los directorios de medios que Sonarr/Radarr gestionan.

### Mejores Prácticas

- Usa el **mismo mapeo de volumen** para Janitorr, Sonarr, Radarr y Jellyfin
- Asegúrate de que el usuario (`1000:1000`) tenga permisos de lectura/escritura
- El directorio leaving-soon debe ser escribible (Janitorr crea enlaces simbólicos aquí)

## Variables de Entorno

### Variables Requeridas

- `THC_PATH=/health` - Ruta del endpoint de comprobación de salud
- `THC_PORT=8081` - Puerto de comprobación de salud

### Variables Opcionales (Solo Imagen Nativa)

- `SPRING_CONFIG_ADDITIONAL_LOCATION=/config/application.yml` - Ubicación del archivo de configuración
  - Solo necesario para imagen nativa
  - No requerido para imagen JVM

### Configuración de Memoria JVM

El `mem_limit` se usa para calcular dinámicamente el tamaño del heap:
- **Mínimo:** 200M (puede causar problemas)
- **Recomendado:** 256M
- **Bibliotecas grandes:** 512M o superior

### Puerto de la Interfaz de Gestión

La Interfaz de Gestión usa el puerto configurado en `application.yml`:

```yaml
# En application.yml
server:
  port: 8978
```

```yaml
# En docker-compose.yml
ports:
  - "8978:8978"  # Mapea puerto de host 8978 a puerto de contenedor 8978
```

Para usar un puerto de host diferente (ej., 9000):
```yaml
ports:
  - "9000:8978"  # Acceder a la UI en http://localhost:9000/
```

**Nota:** Si no necesitas acceso externo a la UI, puedes omitir la sección `ports:` completamente y acceder a la UI solo desde otros contenedores en la misma red Docker.

## Comprobaciones de Salud

Janitorr incluye un verificador de salud integrado:

```yaml
healthcheck:
  test: ["CMD", "/workspace/health-check"]
  start_period: 30s
  interval: 5s
  retries: 3
```

Esto asegura que el contenedor esté saludable antes de enrutar tráfico hacia él.

## Etiquetas de Imagen Disponibles

### Versiones Estables

- `ghcr.io/carcheky/janitorr:jvm-stable` - Última imagen JVM estable (recomendada)
  - ✅ **Incluye Interfaz de Gestión**
  - ✅ Soporte completo de características
  - Memoria: 256MB recomendado
  
- `ghcr.io/carcheky/janitorr:jvm-v1.x.x` - Versión JVM específica
  - ✅ **Incluye Interfaz de Gestión**
  - Usa para fijar versión
  
- `ghcr.io/carcheky/janitorr:native-stable` - Última imagen nativa estable (obsoleta)
  - ❌ **NO incluye Interfaz de Gestión**
  - ⚠️ Obsoleta desde v1.9.0
  - Menor uso de memoria (~150MB)
  
- `ghcr.io/carcheky/janitorr:native-v1.x.x` - Versión nativa específica
  - ❌ **NO incluye Interfaz de Gestión**
  - ⚠️ Obsoleta

### Compilaciones de Desarrollo

- `ghcr.io/carcheky/janitorr:jvm-develop` - Última compilación de desarrollo (JVM)
  - ✅ **Incluye Interfaz de Gestión**
  - ⚠️ Puede ser inestable
  
- `ghcr.io/carcheky/janitorr:native-develop` - Última compilación de desarrollo (nativa)
  - ❌ **NO incluye Interfaz de Gestión**
  - ⚠️ Puede ser inestable

> **Advertencia:** Las compilaciones de desarrollo pueden ser inestables. Usa solo para pruebas.

### ¿Qué Imagen Debo Usar?

**Para la mayoría de usuarios:** Usa `jvm-stable`
- ✅ Incluye Interfaz de Gestión
- ✅ Completamente soportada
- ✅ Mejor compatibilidad

**Actualización de nativa a JVM:**

Si estás usando la imagen nativa y quieres la Interfaz de Gestión:

1. Cambia la etiqueta de imagen en docker-compose.yml
2. Aumenta `mem_limit` a 256M
3. Elimina `SPRING_CONFIG_ADDITIONAL_LOCATION` si está presente
4. Ejecuta `docker-compose pull && docker-compose up -d`
5. Accede a la UI en `http://<host>:8978/`

## Ejemplo de Stack Completo

Aquí hay una configuración completa de Docker Compose con Jellyfin, Sonarr, Radarr, Jellyseerr, Jellystat y Janitorr:

```yaml
version: "3"

services:
  jellyfin:
    image: jellyfin/jellyfin:latest
    container_name: jellyfin
    environment:
      - PUID=1000
      - PGID=1000
      - TZ=Europe/Madrid
    volumes:
      - /appdata/jellyfin:/config
      - /share_media:/data
    ports:
      - 8096:8096
    restart: unless-stopped

  radarr:
    image: lscr.io/linuxserver/radarr:latest
    container_name: radarr
    environment:
      - PUID=1000
      - PGID=1000
      - TZ=Europe/Madrid
    volumes:
      - /appdata/radarr:/config
      - /share_media:/data
    ports:
      - 7878:7878
    restart: unless-stopped

  sonarr:
    image: lscr.io/linuxserver/sonarr:latest
    container_name: sonarr
    environment:
      - PUID=1000
      - PGID=1000
      - TZ=Europe/Madrid
    volumes:
      - /appdata/sonarr:/config
      - /share_media:/data
    ports:
      - 8989:8989
    restart: unless-stopped

  jellyseerr:
    image: fallenbagel/jellyseerr:latest
    container_name: jellyseerr
    environment:
      - PUID=1000
      - PGID=1000
      - TZ=Europe/Madrid
    volumes:
      - /appdata/jellyseerr:/app/config
    ports:
      - 5050:5050
    restart: unless-stopped

  jellystat-db:
    container_name: jellystat-db
    image: postgres:15.2
    environment:
      POSTGRES_DB: 'jfstat'
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: micontraseña
    volumes:
      - /appdata/jellystat/postgres-data:/var/lib/postgresql/data
    restart: unless-stopped

  jellystat:
    container_name: jellystat
    image: cyfershepard/jellystat:latest
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: micontraseña
      POSTGRES_IP: jellystat-db
      POSTGRES_PORT: 5432
      JWT_SECRET: 'tu-secreto-aqui'
    volumes:
      - /appdata/jellystat/config:/app/backend/backup-data
    ports:
      - "3000:3000"
    depends_on:
      - jellystat-db
    restart: unless-stopped

  janitorr:
    container_name: janitorr
    image: ghcr.io/carcheky/janitorr:jvm-stable
    user: 1000:1000
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
      - "8978:8978"
    healthcheck:
      test: ["CMD", "/workspace/health-check"]
      start_period: 30s
      interval: 5s
      retries: 3
    restart: unless-stopped
```

**Notas Importantes:**
- Todos los contenedores usan el mismo mapeo `/share_media:/data`
- Los IDs de usuario (1000:1000) deben coincidir con tu usuario del host
- Actualiza la zona horaria (`TZ`) según tu ubicación
- Cambia todas las contraseñas y secretos antes de implementar

## Solución de Problemas

### El Contenedor No Inicia

**Problema:** Janitorr se cierra inmediatamente después de iniciarse.

**Solución:**
1. Verifica que `application.yml` existe en la ubicación mapeada
2. Verifica que el archivo tenga los permisos correctos (legible por el usuario 1000)
3. Revisa los logs de Docker: `docker logs janitorr`

### No se Pueden Crear Enlaces Simbólicos

**Problema:** Los logs de Janitorr muestran errores "Failed to create symlink".

**Solución:**
1. Verifica que el directorio leaving-soon sea escribible
2. Verifica que los mapeos de volumen coincidan entre Janitorr y los *arrs
3. Asegúrate de que el usuario tenga permisos de escritura en el directorio leaving-soon

### Los Archivos No se Eliminan

**Problema:** Los medios no se eliminan aunque deberían.

**Solución:**
1. Verifica si el modo dry-run está habilitado en `application.yml`
2. Verifica que las claves API sean correctas
3. Verifica que los medios tengan la edad/requisitos adecuados para eliminación
4. Revisa los logs de Janitorr para cualquier error

### Puerto Ya en Uso

**Problema:** Docker dice que el puerto 8978 ya está asignado.

**Solución:**
1. Cambia el mapeo de puerto: `"8979:8978"` (host:contenedor)
2. O elimina la sección de puertos completamente si no necesitas acceso externo

### Errores de Permisos Denegados

**Problema:** Janitorr no puede leer/escribir archivos.

**Solución:**
1. Verifica que el ajuste `user:` coincida con el ID de usuario de tu host
2. Ejecuta `id` en tu host para encontrar tu UID:GID
3. Actualiza el campo `user:` en docker-compose.yml
4. Establece la propiedad adecuada: `chown -R 1000:1000 /appdata/janitorr /share_media`

### La Interfaz de Gestión Devuelve Errores 404

**Problema:** Al acceder a `/api/management/status` u otros endpoints de gestión se devuelven errores 404.

**Solución:**
1. Verifica si la variable de entorno `SPRING_PROFILES_ACTIVE` incluye `leyden`
2. Elimina `leyden` de los perfiles activos - es solo para uso en tiempo de compilación
3. Si necesitas perfiles personalizados, configúralos sin `leyden`:
   ```yaml
   environment:
     - SPRING_PROFILES_ACTIVE=prod,custom  # NO incluyas leyden
   ```
4. Reinicia el contenedor después de eliminar el perfil leyden
5. Verifica que los endpoints sean accesibles: `curl http://localhost:8978/api/management/status`
6. Revisa los logs del contenedor para confirmar que ManagementController se cargó: `docker logs janitorr`

## Próximos Pasos

Después de una implementación exitosa:

1. **Accede a la Interfaz de Gestión** en `http://<ip-de-tu-servidor>:8978/`
2. **Revisa la configuración** y verifica que todos los servicios estén conectados
3. **Prueba en modo dry-run** antes de habilitar eliminaciones reales
4. **Monitorea los logs** para entender qué hará Janitorr
5. **Configura la colección "Leaving Soon"** en Jellyfin

## Recursos Adicionales

- [Guía de Configuración](Guia-Configuracion.md) - Configuración detallada de application.yml
- [Preguntas Frecuentes](Preguntas-Frecuentes.md) - Preguntas y respuestas comunes
- [Solución de Problemas](Solucion-Problemas.md) - Guía detallada de solución de problemas
- [GitHub Discussions](https://github.com/carcheky/janitorr/discussions) - Soporte de la comunidad

---

**¿Necesitas ayuda?** ¡Consulta las [Preguntas Frecuentes](Preguntas-Frecuentes.md) o inicia una [discusión](https://github.com/carcheky/janitorr/discussions)!
