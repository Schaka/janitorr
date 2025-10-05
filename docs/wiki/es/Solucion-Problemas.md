# Solución de Problemas

Problemas comunes y sus soluciones al ejecutar Janitorr.

## Tabla de Contenidos

- [Problemas del Contenedor](#problemas-del-contenedor)
- [Problemas de Configuración](#problemas-de-configuración)
- [Problemas de Conexión](#problemas-de-conexión)
- [Problemas de Operaciones de Archivos](#problemas-de-operaciones-de-archivos)
- [Problemas de Eliminación](#problemas-de-eliminación)
- [Problemas de Rendimiento](#problemas-de-rendimiento)
- [Problemas de la Interfaz de Gestión](#problemas-de-la-interfaz-de-gestión)
- [Registro y Depuración](#registro-y-depuración)

## Problemas del Contenedor

### El Contenedor No Inicia

**Síntomas:** El contenedor se cierra inmediatamente, no se mantiene en ejecución

**Causas Comunes:**
1. Falta `application.yml` o no es accesible
2. Sintaxis YAML inválida
3. Puerto ya en uso
4. Memoria insuficiente

**Soluciones:**

1. **Verifica si existe la configuración:**
   ```bash
   ls -la /appdata/janitorr/config/application.yml
   ```

2. **Verifica permisos del archivo:**
   ```bash
   # Debe ser legible por el usuario 1000
   chmod 644 /appdata/janitorr/config/application.yml
   chown 1000:1000 /appdata/janitorr/config/application.yml
   ```

3. **Revisa los logs:**
   ```bash
   docker logs janitorr
   ```

4. **Valida sintaxis YAML:**
   Usa un validador YAML en línea o:
   ```bash
   docker run --rm -v /appdata/janitorr/config:/config \
     alpine sh -c "apk add --no-cache yq && yq eval /config/application.yml"
   ```

### El Contenedor se Reinicia Repetidamente

**Síntomas:** El contenedor sigue reiniciándose, falla la comprobación de salud

**Soluciones:**

1. **Revisa límites de memoria:**
   ```yaml
   mem_limit: 256M  # Aumenta si es necesario
   ```

2. **Revisa logs de comprobación de salud:**
   ```bash
   docker inspect janitorr | grep -A 10 Health
   ```

3. **Deshabilita temporalmente la comprobación de salud:**
   ```yaml
   # Comenta o elimina la sección healthcheck
   # healthcheck:
   #   test: ["CMD", "/workspace/health-check"]
   ```

### Conflicto de Puerto

**Síntomas:** Error: "port is already allocated"

**Soluciones:**

1. **Encuentra qué está usando el puerto:**
   ```bash
   sudo netstat -tulpn | grep 8978
   # o
   sudo lsof -i :8978
   ```

2. **Cambia el puerto del host:**
   ```yaml
   ports:
     - "8979:8978"  # Usa 8979 en el host
   ```

3. **Elimina el mapeo de puerto** (si no necesitas acceso externo):
   ```yaml
   # Comenta o elimina la sección de puertos
   # ports:
   #   - "8978:8978"
   ```

## Problemas de Configuración

### Sintaxis de Configuración Inválida

**Síntomas:** Los logs del contenedor muestran errores de análisis YAML

**Soluciones:**

1. **Errores YAML comunes:**
   ```yaml
   # ❌ Incorrecto: Faltan comillas para cadenas con caracteres especiales
   leaving-soon-dir: /data/leaving-soon
   
   # ✅ Correcto: Usa comillas
   leaving-soon-dir: "/data/leaving-soon"
   ```

   ```yaml
   # ❌ Incorrecto: Indentación inconsistente
   sonarr:
     - url: "http://sonarr:8989"
       api-key: "clave"
   ```

   ```yaml
   # ✅ Correcto: Indentación consistente (2 espacios)
   sonarr:
     - url: "http://sonarr:8989"
       api-key: "clave"
   ```

2. **Valida el YAML:**
   ```bash
   # En línea: Usa yamllint.com o similar
   # CLI: Instala yamllint
   yamllint /appdata/janitorr/config/application.yml
   ```

### La Configuración No Tiene Efecto

**Síntomas:** Los cambios en application.yml no se aplican

**Soluciones:**

1. **Reinicia el contenedor:**
   ```bash
   docker-compose restart janitorr
   ```

2. **Verifica que el archivo está mapeado correctamente:**
   ```bash
   docker exec janitorr cat /config/application.yml
   ```

3. **Revisa variables de entorno de reemplazo:**
   Algunas configuraciones pueden ser reemplazadas por variables de entorno en docker-compose.yml

## Problemas de Conexión

### No se Puede Conectar a Aplicaciones *arr

**Síntomas:** Los logs muestran errores de conexión rechazada o timeout para Sonarr/Radarr

**Soluciones:**

1. **Verifica las URLs:**
   ```bash
   # Desde dentro del contenedor
   docker exec janitorr wget -O- http://sonarr:8989/api/v3/system/status
   ```

2. **Verifica si los contenedores están en la misma red:**
   ```bash
   docker network inspect bridge
   ```

3. **Usa dirección IP en lugar de nombre de host:**
   ```yaml
   sonarr:
     - url: "http://192.168.1.10:8989"  # Usa IP en lugar de hostname
   ```

4. **Verifica claves API:**
   - Inicia sesión en Sonarr/Radarr
   - Ve a Configuración → General
   - Copia la clave API exactamente

### No se Puede Conectar a Jellyfin/Emby

**Síntomas:** Errores de autenticación o fallos de conexión

**Soluciones:**

1. **Verifica nombre de usuario y contraseña:**
   ```yaml
   jellyfin:
     username: "janitorr"  # Debe ser exacto
     password: "tu-contraseña"
   ```

2. **Verifica que el usuario existe y tiene permisos:**
   - El usuario debe existir en Jellyfin/Emby
   - El usuario necesita permisos de eliminación
   - Prueba el inicio de sesión manualmente en la interfaz web de Jellyfin/Emby

3. **La clave API sola no es suficiente:**
   Janitorr necesita tanto la clave API COMO credenciales de usuario para eliminar archivos.

### No se Puede Conectar a Jellyseerr

**Síntomas:** Falla la limpieza de solicitudes

**Soluciones:**

1. **Verifica la clave API:**
   - Ve a Configuración de Jellyseerr → General
   - Copia la clave API (en la sección "API Key")

2. **Verifica formato de URL:**
   ```yaml
   jellyseerr:
     url: "http://jellyseerr:5050"  # Sin barra al final
   ```

## Problemas de Operaciones de Archivos

### No se Pueden Crear Enlaces Simbólicos

**Síntomas:** Los logs muestran errores "Failed to create symlink"

**Causas:**
- Desajuste en mapeo de volúmenes
- Problemas de permisos
- El sistema de archivos no soporta enlaces simbólicos

**Soluciones:**

1. **Verifica que las rutas son accesibles:**
   ```bash
   docker exec janitorr ls -la /data/media/movies
   docker exec janitorr ls -la /data/media/leaving-soon
   ```

2. **Verifica permisos de escritura:**
   ```bash
   # Desde el host
   ls -la /share_media/media/leaving-soon
   # Debe ser escribible por el usuario 1000
   chown -R 1000:1000 /share_media/media/leaving-soon
   ```

3. **Asegúrate de que los mapeos de volumen coinciden:**
   Todos los contenedores (Janitorr, Sonarr, Radarr, Jellyfin) deben usar el mismo mapeo:
   ```yaml
   volumes:
     - /share_media:/data
   ```

4. **Prueba crear enlaces simbólicos manualmente:**
   ```bash
   docker exec janitorr ln -s /data/media/movies/test.mkv /data/media/leaving-soon/test.mkv
   ```

### Errores de Ruta No Encontrada

**Síntomas:** Los logs muestran archivo o directorio no encontrado

**Causa:** La ruta reportada por *arr no existe en la vista de Janitorr

**Soluciones:**

1. **Verifica mapeos de volumen:**
   ```yaml
   # ❌ Incorrecto: Mapeos diferentes
   # Sonarr:
   volumes:
     - /media:/data
   
   # Janitorr:
   volumes:
     - /media:/movies  # ¡DESAJUSTE!
   ```

   ```yaml
   # ✅ Correcto: Mismos mapeos
   # Ambos contenedores:
   volumes:
     - /media:/data
   ```

2. **Verifica la ruta reportada por *arr:**
   - Revisa Sonarr/Radarr → Series/Película → Archivos
   - Anota la ruta mostrada
   - Asegúrate de que Janitorr puede ver la misma ruta

3. **Depura con logs:**
   Habilita registro TRACE:
   ```yaml
   logging:
     level:
       com.github.schaka: TRACE
   ```

### Errores de Permisos Denegados

**Síntomas:** No puede leer/escribir archivos, errores de permisos en logs

**Soluciones:**

1. **Verifica ID de usuario/grupo:**
   ```bash
   # En tu host, encuentra tu ID de usuario
   id
   # Salida de ejemplo: uid=1000(miusuario) gid=1000(miusuario)
   ```

2. **Actualiza docker-compose.yml:**
   ```yaml
   user: 1000:1000  # Coincide con tu usuario del host
   ```

3. **Corrige propiedad de archivos:**
   ```bash
   sudo chown -R 1000:1000 /appdata/janitorr
   sudo chown -R 1000:1000 /share_media
   ```

4. **Verifica permisos de directorio:**
   ```bash
   # Los directorios necesitan permiso de ejecución
   sudo chmod -R 755 /share_media
   ```

## Problemas de Eliminación

### Los Archivos No se Eliminan

**Síntomas:** Los medios que deberían eliminarse permanecen

**Soluciones:**

1. **Verifica si dry-run está habilitado:**
   ```yaml
   dry-run: false  # Debe ser false para eliminar realmente
   ```

2. **Verifica que los medios cumplen requisitos de edad:**
   Revisa los logs para ver por qué se omitieron medios:
   ```bash
   docker logs janitorr 2>&1 | grep -i "skip\|keep\|too young"
   ```

3. **Verifica etiquetas de exclusión:**
   ```bash
   # En Sonarr/Radarr, verifica si los medios tienen:
   # - etiqueta janitorr_keep
   # - Cualquier etiqueta configurada en exclusion-tags
   ```

4. **Verifica que la limpieza está habilitada:**
   ```yaml
   media-cleanup:
     enabled: true  # Debe ser true
   ```

5. **Verifica umbral de disco:**
   Si la eliminación consciente del disco está habilitada:
   ```yaml
   disk-management:
     enabled: true
     threshold: 80  # Solo limpia cuando disco > 80%
   ```
   La eliminación no ocurrirá hasta que el uso del disco exceda el umbral.

6. **Medios no de *arr:**
   Janitorr solo gestiona medios descargados a través de Sonarr/Radarr. Los medios agregados manualmente no se eliminarán.

### Las Eliminaciones Ocurren Demasiado Agresivamente

**Síntomas:** Se está eliminando demasiado contenido

**Soluciones:**

1. **Aumenta minimum-days:**
   ```yaml
   media-cleanup:
     minimum-days: 60  # Mantén medios más tiempo
   ```

2. **Agrega etiquetas de exclusión:**
   Etiqueta medios que quieres mantener permanentemente:
   ```yaml
   exclusion-tags:
     - "janitorr_keep"
     - "favorito"
   ```

3. **Ajusta umbral de disco:**
   ```yaml
   disk-management:
     threshold: 90  # Solo limpiar cuando esté muy lleno
   ```

4. **Revisa primero en modo dry-run:**
   ```yaml
   dry-run: true
   ```
   Revisa los logs para ver qué se eliminaría.

### La Colección "Próximamente a Eliminar" No se Muestra

**Síntomas:** La colección no aparece en Jellyfin

**Soluciones:**

1. **Verifica que la colección está habilitada:**
   ```yaml
   leaving-soon:
     enabled: true
   ```

2. **Verifica biblioteca de Jellyfin:**
   - Ve a Jellyfin → Bibliotecas
   - Escanea biblioteca
   - Las colecciones deberían aparecer bajo "Colecciones"

3. **Verifica que se crearon enlaces simbólicos:**
   ```bash
   ls -la /share_media/media/leaving-soon
   ```

4. **Verifica ruta leaving-soon-dir:**
   ```yaml
   leaving-soon-dir: "/data/media/leaving-soon"
   media-server-leaving-soon-dir: "/data/media/leaving-soon"  # Vista de Jellyfin
   ```

5. **La biblioteca de Jellyfin debe incluir el directorio leaving-soon:**
   - En Jellyfin, ve a Panel → Bibliotecas
   - Tu biblioteca debe incluir la ruta leaving-soon

## Problemas de Rendimiento

### Uso Alto de Memoria

**Síntomas:** El contenedor usa demasiada RAM

**Soluciones:**

1. **Aumenta límite de memoria:**
   ```yaml
   mem_limit: 512M  # O superior para bibliotecas grandes
   ```

2. **Reduce frecuencia de escaneo:**
   ```yaml
   media-cleanup:
     schedule: "0 0 2 * * ?"  # Menos frecuente
   ```

3. **Usa imagen nativa** (si JVM es muy pesada):
   ```yaml
   image: ghcr.io/carcheky/janitorr-native:latest
   ```
   Nota: La imagen nativa está obsoleta pero usa menos memoria.

### Escaneos Lentos

**Síntomas:** La limpieza tarda mucho en completarse

**Causas:**
- Biblioteca grande
- Red lenta a servicios *arr
- Muchas llamadas API

**Soluciones:**

1. **Habilita procesamiento paralelo** (si está disponible en tu versión)

2. **Reduce frecuencia de llamadas API:**
   - Usa integración de estadísticas (Jellystat) para reducir llamadas API a *arr

3. **Programa durante horas de menor uso:**
   ```yaml
   media-cleanup:
     schedule: "0 0 3 * * ?"  # 3 AM cuando el servidor está menos ocupado
   ```

## Problemas de la Interfaz de Gestión

**✅ Nota:** La Interfaz de Gestión está completamente funcional en las versiones actuales. La mayoría de los problemas comunes han sido resueltos.

### No Se Puede Acceder a la Interfaz

**Síntomas:** No se puede acceder a la interfaz web en `http://localhost:8978/`

**Soluciones:**

1. **Verifica que estés usando la imagen más reciente:**
   ```bash
   docker-compose pull janitorr
   docker-compose up -d janitorr
   ```

2. **Verifica si la interfaz está habilitada:**
   ```bash
   docker logs janitorr | grep "Management UI"
   ```
   
   Debería mostrar:
   ```
   INFO - Management UI is ENABLED and available at http://localhost:8978/
   ```

3. **Verifica la variable de entorno:**
   ```bash
   docker exec janitorr printenv | grep JANITORR_UI_ENABLED
   ```
   
   Debería devolver `JANITORR_UI_ENABLED=true` (o nada, ya que true es el valor predeterminado)

4. **Verifica el mapeo de puertos:**
   ```yaml
   ports:
     - "8978:8978"  # Asegúrate de que esto esté en tu docker-compose.yml
   ```

5. **Prueba el endpoint:**
   ```bash
   curl http://localhost:8978/api/management/status
   ```
   
   Debería devolver JSON con el estado del sistema.

### La Interfaz Muestra Error 404

**✅ ¡Este problema ha sido CORREGIDO en las versiones actuales!**

Si todavía ves errores 404 en la Interfaz de Gestión:

**Síntomas:** Acceder a la URL raíz devuelve 404 No Encontrado

**Solución:**

1. **Actualiza a la imagen más reciente:**
   ```bash
   docker-compose pull janitorr
   docker-compose up -d janitorr
   ```
   
2. **Verifica la etiqueta de imagen:**
   ```yaml
   image: ghcr.io/carcheky/janitorr:latest  # Usa esta o :main para desarrollo
   ```

3. **Limpia la caché del navegador y vuelve a intentar**

**Comportamiento esperado con las imágenes actuales:**
- ✅ `http://localhost:8978/` muestra la Interfaz de Gestión
- ✅ Todos los botones y funciones trabajan correctamente
- ✅ Los endpoints de API devuelven respuestas adecuadas

### Los Endpoints de la API Devuelven 404

**✅ ¡Este problema ha sido CORREGIDO en las versiones actuales!**

Si `/api/management/status` devuelve 404:

**Solución:**
1. **Actualiza a la imagen más reciente** como se describe arriba
2. **Verifica el endpoint:**
   ```bash
   curl http://localhost:8978/api/management/status
   ```
3. **Revisa los logs del contenedor:**
   ```bash
   docker logs janitorr | grep "Management"
   ```

### Las Funciones de la Interfaz No Funcionan

**Síntomas:** Los botones no responden o las limpiezas no se activan

**Soluciones:**

1. **Verifica errores en la consola del navegador:**
   - Abre las herramientas de desarrollo del navegador (F12)
   - Busca errores de JavaScript en la pestaña Console

2. **Verifica la conectividad de la API:**
   ```bash
   curl -X POST http://localhost:8978/api/management/cleanup/media
   ```

3. **Revisa los logs de Janitorr:**
   ```bash
   docker logs -f janitorr
   ```
   Observa los mensajes de ejecución de limpieza.

### Deshabilitar la Interfaz

Si deseas ejecutar Janitorr sin la interfaz web:

**Método 1 - Variable de Entorno (recomendado):**
```yaml
environment:
  - JANITORR_UI_ENABLED=false
```

**Método 2 - Archivo de Configuración:**
```yaml
management:
  ui:
    enabled: false
```

Después de deshabilitar, los logs mostrarán:
```
INFO - Management UI is DISABLED by configuration (management.ui.enabled=false)
```

## Registro y Depuración

### Habilitar Registro de Depuración

```yaml
logging:
  level:
    root: INFO
    com.github.schaka: DEBUG  # o TRACE para aún más detalle
  file:
    name: /logs/janitorr.log
```

### Ver Logs

**Logs del contenedor:**
```bash
docker logs janitorr
docker logs -f janitorr  # Seguir logs en tiempo real
docker logs --tail 100 janitorr  # Últimas 100 líneas
```

**Logs de archivo:**
```bash
tail -f /appdata/janitorr/logs/janitorr.log
grep -i error /appdata/janitorr/logs/janitorr.log
```

### Mensajes de Log Comunes

**Nivel INFO:**
- `Starting Janitorr` - Aplicación iniciando
- `Connected to Sonarr` - Conexión exitosa
- `Would delete` (dry-run) - Qué se eliminaría
- `Deleted` - Eliminación real realizada

**Nivel WARNING:**
- `Skipping media - too young` - Los medios no cumplen requisitos de edad
- `API rate limit` - Demasiadas llamadas API

**Nivel ERROR:**
- `Failed to connect` - Problemas de conexión
- `Permission denied` - Problemas de permisos de archivo
- `Failed to delete` - Fallo en eliminación

### Lista de Verificación de Depuración

Cuando las cosas no funcionan:

1. ✅ Verifica que el contenedor está corriendo: `docker ps | grep janitorr`
2. ✅ Verifica logs: `docker logs janitorr`
3. ✅ Verifica configuración: `docker exec janitorr cat /config/application.yml`
4. ✅ Prueba conectividad de red: `docker exec janitorr ping sonarr`
5. ✅ Verifica permisos de archivo: `docker exec janitorr ls -la /data`
6. ✅ Habilita registro de depuración
7. ✅ Verifica espacio en disco: `df -h`
8. ✅ Revisa historial *arr para medios en cuestión

## Obtener Ayuda

Si has probado lo anterior y aún tienes problemas:

1. **Verifica problemas existentes:**
   [GitHub Issues](https://github.com/carcheky/janitorr/issues)

2. **Busca en discusiones:**
   [GitHub Discussions](https://github.com/carcheky/janitorr/discussions)

3. **Crea una nueva discusión:**
   Incluye:
   - Tu docker-compose.yml (elimina datos sensibles)
   - Secciones relevantes de application.yml (elimina claves API)
   - Logs del contenedor
   - Qué has intentado ya

4. **Reporta un error:**
   Si has encontrado un error, [abre un issue](https://github.com/carcheky/janitorr/issues/new) con:
   - Descripción detallada
   - Pasos para reproducir
   - Comportamiento esperado vs real
   - Logs mostrando el error

---

[← Volver al Inicio](Home.md) | [Guía de Configuración](Guia-Configuracion.md) | [Preguntas Frecuentes](Preguntas-Frecuentes.md)
