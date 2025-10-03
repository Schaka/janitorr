# AI/ML Intelligence Engine Architecture

## Overview

The AI/ML Intelligence Engine is an advanced feature for Janitorr that uses machine learning to optimize media cleanup decisions based on viewing patterns, user preferences, and predictive analytics.

**Status**: 🚧 **Future Development** - Architecture Documentation  
**Priority**: 🟢 Low (Advanced Future Feature)

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    AI/ML Intelligence Engine                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ Data Collection│  │   Feature    │  │  Model Training  │   │
│  │    Pipeline    │─▶│ Engineering  │─▶│    Pipeline      │   │
│  └────────────────┘  └──────────────┘  └──────────────────┘   │
│          │                                       │              │
│          ▼                                       ▼              │
│  ┌────────────────┐                    ┌──────────────────┐   │
│  │   Training     │                    │  Trained Models  │   │
│  │   Data Store   │                    │   Repository     │   │
│  └────────────────┘                    └──────────────────┘   │
│                                                 │              │
│                                                 ▼              │
│  ┌────────────────────────────────────────────────────────┐   │
│  │           Real-time Inference Engine                   │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐  │   │
│  │  │Content Scorer│  │  Predictor   │  │  Explainer  │  │   │
│  │  └──────────────┘  └──────────────┘  └─────────────┘  │   │
│  └────────────────────────────────────────────────────────┘   │
│                              │                                 │
└──────────────────────────────┼─────────────────────────────────┘
                               ▼
                    ┌──────────────────────┐
                    │  Cleanup Decisions   │
                    │   & Recommendations  │
                    └──────────────────────┘
```

## Core Components

### 1. Data Collection Pipeline

Aggregates data from multiple sources:

- **Viewing History**: From Tautulli, Jellystat, or Streamystats
- **Media Metadata**: From Jellyfin/Emby and *arr services
- **User Decisions**: Manual cleanup decisions and overrides
- **External APIs**: Streaming availability, social trends, ratings
- **System Metrics**: Storage usage, performance data

**Data Model**:
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

### 2. Feature Engineering

Transforms raw data into ML-ready features:

**Feature Categories**:
- **Temporal Features**: Watch frequency, recency, seasonal patterns
- **Content Features**: Genre, rating, quality, size
- **User Behavior**: Viewing patterns, genre preferences, binge habits
- **Storage Metrics**: Disk pressure, growth rate
- **External Signals**: Streaming availability, trending status

**Feature Vector**:
```kotlin
data class MediaFeatures(
    val watchFrequency: Double,           // Views per week
    val daysSinceLastWatch: Int,          // Recency
    val avgCompletionRate: Double,        // How much users watch
    val genreScore: Double,               // User genre preference
    val trendingScore: Double,            // External popularity
    val storageImpact: Double,            // Size in GB
    val seasonalRelevance: Double,        // Seasonal pattern match
    val similarContentBehavior: Double    // How similar content is treated
)
```

### 3. ML Models

#### Content Scoring Model
Predicts likelihood that media should be kept:

```kotlin
interface ContentScoringModel {
    /**
     * Predict probability that media should be kept (0.0 to 1.0)
     */
    fun predictKeepProbability(features: MediaFeatures): Double
    
    /**
     * Generate human-readable explanation of the score
     */
    fun explainPrediction(features: MediaFeatures, score: Double): String
    
    /**
     * Get feature importance for this prediction
     */
    fun getFeatureImportance(features: MediaFeatures): Map<String, Double>
}
```

**Model Types**:
- **Gradient Boosting** (primary): XGBoost or LightGBM
- **Random Forest** (fallback): Robust to overfitting
- **Neural Network** (experimental): For complex patterns

#### Pattern Recognition Models

- **Temporal Pattern Detector**: Identifies viewing schedules
- **Genre Clustering**: Groups content by user preferences  
- **Binge Detection**: Recognizes active series watching
- **Anomaly Detection**: Finds unusual viewing patterns

#### Predictive Models

- **Storage Forecasting**: Predicts disk usage trends
- **Optimal Timing**: Best time for cleanup operations
- **Library Growth**: Future space requirements
- **Content Relevance**: Long-term keep probability

### 4. Training Pipeline

```kotlin
class MLTrainingPipeline(
    val dataCollector: DataCollector,
    val featureEngineer: FeatureEngineer,
    val modelTrainer: ModelTrainer,
    val validator: ModelValidator
) {
    /**
     * Full training pipeline execution
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

### 5. Inference Engine

Real-time prediction service:

```kotlin
interface InferenceEngine {
    /**
     * Get AI recommendation for a single media item
     */
    fun getRecommendation(mediaId: String): AIRecommendation
    
    /**
     * Batch process entire library (for overnight jobs)
     */
    suspend fun batchScore(libraryItems: List<LibraryItem>): Map<String, AIRecommendation>
    
    /**
     * Get model confidence level
     */
    fun getConfidence(mediaId: String): ConfidenceLevel
}

data class AIRecommendation(
    val action: RecommendedAction,      // KEEP, DELETE, ARCHIVE, REVIEW
    val confidence: Double,              // 0.0 to 1.0
    val reasoning: String,               // Human-readable explanation
    val alternativeScenarios: List<Scenario>
)
```

## Performance Requirements

### Latency
- **Single Prediction**: < 100ms
- **Batch Processing**: < 1 minute per 1000 items
- **Model Loading**: < 5 seconds on startup

### Accuracy Targets
- **Precision**: > 85% (recommendations followed)
- **Recall**: > 75% (important content saved)
- **F1-Score**: > 80% (balanced performance)
- **User Satisfaction**: > 4.0/5.0

### Resource Constraints
- **Memory**: < 512MB for model + inference
- **CPU**: < 10% average utilization
- **Storage**: < 100MB for model files

## Privacy & Ethics

### Data Protection
- **Anonymization**: All user IDs hashed before ML processing
- **Local Processing**: No data sent to external ML services
- **Data Retention**: Training data purged after 90 days
- **Opt-out**: Easy disable of all AI features

### Explainability
- **Transparent Decisions**: Always show reasoning
- **Feature Importance**: Display which factors influenced decision
- **Override Capability**: Users can always reject AI recommendations
- **Audit Trail**: Log all AI decisions for review

### Bias Prevention
- **Fairness Monitoring**: Check for demographic biases
- **Diverse Training**: Include varied user patterns
- **Regular Retraining**: Adapt to changing preferences
- **Human Oversight**: Flag low-confidence decisions for review

## Integration Points

### Existing Services
- **StatsService**: Viewing history data source
- **ServarrService**: Media metadata and management
- **MediaServerService**: Jellyfin/Emby integration
- **CleanupSchedule**: Inject AI recommendations

### New Configuration
```yaml
ai:
  enabled: false                # AI features disabled by default
  model-path: /config/models    # Where to store trained models
  training:
    enabled: false              # Manual training only initially
    schedule: "0 0 3 * * ?"     # 3 AM daily if enabled
    min-data-points: 1000       # Minimum data before training
  inference:
    cache-ttl: 3600             # Cache predictions for 1 hour
    batch-size: 100             # Items per batch
    confidence-threshold: 0.7   # Minimum confidence to act
  features:
    external-apis: false        # Disable external data by default
    user-feedback: true         # Learn from user corrections
```

## Phased Implementation Plan

### Phase 1: Foundation (Months 1-2)
- [ ] Data collection infrastructure
- [ ] Feature engineering pipeline
- [ ] Basic configuration and properties
- [ ] Data storage and schemas

### Phase 2: Core ML (Months 3-4)
- [ ] Content scoring model
- [ ] Training pipeline
- [ ] Model validation and testing
- [ ] Inference engine basics

### Phase 3: Intelligence (Months 5-6)
- [ ] Pattern recognition models
- [ ] Predictive analytics
- [ ] Explanation engine
- [ ] A/B testing framework

### Phase 4: UI & UX (Months 7-8)
- [ ] AI recommendations dashboard
- [ ] Interactive feedback system
- [ ] Visualization of decisions
- [ ] User preference tuning

### Phase 5: Advanced Features (Months 9-12)
- [ ] Natural language interface
- [ ] Computer vision integration
- [ ] External API integrations
- [ ] Continuous learning system

## Technology Stack

### ML Libraries (Options)
- **Primary**: [Kotlin for ML](https://github.com/Kotlin/kotlindl) or [DJL](https://djl.ai/)
- **Alternative**: Python microservice with gRPC (if Kotlin ML insufficient)
- **Model Format**: ONNX for portability

### Storage
- **Training Data**: SQLite or H2 embedded database
- **Models**: Filesystem with versioning
- **Cache**: Caffeine (already in use)

### Monitoring
- **Metrics**: Micrometer (Spring Actuator)
- **Model Performance**: Custom metrics endpoint
- **Alerting**: Log-based monitoring

## Testing Strategy

### Unit Tests
- Feature engineering functions
- Model prediction logic
- Configuration validation

### Integration Tests
- Full training pipeline
- Inference with cached models
- API endpoint testing

### Model Tests
- Accuracy validation on test dataset
- Bias detection tests
- Performance benchmarking

### User Acceptance
- A/B testing framework
- User feedback collection
- Confidence calibration

## Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Insufficient training data | High | Start with rule-based + gradual ML adoption |
| Model overfitting | Medium | Cross-validation, regularization |
| High resource usage | Medium | Optimize models, use quantization |
| User distrust | High | Transparent explanations, easy opt-out |
| Prediction errors | High | Confidence thresholds, human review |
| Privacy concerns | High | Local processing, anonymization |

## Success Metrics

### Business Metrics
- **Storage Efficiency**: 25-40% improvement
- **Time Savings**: 80% reduction in manual decisions
- **User Adoption**: > 50% keep AI enabled
- **User Satisfaction**: > 4.0/5.0 rating

### Technical Metrics
- **Model Accuracy**: > 85% precision
- **Response Time**: < 100ms per prediction
- **Uptime**: > 99.5% availability
- **Resource Usage**: < 512MB memory

### User Experience
- **Explainability**: > 90% understand recommendations
- **Trust**: > 80% follow AI suggestions
- **Override Rate**: < 15% reject recommendations
- **Feedback**: > 70% provide feedback

## Future Enhancements

The following enhancements are under consideration and may be implemented in future releases:

### Natural Language Interface (Under Consideration)
- Query understanding: "Show movies unwatched for 6 months" (may be added)
- Conversational cleanup: "What should I delete for 50GB?" (under consideration)
- Voice command integration (may be implemented)

### Computer Vision (Under Consideration)
- Poster/artwork quality scoring (may be added)
- Scene detection for content type (under consideration)
- Duplicate detection via visual similarity (may be implemented)
- Quality assessment (upscaled content) (under consideration)

### Advanced Analytics (Under Consideration)
- Disk health monitoring (may be added)
- Performance optimization suggestions (under consideration)
- Resource planning predictions (may be implemented)
- Service health forecasting (under consideration)

## References

- [Kotlin for Machine Learning](https://github.com/Kotlin/kotlindl)
- [Deep Java Library (DJL)](https://djl.ai/)
- [ONNX Runtime](https://onnxruntime.ai/)
- [Spring AI](https://spring.io/projects/spring-ai)
- [Explainable AI Principles](https://www.oreilly.com/library/view/interpretable-machine-learning/9781492033158/)

## Contributing

This feature is in the planning phase. Contributions welcome in:
- Architecture refinement
- ML model selection
- Feature engineering ideas
- Privacy/ethics considerations
- Performance optimization

See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.
