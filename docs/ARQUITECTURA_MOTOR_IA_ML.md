# Arquitectura del Motor de Inteligencia IA/ML

## Descripción General

El Motor de Inteligencia IA/ML es una característica avanzada para Janitorr que utiliza aprendizaje automático para optimizar las decisiones de limpieza de medios basándose en patrones de visualización, preferencias del usuario y análisis predictivos.

**Estado**: 🚧 **Desarrollo Futuro** - Documentación de Arquitectura  
**Prioridad**: 🟢 Baja (Característica Futura Avanzada)

## Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                 Motor de Inteligencia IA/ML                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │  Recopilación  │  │  Ingeniería  │  │  Pipeline de     │   │
│  │   de Datos     │─▶│     de       │─▶│ Entrenamiento    │   │
│  │                │  │ Características│  │   de Modelos    │   │
│  └────────────────┘  └──────────────┘  └──────────────────┘   │
│          │                                       │              │
│          ▼                                       ▼              │
│  ┌────────────────┐                    ┌──────────────────┐   │
│  │  Almacén de    │                    │  Repositorio de  │   │
│  │    Datos de    │                    │     Modelos      │   │
│  │ Entrenamiento  │                    │   Entrenados     │   │
│  └────────────────┘                    └──────────────────┘   │
│                                                 │              │
│                                                 ▼              │
│  ┌────────────────────────────────────────────────────────┐   │
│  │        Motor de Inferencia en Tiempo Real              │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐  │   │
│  │  │ Puntuador de │  │  Predictor   │  │ Explicador  │  │   │
│  │  │  Contenido   │  │              │  │             │  │   │
│  │  └──────────────┘  └──────────────┘  └─────────────┘  │   │
│  └────────────────────────────────────────────────────────┘   │
│                              │                                 │
└──────────────────────────────┼─────────────────────────────────┘
                               ▼
                    ┌──────────────────────┐
                    │  Decisiones de       │
                    │  Limpieza y          │
                    │  Recomendaciones     │
                    └──────────────────────┘
```

## Componentes Principales

### 1. Pipeline de Recopilación de Datos

Agrega datos de múltiples fuentes:

- **Historial de Visualización**: De Tautulli, Jellystat o Streamystats
- **Metadatos de Medios**: De Jellyfin/Emby y servicios *arr
- **Decisiones del Usuario**: Decisiones manuales de limpieza y anulaciones
- **APIs Externas**: Disponibilidad en streaming, tendencias sociales, calificaciones
- **Métricas del Sistema**: Uso de almacenamiento, datos de rendimiento

**Modelo de Datos**:
```kotlin
data class ViewingSession(
    val mediaId: String,
    val userId: String,
    val startTime: Instant,
    val endTime: Instant,
    val completionPercentage: Double,
    val device: String,
    val timeOfDay: LocalTime,
    val dayOfWeek: DayOfWeek
)

data class CleanupDecision(
    val mediaId: String,
    val userDecision: Decision, // KEEP, DELETE, ARCHIVE
    val aiRecommendation: Double,
    val features: Map<String, Double>,
    val timestamp: Instant
)
```

### 2. Ingeniería de Características

Transforma datos brutos en características listas para ML:

**Categorías de Características**:
- **Características Temporales**: Frecuencia de visualización, antigüedad, patrones estacionales
- **Características de Contenido**: Género, calificación, calidad, tamaño
- **Comportamiento del Usuario**: Patrones de visualización, preferencias de género, hábitos de maratón
- **Métricas de Almacenamiento**: Presión de disco, tasa de crecimiento
- **Señales Externas**: Disponibilidad en streaming, estado de tendencia

**Vector de Características**:
```kotlin
data class MediaFeatures(
    val watchFrequency: Double,           // Vistas por semana
    val daysSinceLastWatch: Int,          // Antigüedad
    val avgCompletionRate: Double,        // Cuánto ven los usuarios
    val genreScore: Double,               // Preferencia de género del usuario
    val trendingScore: Double,            // Popularidad externa
    val storageImpact: Double,            // Tamaño en GB
    val seasonalRelevance: Double,        // Coincidencia de patrón estacional
    val similarContentBehavior: Double    // Cómo se trata contenido similar
)
```

### 3. Modelos ML

#### Modelo de Puntuación de Contenido
Predice la probabilidad de que los medios deban conservarse:

```kotlin
interface ContentScoringModel {
    /**
     * Predecir probabilidad de que los medios deban conservarse (0.0 a 1.0)
     */
    fun predictKeepProbability(features: MediaFeatures): Double
    
    /**
     * Generar explicación legible de la puntuación
     */
    fun explainPrediction(features: MediaFeatures, score: Double): String
    
    /**
     * Obtener importancia de características para esta predicción
     */
    fun getFeatureImportance(features: MediaFeatures): Map<String, Double>
}
```

**Tipos de Modelos**:
- **Gradient Boosting** (principal): XGBoost o LightGBM
- **Random Forest** (alternativo): Robusto contra sobreajuste
- **Red Neuronal** (experimental): Para patrones complejos

#### Modelos de Reconocimiento de Patrones

- **Detector de Patrones Temporales**: Identifica horarios de visualización
- **Agrupación por Género**: Agrupa contenido por preferencias del usuario
- **Detección de Maratones**: Reconoce visualización activa de series
- **Detección de Anomalías**: Encuentra patrones inusuales de visualización

#### Modelos Predictivos

- **Pronóstico de Almacenamiento**: Predice tendencias de uso de disco
- **Temporización Óptima**: Mejor momento para operaciones de limpieza
- **Crecimiento de Biblioteca**: Requisitos futuros de espacio
- **Relevancia de Contenido**: Probabilidad de conservación a largo plazo

### 4. Pipeline de Entrenamiento

```kotlin
class MLTrainingPipeline(
    val dataCollector: DataCollector,
    val featureEngineer: FeatureEngineer,
    val modelTrainer: ModelTrainer,
    val validator: ModelValidator
) {
    /**
     * Ejecución completa del pipeline de entrenamiento
     */
    suspend fun trainModels(): TrainingResult {
        val rawData = dataCollector.collectTrainingData()
        val features = featureEngineer.transform(rawData)
        val splitData = features.trainTestSplit(testSize = 0.2)
        
        val models = modelTrainer.trainMultiple(
            splitData.train,
            algorithms = listOf(GRADIENT_BOOST, RANDOM_FOREST)
        )
        
        val bestModel = validator.selectBestModel(
            models,
            splitData.test,
            metrics = listOf(PRECISION, RECALL, F1_SCORE)
        )
        
        return TrainingResult(bestModel, validator.getMetrics())
    }
}
```

### 5. Motor de Inferencia

Servicio de predicción en tiempo real:

```kotlin
interface InferenceEngine {
    /**
     * Obtener recomendación de IA para un elemento multimedia individual
     */
    fun getRecommendation(mediaId: String): AIRecommendation
    
    /**
     * Procesar por lotes toda la biblioteca (para trabajos nocturnos)
     */
    suspend fun batchScore(libraryItems: List<LibraryItem>): Map<String, AIRecommendation>
    
    /**
     * Obtener nivel de confianza del modelo
     */
    fun getConfidence(mediaId: String): ConfidenceLevel
}

data class AIRecommendation(
    val action: RecommendedAction,      // KEEP, DELETE, ARCHIVE, REVIEW
    val confidence: Double,              // 0.0 a 1.0
    val reasoning: String,               // Explicación legible
    val alternativeScenarios: List<Scenario>
)
```

## Requisitos de Rendimiento

### Latencia
- **Predicción Individual**: < 100ms
- **Procesamiento por Lotes**: < 1 minuto por 1000 elementos
- **Carga de Modelo**: < 5 segundos al inicio

### Objetivos de Precisión
- **Precisión**: > 85% (recomendaciones seguidas)
- **Recall**: > 75% (contenido importante guardado)
- **Puntuación F1**: > 80% (rendimiento equilibrado)
- **Satisfacción del Usuario**: > 4.0/5.0

### Restricciones de Recursos
- **Memoria**: < 512MB para modelo + inferencia
- **CPU**: < 10% utilización promedio
- **Almacenamiento**: < 100MB para archivos de modelo

## Privacidad y Ética

### Protección de Datos
- **Anonimización**: Todos los IDs de usuario hasheados antes del procesamiento ML
- **Procesamiento Local**: No se envían datos a servicios ML externos
- **Retención de Datos**: Datos de entrenamiento purgados después de 90 días
- **Opt-out**: Fácil desactivación de todas las características de IA

### Explicabilidad
- **Decisiones Transparentes**: Siempre mostrar razonamiento
- **Importancia de Características**: Mostrar qué factores influyeron en la decisión
- **Capacidad de Anulación**: Los usuarios siempre pueden rechazar recomendaciones de IA
- **Registro de Auditoría**: Registrar todas las decisiones de IA para revisión

### Prevención de Sesgos
- **Monitoreo de Equidad**: Verificar sesgos demográficos
- **Entrenamiento Diverso**: Incluir patrones de usuario variados
- **Reentrenamiento Regular**: Adaptarse a preferencias cambiantes
- **Supervisión Humana**: Marcar decisiones de baja confianza para revisión

## Puntos de Integración

### Servicios Existentes
- **StatsService**: Fuente de datos de historial de visualización
- **ServarrService**: Metadatos y gestión de medios
- **MediaServerService**: Integración con Jellyfin/Emby
- **CleanupSchedule**: Inyectar recomendaciones de IA

### Nueva Configuración
```yaml
ai:
  enabled: false                # Características de IA deshabilitadas por defecto
  model-path: /config/models    # Dónde almacenar modelos entrenados
  training:
    enabled: false              # Entrenamiento manual solo inicialmente
    schedule: "0 0 3 * * ?"     # 3 AM diario si está habilitado
    min-data-points: 1000       # Mínimo de datos antes de entrenar
  inference:
    cache-ttl: 3600             # Cachear predicciones por 1 hora
    batch-size: 100             # Elementos por lote
    confidence-threshold: 0.7   # Confianza mínima para actuar
  features:
    external-apis: false        # Deshabilitar datos externos por defecto
    user-feedback: true         # Aprender de correcciones de usuario
```

## Plan de Implementación por Fases

### Fase 1: Fundación (Meses 1-2)
- [ ] Infraestructura de recopilación de datos
- [ ] Pipeline de ingeniería de características
- [ ] Configuración y propiedades básicas
- [ ] Almacenamiento y esquemas de datos

### Fase 2: ML Central (Meses 3-4)
- [ ] Modelo de puntuación de contenido
- [ ] Pipeline de entrenamiento
- [ ] Validación y pruebas de modelo
- [ ] Básicos del motor de inferencia

### Fase 3: Inteligencia (Meses 5-6)
- [ ] Modelos de reconocimiento de patrones
- [ ] Análisis predictivo
- [ ] Motor de explicación
- [ ] Framework de pruebas A/B

### Fase 4: UI y UX (Meses 7-8)
- [ ] Panel de recomendaciones de IA
- [ ] Sistema de retroalimentación interactiva
- [ ] Visualización de decisiones
- [ ] Ajuste de preferencias de usuario

### Fase 5: Características Avanzadas (Meses 9-12)
- [ ] Interfaz de lenguaje natural
- [ ] Integración de visión por computadora
- [ ] Integraciones de API externas
- [ ] Sistema de aprendizaje continuo

## Stack Tecnológico

### Bibliotecas ML (Opciones)
- **Principal**: [Kotlin for ML](https://github.com/Kotlin/kotlindl) o [DJL](https://djl.ai/)
- **Alternativa**: Microservicio Python con gRPC (si Kotlin ML es insuficiente)
- **Formato de Modelo**: ONNX para portabilidad

### Almacenamiento
- **Datos de Entrenamiento**: Base de datos embebida SQLite o H2
- **Modelos**: Sistema de archivos con versionado
- **Caché**: Caffeine (ya en uso)

### Monitoreo
- **Métricas**: Micrometer (Spring Actuator)
- **Rendimiento del Modelo**: Endpoint de métricas personalizado
- **Alertas**: Monitoreo basado en logs

## Estrategia de Pruebas

### Pruebas Unitarias
- Funciones de ingeniería de características
- Lógica de predicción de modelo
- Validación de configuración

### Pruebas de Integración
- Pipeline completo de entrenamiento
- Inferencia con modelos en caché
- Pruebas de endpoint API

### Pruebas de Modelo
- Validación de precisión en dataset de prueba
- Pruebas de detección de sesgo
- Benchmarking de rendimiento

### Aceptación de Usuario
- Framework de pruebas A/B
- Recopilación de retroalimentación de usuario
- Calibración de confianza

## Riesgos y Mitigación

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Datos de entrenamiento insuficientes | Alto | Comenzar con basado en reglas + adopción gradual de ML |
| Sobreajuste del modelo | Medio | Validación cruzada, regularización |
| Alto uso de recursos | Medio | Optimizar modelos, usar cuantización |
| Desconfianza del usuario | Alto | Explicaciones transparentes, opt-out fácil |
| Errores de predicción | Alto | Umbrales de confianza, revisión humana |
| Preocupaciones de privacidad | Alto | Procesamiento local, anonimización |

## Métricas de Éxito

### Métricas de Negocio
- **Eficiencia de Almacenamiento**: Mejora del 25-40%
- **Ahorro de Tiempo**: Reducción del 80% en decisiones manuales
- **Adopción de Usuario**: > 50% mantienen IA habilitada
- **Satisfacción del Usuario**: > 4.0/5.0 de calificación

### Métricas Técnicas
- **Precisión del Modelo**: > 85% de precisión
- **Tiempo de Respuesta**: < 100ms por predicción
- **Tiempo de Actividad**: > 99.5% disponibilidad
- **Uso de Recursos**: < 512MB de memoria

### Experiencia de Usuario
- **Explicabilidad**: > 90% entienden las recomendaciones
- **Confianza**: > 80% siguen sugerencias de IA
- **Tasa de Anulación**: < 15% rechazan recomendaciones
- **Retroalimentación**: > 70% proporcionan retroalimentación

## Mejoras Futuras

### Interfaz de Lenguaje Natural
- Comprensión de consultas: "Mostrar películas sin ver por 6 meses"
- Limpieza conversacional: "¿Qué debo eliminar para 50GB?"
- Integración de comandos de voz

### Visión por Computadora
- Puntuación de calidad de poster/artwork
- Detección de escenas para tipo de contenido
- Detección de duplicados mediante similitud visual
- Evaluación de calidad (contenido escalado)

### Análisis Avanzado
- Monitoreo de salud del disco
- Sugerencias de optimización de rendimiento
- Predicciones de planificación de recursos
- Pronóstico de salud del servicio

## Referencias

- [Kotlin for Machine Learning](https://github.com/Kotlin/kotlindl)
- [Deep Java Library (DJL)](https://djl.ai/)
- [ONNX Runtime](https://onnxruntime.ai/)
- [Spring AI](https://spring.io/projects/spring-ai)
- [Principios de IA Explicable](https://www.oreilly.com/library/view/interpretable-machine-learning/9781492033158/)

## Contribuir

Esta característica está en fase de planificación. Se aceptan contribuciones en:
- Refinamiento de arquitectura
- Selección de modelos ML
- Ideas de ingeniería de características
- Consideraciones de privacidad/ética
- Optimización de rendimiento

Ver [CONTRIBUTING.md](../CONTRIBUTING.md) para pautas.
