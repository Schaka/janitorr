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

# Configuración de la Interfaz de Gestión
management:
  ui:
    enabled: true  # Configura en false para deshabilitar la interfaz web

# Comportamiento de la aplicación
dry-run: true     # IMPORTANTE: Configura en false para habilitar eliminaciones reales
run-once: false   # Configura en true para ejecutar una vez y salir

# Rutas de medios
leaving-soon-dir: "/data/media/leaving-soon"
media-server-leaving-soon-dir: "/data/media/leaving-soon"
```

### Configuración de la Interfaz de Gestión

La Interfaz de Gestión proporciona una interfaz web para monitorear y controlar Janitorr:

```yaml
management:
  ui:
    enabled: true  # Habilitar/deshabilitar la interfaz web
  endpoints:
    web:
      exposure:
        include: health,info,management  # Endpoints de API a exponer
```

**Variables de Entorno (para Docker):**

```yaml
environment:
  - JANITORR_UI_ENABLED=true  # Habilitar/deshabilitar interfaz
  - SERVER_PORT=8080  # Cambiar puerto de la aplicación
  - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,management  # Controlar endpoints
```

**Cuándo deshabilitar la interfaz:**
- Entornos de servidor sin interfaz gráfica donde solo necesitas limpiezas programadas
- Preocupaciones de seguridad sobre exponer una interfaz web
- Ejecución en pipelines CI/CD o scripts automatizados
- Requisitos mínimos de uso de recursos

Consulta [MANAGEMENT_UI.md](../../../MANAGEMENT_UI.md) para documentación detallada de la interfaz.

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

### APIs Externas para Limpieza Inteligente

Janitorr puede integrarse con APIs externas para tomar decisiones de limpieza más inteligentes basadas en calificaciones, popularidad y datos de tendencias.

**APIs Soportadas:**
- **TMDB** (The Movie Database) - Calificaciones, popularidad, datos de tendencias
- **OMDb** (datos de IMDb) - Calificaciones de IMDb, puntuaciones de Metacritic, premios
- **Trakt.tv** - Estadísticas de visualización, datos de colección, información de tendencias

**Configuración:**

```yaml
external-apis:
  enabled: true
  cache-refresh-interval: 24h
  
  tmdb:
    enabled: true
    api-key: "tu-clave-api-tmdb"
    base-url: "https://api.themoviedb.org/3"
  
  omdb:
    enabled: true
    api-key: "tu-clave-api-omdb"
    base-url: "http://www.omdbapi.com"
  
  trakt:
    enabled: true
    client-id: "tu-client-id-trakt"
    client-secret: "tu-client-secret-trakt"
    base-url: "https://api.trakt.tv"
  
  scoring:
    tmdb-rating-weight: 0.25
    imdb-rating-weight: 0.25
    popularity-weight: 0.20
    trending-weight: 0.15
    availability-weight: 0.10
    collectibility-weight: 0.05
```

**Obtención de Claves API:**

1. **TMDB**: Regístrate en https://www.themoviedb.org y solicita una clave API en https://www.themoviedb.org/settings/api (gratis)
2. **OMDb**: Obtén una clave API en http://www.omdbapi.com/apikey.aspx (nivel gratuito disponible)
3. **Trakt**: Crea una aplicación en https://trakt.tv/oauth/applications

**Reglas de Limpieza Inteligente:**

Cuando las APIs externas están habilitadas, Janitorr preservará automáticamente contenido que:
- Tenga una calificación de IMDb ≥ 8.0
- Tenga una calificación de TMDB ≥ 8.0
- Esté actualmente en tendencia
- Tenga una alta puntuación de coleccionabilidad (contenido raro)
- Tenga una puntuación de inteligencia general ≥ 70

**Sistema de Puntuación:**

Los pesos de puntuación determinan cuánto contribuye cada factor a la puntuación general de inteligencia:
- Mayor peso = más influencia en la decisión
- Todos los pesos deben sumar aproximadamente 1.0
- Ajusta según tus preferencias (ej. priorizar calificaciones sobre tendencias)

**Beneficios:**
- **Decisiones Más Inteligentes**: Mantén contenido valioso basado en datos reales
- **Preservar Calidad**: Nunca elimines contenido altamente calificado o galardonado
- **Seguir Tendencias**: Mantén medios populares y en tendencia
- **Guardar Contenido Raro**: Protege elementos difíciles de encontrar o coleccionables

**Rendimiento:**

Las respuestas de API se almacenan en caché durante el `cache-refresh-interval` configurado (predeterminado: 24 horas) para minimizar las llamadas a API y mejorar el rendimiento.

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

## Motor de Inteligencia IA/ML (Característica Futura)

> **Estado:** 🚧 Fase de Planificación - No Implementada Aún  
> **Prioridad:** Baja (Característica Futura Avanzada)

El Motor de Inteligencia IA/ML es una característica futura que utilizará aprendizaje automático para optimizar las decisiones de limpieza basándose en patrones de visualización y preferencias del usuario.

### Descripción General

Cuando se implemente, esta característica:
- Analizará el historial de visualización para predecir qué medios deben conservarse
- Aprenderá de las decisiones y preferencias del usuario
- Proporcionará recomendaciones inteligentes con explicaciones
- Optimizará el momento de limpieza basándose en patrones de uso

### Configuración

La característica de IA puede configurarse en `application.yml`, pero está **deshabilitada por defecto**:

```yaml
ai:
  enabled: false  # Características de IA no implementadas aún
  model-path: /config/models
  training:
    enabled: false
    schedule: "0 0 3 * * ?"
    min-data-points: 1000
    historical-data-days: 90
  inference:
    cache-ttl: 3600
    batch-size: 100
    confidence-threshold: 0.7
    timeout-ms: 100
  features:
    external-apis: false  # Preservación de privacidad, solo local
    user-feedback: true   # Aprender de correcciones
    natural-language: false  # Característica futura
    computer-vision: false   # Característica futura
```

### Documentación de Arquitectura

Para información detallada sobre la arquitectura de IA/ML planificada:
- **Inglés:** [AI/ML Engine Architecture](../../AI_ML_ENGINE_ARCHITECTURE.md)
- **Español:** [Arquitectura del Motor IA/ML](../../ARQUITECTURA_MOTOR_IA_ML.md)

### Características Clave (Planificadas)

#### Modelo de Puntuación de Contenido
- Predice probabilidad de conservar/eliminar para cada elemento multimedia
- Considera: frecuencia de visualización, antigüedad, preferencias de género, impacto de almacenamiento
- Proporciona puntuaciones de confianza y explicaciones

#### Reconocimiento de Patrones
- Detecta horarios y hábitos de visualización
- Identifica patrones de maratón para series activas
- Reconoce preferencias estacionales

#### Análisis Predictivo
- Pronostica necesidades de almacenamiento
- Sugiere momento óptimo de limpieza
- Recomienda contenido a conservar basándose en tendencias

### Privacidad y Ética

El motor de IA está diseñado con la privacidad en mente:
- **Procesamiento Local:** Todo ML se ejecuta localmente, sin compartir datos externos
- **Anonimización:** Los IDs de usuario se hashean antes de procesarse
- **Transparencia:** Todas las decisiones vienen con explicaciones
- **Control del Usuario:** Fácil opt-out y capacidad de anulación
- **Retención de Datos:** Datos de entrenamiento purgados después de 90 días

### Estado Actual

Esta característica está en la fase de arquitectura y planificación. El código base incluye:
- Estructura de configuración (`AIProperties`)
- Modelos de datos para características ML (`MediaFeatures`, `ViewingSession`)
- Interfaces de servicio (`InferenceEngine`, `ContentScoringModel`)
- Implementaciones placeholder

**Para contribuir o seguir el progreso:**
- Revisa la documentación de arquitectura
- Proporciona retroalimentación sobre requisitos de características
- Sugiere algoritmos ML y enfoques

### ¿Cuándo Estará Disponible?

Esta es una característica compleja a largo plazo. Cronograma de implementación:
- **Fase 1:** Infraestructura de recopilación de datos (2-3 meses)
- **Fase 2:** Modelos ML centrales (3-4 meses)
- **Fase 3:** Características de inteligencia (2-3 meses)
- **Fase 4:** Integración de UI (2 meses)
- **Fase 5:** Características avanzadas (3-4 meses)

**Tiempo estimado total:** 12-16 meses

Consulta [GitHub Issues](https://github.com/carcheky/janitorr/issues) para estado actual y discusiones.

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

---

[← Volver al Inicio](Home.md)
