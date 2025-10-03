# Guía de Configuración

Esta guía cubre la configuración de Janitorr a través del archivo `application.yml`.

## Resumen

Janitorr se configura a través de un archivo YAML que debe proporcionarse al iniciar el contenedor. El archivo de configuración controla todos los aspectos del comportamiento de Janitorr.

## Obtener la Plantilla

Descarga la plantilla de configuración:

```bash
wget -O /appdata/janitorr/config/application.yml \
  https://raw.githubusercontent.com/carcheky/janitorr/main/src/main/resources/application-template.yml
```

O descarga manualmente desde: [application-template.yml](https://github.com/carcheky/janitorr/blob/main/src/main/resources/application-template.yml)

## Configuración Básica

### Ajustes Esenciales

Estos ajustes son necesarios para que Janitorr funcione:

```yaml
# Configuración del servidor
server:
  port: 8978  # Puerto en el que Janitorr escucha

# Comportamiento de la aplicación
dry-run: true     # IMPORTANTE: Configura en false para habilitar eliminaciones reales
run-once: false   # Configura en true para ejecutar una vez y salir

# Rutas de medios
leaving-soon-dir: "/data/media/leaving-soon"
media-server-leaving-soon-dir: "/data/media/leaving-soon"
```

### Conectar a Aplicaciones *arr

Configura al menos una aplicación *arr:

```yaml
# Configuración de Sonarr
sonarr:
  - url: "http://sonarr:8989"
    api-key: "tu-clave-api-sonarr"
    
# Configuración de Radarr
radarr:
  - url: "http://radarr:7878"
    api-key: "tu-clave-api-radarr"
```

Puedes configurar múltiples instancias de cada una:

```yaml
sonarr:
  - url: "http://sonarr-4k:8989"
    api-key: "clave-api-1"
  - url: "http://sonarr-1080p:8990"
    api-key: "clave-api-2"
```

### Configuración del Servidor Multimedia

Configura Jellyfin O Emby (no ambos):

**Jellyfin:**
```yaml
jellyfin:
  enabled: true
  url: "http://jellyfin:8096"
  api-key: "tu-clave-api-jellyfin"
  username: "janitorr"      # Usuario con permisos de eliminación
  password: "tu-contraseña"
```

**Emby:**
```yaml
emby:
  enabled: true
  url: "http://emby:8096"
  api-key: "tu-clave-api-emby"
  username: "janitorr"
  password: "tu-contraseña"
```

**Nota:** Se requiere una cuenta de usuario (no solo clave API) para eliminar archivos.

## Configuración Avanzada

### Gestión de Solicitudes (Jellyseerr)

Limpia solicitudes de medios eliminados:

```yaml
jellyseerr:
  enabled: true
  url: "http://jellyseerr:5050"
  api-key: "tu-clave-api-jellyseerr"
```

### Integración de Estadísticas

Configura Jellystat O Streamystats (no ambos):

**Jellystat:**
```yaml
jellystat:
  enabled: true
  url: "http://jellystat:3000"
  api-key: "tu-clave-api-jellystat"
```

**Streamystats:**
```yaml
streamystats:
  enabled: true
  url: "http://streamystats:8080"
  api-key: "tu-clave-api-streamystats"
```

Cuando esté configurado, el historial de visualización se usará para determinar la edad de los medios en lugar de solo la fecha de descarga.

### Programaciones de Limpieza

#### Limpieza de Medios

Limpia películas y series basándose en edad y espacio en disco:

```yaml
media-cleanup:
  enabled: true
  schedule: "0 0 2 * * ?"  # Cron: Diariamente a las 2 AM
  minimum-days: 30          # Mantener medios al menos 30 días
  disk-threshold: 80        # Solo limpiar cuando el disco esté al 80% (opcional)
```

#### Limpieza Basada en Etiquetas

Elimina medios basándose en etiquetas con expiración personalizada:

```yaml
tag-cleanup:
  enabled: true
  schedule: "0 0 3 * * ?"  # Cron: Diariamente a las 3 AM
  tags:
    - name: "eliminar_90_dias"
      days: 90
    - name: "eliminar_30_dias"
      days: 30
```

Crea estas etiquetas en Sonarr/Radarr y aplícalas a medios que deben expirar después de los días especificados.

#### Limpieza de Episodios

Limpia episodios individuales para series etiquetadas para gestión a nivel de episodio:

```yaml
episode-cleanup:
  enabled: true
  schedule: "0 0 4 * * ?"  # Cron: Diariamente a las 4 AM
  episode-tag: "janitorr_episodes"  # Etiqueta en Sonarr
  minimum-days: 7                    # Mantener episodios al menos 7 días
  maximum-episodes: 10               # Mantener máx. 10 episodios no vistos por serie
```

Aplica la `episode-tag` a series donde quieres limpieza a nivel de episodio en lugar de eliminar series completas.

### Etiquetas de Exclusión

Evita que medios específicos sean eliminados:

```yaml
exclusion-tags:
  - "janitorr_keep"    # Etiqueta de exclusión predeterminada
  - "favorito"
  - "protegido"
```

Cualquier medio con estas etiquetas en Sonarr/Radarr nunca será eliminado.

### Colección "Próximamente a Eliminar"

Configura la colección "Próximamente a Eliminar" mostrada en Jellyfin/Emby:

```yaml
leaving-soon:
  enabled: true
  days-before-deletion: 7   # Mostrar en colección 7 días antes de eliminar
  collection-name: "Próximamente a Eliminar"
```

**Importante:** Esta colección se crea incluso en modo dry-run.

### Registro de Eventos

Configura el comportamiento del registro:

```yaml
logging:
  level:
    root: INFO
    com.github.schaka: DEBUG  # Cambia a DEBUG o TRACE para más detalle
  file:
    name: /logs/janitorr.log
    max-size: 10MB
    max-history: 30
```

**Niveles de Registro:**
- `ERROR` - Solo errores
- `WARN` - Advertencias y errores
- `INFO` - Información general (recomendado)
- `DEBUG` - Información detallada de depuración
- `TRACE` - Información de rastreo muy detallada

## Configuración de la Interfaz de Gestión

La Interfaz de Gestión está habilitada por defecto al usar la imagen JVM.

### Configuración de Puerto

La UI usa el mismo puerto que la aplicación principal:

```yaml
server:
  port: 8978  # Cambia para usar un puerto diferente
```

### Accediendo a la UI

Una vez que Janitorr esté ejecutándose:
```
http://<ip-de-tu-servidor>:8978/
```

### Mapeo de Puerto en Docker

Mapea el puerto en docker-compose.yml:
```yaml
ports:
  - "8978:8978"  # host:contenedor
```

Para usar un puerto de host diferente:
```yaml
ports:
  - "9000:8978"  # Acceder en http://localhost:9000/
```

### Requisitos de Imagen

⚠️ **Importante:** La Interfaz de Gestión solo está disponible en la imagen JVM.

```yaml
# ✅ Incluye Interfaz de Gestión
image: ghcr.io/carcheky/janitorr:jvm-stable

# ❌ NO incluye Interfaz de Gestión (obsoleta)
image: ghcr.io/carcheky/janitorr:native-stable
```

Para documentación completa de la UI, consulta [MANAGEMENT_UI.md](../../MANAGEMENT_UI.md).

## Configuración de Rutas

### Entendiendo el Mapeo de Rutas

**Crítico:** ¡Las rutas deben ser consistentes en todos los contenedores!

#### Configuración Simple (Recomendada)

Todos los contenedores usan el mismo mapeo de volumen:

**Docker Compose:**
```yaml
volumes:
  - /share_media:/data
```

**application.yml:**
```yaml
leaving-soon-dir: "/data/media/leaving-soon"
media-server-leaving-soon-dir: "/data/media/leaving-soon"
```

#### Configuración Compleja (Ruta Diferente de Jellyfin)

Cuando Jellyfin ve rutas de manera diferente:

**Volúmenes de Janitorr:**
```yaml
volumes:
  - /share_media:/data
```

**Volúmenes de Jellyfin:**
```yaml
volumes:
  - /share_media/media/leaving-soon:/library/leaving-soon
```

**application.yml:**
```yaml
leaving-soon-dir: "/data/media/leaving-soon"           # Como lo ve Janitorr
media-server-leaving-soon-dir: "/library/leaving-soon" # Como lo ve Jellyfin
```

### Acceso al Sistema de Archivos

Cuando se usa acceso al sistema de archivos (no solo API):

```yaml
jellyfin:
  filesystem-access: true  # Usar operaciones del sistema de archivos
```

Esto requiere:
- Janitorr y Jellyfin tienen exactamente la misma vista de las rutas de la biblioteca
- El directorio leaving-soon es accesible para ambos

## Gestión de Disco

### Eliminación Consciente del Espacio en Disco

Solo eliminar cuando el uso del disco exceda un umbral:

```yaml
disk-management:
  enabled: true
  threshold: 85        # Solo limpiar cuando el disco esté al 85%
  target: 70          # Limpiar hasta que el disco esté al 70%
  path: "/data"       # Ruta a monitorear
```

Esto evita eliminaciones innecesarias cuando tienes mucho espacio.

### Cálculo de Espacio Libre

Configura cómo se calcula el espacio libre:

```yaml
free-space:
  buffer-gb: 100  # Siempre intentar mantener 100GB libres
```

## Ejemplos de Programación Cron

Formato cron: `segundo minuto hora día mes díasemana`

```yaml
# Todos los días a las 2 AM
schedule: "0 0 2 * * ?"

# Todos los domingos a las 3 AM
schedule: "0 0 3 ? * SUN"

# Cada 6 horas
schedule: "0 0 */6 * * ?"

# Primer día de cada mes a medianoche
schedule: "0 0 0 1 * ?"

# Días laborables a las 2 AM
schedule: "0 0 2 ? * MON-FRI"
```

## Consideraciones de Seguridad

### Claves API

- Nunca subas claves API al control de versiones
- Usa claves API fuertes y únicas para cada servicio
- Rota las claves API periódicamente

### Permisos de Usuario

El usuario de Janitorr necesita:
- Acceso de lectura a todos los directorios de medios
- Acceso de escritura al directorio leaving-soon
- Permisos de eliminación en Jellyfin/Emby (requiere cuenta de usuario)

### Acceso a la Red

Si expones la Interfaz de Gestión:
- Usa un proxy inverso con autenticación
- Considera usar HTTPS
- Restringe el acceso por IP si es posible

### Acceso a la Interfaz de Gestión

La Interfaz de Gestión no tiene autenticación integrada. Para asegurarla:

**Opción 1: Sin acceso externo (recomendado para la mayoría)**
```yaml
# docker-compose.yml
ports:
  # Elimina o comenta la sección de puertos
  # - "8978:8978"  # No exponer al host
```
Accede a la UI solo desde otros contenedores en la misma red Docker.

**Opción 2: Proxy inverso con autenticación**

Usa Nginx, Traefik o similar con autenticación básica:

```nginx
# Ejemplo Nginx
location / {
    auth_basic "Janitorr Admin";
    auth_basic_user_file /etc/nginx/.htpasswd;
    proxy_pass http://janitorr:8978;
}
```

**Opción 3: Restricción por IP**

Restringe el acceso a IPs específicas:
```nginx
location / {
    allow 192.168.1.0/24;  # Tu red local
    deny all;
    proxy_pass http://janitorr:8978;
}
```

## Ejemplo de Configuración Completa

Consulta [Configuración con Docker Compose](Configuracion-Docker-Compose.md#ejemplo-de-stack-completo) para un ejemplo completo con todos los servicios configurados.

## Probando Tu Configuración

1. **Comienza con dry-run habilitado:**
   ```yaml
   dry-run: true
   ```

2. **Habilita registro de depuración:**
   ```yaml
   logging:
     level:
       com.github.schaka: DEBUG
   ```

3. **Inicia Janitorr y revisa los logs:**
   ```bash
   docker-compose up -d
   docker logs -f janitorr
   ```

4. **Verifica las conexiones:**
   - Comprueba que Janitorr puede conectarse a todos los servicios *arr
   - Verifica conexión a Jellyfin/Emby
   - Confirma que la colección leaving-soon se crea

5. **Revisa qué sería eliminado:**
   - Revisa los logs para mensajes "Would delete"
   - Verifica que los medios seleccionados tienen sentido
   - Ajusta umbrales de edad según sea necesario

6. **Deshabilita dry-run cuando esté listo:**
   ```yaml
   dry-run: false
   ```

## Validación de Configuración

Errores comunes de configuración:

❌ **Incorrecto:**
```yaml
leaving-soon-dir: /data/leaving-soon      # Faltan comillas
dry-run: True                              # T mayúscula (debe ser minúscula)
```

✅ **Correcto:**
```yaml
leaving-soon-dir: "/data/leaving-soon"
dry-run: true
```

## Solución de Problemas

### La Configuración No se Carga

- Verifica que el archivo está en la ruta correcta
- Revisa sintaxis YAML (indentación, dos puntos, comillas)
- Revisa los logs del contenedor para errores de análisis

### Los Servicios No se Conectan

- Verifica que las URLs son accesibles desde dentro del contenedor
- Comprueba que las claves API son correctas
- Asegúrate de que los servicios están en la misma red Docker o accesibles por IP

### Rutas No Encontradas

- Verifica que los mapeos de volumen son correctos
- Comprueba que las rutas en application.yml coinciden con las rutas del contenedor
- Asegúrate de que los permisos son correctos

## Próximos Pasos

- [Configuración con Docker Compose](Configuracion-Docker-Compose.md) - Guía completa de implementación
- [Preguntas Frecuentes](Preguntas-Frecuentes.md) - Preguntas comunes
- [Solución de Problemas](Solucion-Problemas.md) - Resolución detallada de problemas
- [Guía de Interfaz de Gestión](../../MANAGEMENT_UI.md) - Documentación de la interfaz web

---

[← Volver al Inicio](Home.md)
