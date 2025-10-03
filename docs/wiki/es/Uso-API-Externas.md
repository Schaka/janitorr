# Integración de APIs Externas - Ejemplos de Uso

Este documento proporciona ejemplos de cómo integrar la inteligencia de APIs externas en tus flujos de limpieza.

## Descripción General

La integración de APIs externas proporciona puntuación inteligente para elementos multimedia basada en:
- Calificaciones y popularidad de TMDB
- Calificaciones de IMDb y premios
- Estadísticas de visualización de Trakt y datos de tendencias

## Uso Básico

### 1. Habilitar APIs Externas

Primero, configura tu `application.yml`:

```yaml
external-apis:
  enabled: true
  cache-refresh-interval: 24h
  
  tmdb:
    enabled: true
    api-key: "tu-clave-api-tmdb"
  
  omdb:
    enabled: true
    api-key: "tu-clave-api-omdb"
  
  trakt:
    enabled: true
    client-id: "tu-client-id"
    client-secret: "tu-client-secret"
```

### 2. Enriquecer Datos de Medios

El `ExternalDataService` enriquece los elementos de biblioteca con puntuaciones de inteligencia:

```kotlin
import com.github.schaka.janitorr.external.common.ExternalDataService
import com.github.schaka.janitorr.servarr.LibraryItem

// Inyectar el servicio
class CleanupService(
    private val externalDataService: ExternalDataService
) {
    
    fun processLibraryItem(item: LibraryItem) {
        // Obtener datos de inteligencia para el elemento
        val intelligence = externalDataService.enrichMediaData(item)
        
        // Verificar si el elemento debe preservarse
        if (externalDataService.shouldPreserveMedia(intelligence)) {
            log.info("Preservando ${item.filePath} - Puntuación de inteligencia: ${intelligence.overallScore}")
            return // Omitir eliminación
        }
        
        // Continuar con lógica de limpieza normal
        deleteMedia(item)
    }
}
```

### 3. Entendiendo las Puntuaciones de Inteligencia

El objeto `MediaIntelligence` contiene:

```kotlin
data class MediaIntelligence(
    val tmdbRating: Double? = null,        // Escala 0-10
    val imdbRating: Double? = null,        // Escala 0-10
    val popularityScore: Double? = null,   // Escala 0-100
    val trendingScore: Double? = null,     // Escala 0-100
    val availabilityScore: Double? = null, // Escala 0-100 (futuro)
    val collectibilityScore: Double? = null, // Escala 0-100
    val overallScore: Double = 0.0       // Compuesto ponderado 0-100
)
```

### 4. Lógica de Preservación Personalizada

También puedes implementar reglas personalizadas:

```kotlin
fun shouldKeepMedia(item: LibraryItem, intelligence: MediaIntelligence): Boolean {
    // Mantener todo el contenido en tendencia
    if ((intelligence.trendingScore ?: 0.0) >= 50.0) {
        return true
    }
    
    // Mantener ganadores de premios (de OMDb)
    if (omdbService.hasAwards(item)) {
        return true
    }
    
    // Mantener clásicos altamente calificados
    if ((intelligence.imdbRating ?: 0.0) >= 7.5 && 
        (intelligence.collectibilityScore ?: 0.0) >= 60.0) {
        return true
    }
    
    // Usar lógica de preservación predeterminada
    return externalDataService.shouldPreserveMedia(intelligence)
}
```

## Integración con Programaciones de Limpieza

### Eliminación de Medios con Inteligencia

```kotlin
@Scheduled(cron = "\${application.media-deletion.schedule}")
fun cleanupMedia() {
    val items = getAllLibraryItems()
    
    items.forEach { item ->
        // Obtener datos de inteligencia
        val intelligence = externalDataService.enrichMediaData(item)
        
        // Registrar información de inteligencia
        log.debug("""
            Analizando ${item.filePath}:
            - TMDB: ${intelligence.tmdbRating}
            - IMDb: ${intelligence.imdbRating}
            - Tendencia: ${intelligence.trendingScore}
            - General: ${intelligence.overallScore}
        """.trimIndent())
        
        // Verificar preservación
        if (externalDataService.shouldPreserveMedia(intelligence)) {
            log.info("PRESERVADO: ${item.filePath} (puntuación: ${intelligence.overallScore})")
            continue
        }
        
        // Lógica de limpieza normal
        if (shouldDeleteBasedOnAge(item)) {
            deleteMedia(item)
        }
    }
}
```

## Configuración de Puntuación

Ajusta los pesos para que coincidan con tus preferencias:

```yaml
external-apis:
  scoring:
    # Favorecer fuertemente las calificaciones
    tmdb-rating-weight: 0.35
    imdb-rating-weight: 0.35
    
    # Menos importancia en tendencias
    popularity-weight: 0.10
    trending-weight: 0.10
    
    # Consideración mínima para disponibilidad/coleccionabilidad
    availability-weight: 0.05
    collectibility-weight: 0.05
```

## Consideraciones de Rendimiento

### Almacenamiento en Caché

Las respuestas de API se almacenan en caché durante el intervalo configurado:

```yaml
external-apis:
  cache-refresh-interval: 24h  # Refrescar una vez al día
```

### Procesamiento por Lotes

Para bibliotecas grandes, considera procesar en lotes:

```kotlin
fun cleanupLargeLibrary() {
    val items = getAllLibraryItems()
    
    items.chunked(100).forEach { batch ->
        batch.parallelStream().forEach { item ->
            val intelligence = externalDataService.enrichMediaData(item)
            processItem(item, intelligence)
        }
    }
}
```

## Solución de Problemas

### Claves API Faltantes

Si las claves API no están configuradas, el servicio se degradará elegantemente:

```kotlin
// Devuelve inteligencia vacía si las APIs están deshabilitadas
val intelligence = externalDataService.enrichMediaData(item)
// intelligence.overallScore será 0.0
```

### Límites de Tasa de API

Si alcanzas los límites de tasa:
1. Aumenta `cache-refresh-interval`
2. Deshabilita APIs menos críticas
3. Reduce la frecuencia de limpieza

### Registro de Depuración

Habilita el registro de depuración para ver llamadas a API:

```yaml
logging:
  level:
    com.github.schaka.janitorr.external: DEBUG
```

## Escenarios de Ejemplo

### Escenario 1: Mantener Ganadores de Oscar

```kotlin
val intelligence = externalDataService.enrichMediaData(item)
if (omdbService.hasAwards(item)) {
    log.info("Manteniendo ganador de premio: ${item.filePath}")
    return
}
```

### Escenario 2: Eliminar Contenido Antiguo Poco Calificado

```kotlin
val intelligence = externalDataService.enrichMediaData(item)
if ((intelligence.imdbRating ?: 10.0) < 6.0 && 
    item.historyAge.isBefore(LocalDateTime.now().minusMonths(3))) {
    log.info("Eliminando contenido antiguo poco calificado: ${item.filePath}")
    deleteMedia(item)
}
```

### Escenario 3: Mantener Programas en Tendencia

```kotlin
val intelligence = externalDataService.enrichMediaData(item)
if ((intelligence.trendingScore ?: 0.0) >= 75.0) {
    log.info("Manteniendo contenido en tendencia: ${item.filePath}")
    addToTrendingCollection(item)
    return
}
```

## Mejores Prácticas

1. **Siempre manejar nulos**: Los datos de API pueden no estar disponibles
2. **Usar degradación elegante**: No fallar la limpieza si las APIs están caídas
3. **Registrar decisiones de preservación**: Rastrear por qué se mantuvo el contenido
4. **Monitorear uso de API**: Vigilar problemas de límite de tasa
5. **Probar con dry-run**: Verificar comportamiento antes de habilitar eliminaciones
6. **Ajustar pesos gradualmente**: Afinar puntuación con el tiempo

## Próximos Pasos

Para más información:
- [Guía de Configuración](Guia-Configuracion.md)
- [Solución de Problemas](Solucion-Problemas.md)
- [Preguntas Frecuentes](Preguntas-Frecuentes.md)
