# AI/ML Intelligence Engine Package

This package contains the foundational structure for the AI/ML Intelligence Engine feature.

**Status**: 🚧 Planning Phase - Not Yet Implemented  
**Priority**: Low (Advanced Future Feature)

## Package Structure

```
ai/
├── config/          # Configuration classes
│   ├── AIConfiguration.kt     # Spring configuration beans
│   └── AIProperties.kt        # Configuration properties
├── data/            # Data models
│   ├── MediaFeatures.kt       # Feature vectors for ML
│   └── ViewingData.kt         # Viewing sessions and decisions
├── inference/       # Inference engine
│   └── InferenceEngine.kt     # Prediction service interface
└── ml/              # Machine learning models
    └── ContentScoringModel.kt # ML model interface
```

## Current Implementation

The current implementation provides:

### 1. Configuration (`config/`)
- **AIProperties**: Configuration properties for AI features
- **AIConfiguration**: Spring bean configuration (currently provides disabled engine)

### 2. Data Models (`data/`)
- **ViewingSession**: Represents a viewing session for training
- **CleanupDecision**: User decisions for supervised learning
- **MediaFeatures**: Feature vector for ML model input
- **Decision**: Enum for cleanup actions (KEEP, DELETE, ARCHIVE, REVIEW)

### 3. Inference (`inference/`)
- **InferenceEngine**: Interface for making AI predictions
- **DisabledInferenceEngine**: No-op implementation when AI is disabled
- **AIRecommendation**: Recommendation output with confidence and explanation
- **ConfidenceLevel**: Categorization of prediction confidence

### 4. ML Models (`ml/`)
- **ContentScoringModel**: Interface for ML model implementations
- **ModelMetadata**: Information about trained models
- **TrainingResult**: Output from training pipeline

## Usage

### Current State (AI Disabled)

By default, AI features are disabled:

```yaml
ai:
  enabled: false  # Default state
```

Attempting to use the inference engine will throw `UnsupportedOperationException`:

```kotlin
@Autowired
lateinit var inferenceEngine: InferenceEngine  // Will inject DisabledInferenceEngine

fun example() {
    // This will throw UnsupportedOperationException
    val recommendation = inferenceEngine.getRecommendation("media-id")
}
```

### Future State (When Implemented)

When AI features are enabled:

```yaml
ai:
  enabled: true
  model-path: /config/models
  # ... other configuration
```

The inference engine will provide real ML-based recommendations:

```kotlin
@Autowired
lateinit var inferenceEngine: InferenceEngine  // Will inject MLInferenceEngine

fun example() {
    // Get recommendation for a single item
    val recommendation = inferenceEngine.getRecommendation("media-id")
    println("Action: ${recommendation.action}")
    println("Confidence: ${recommendation.confidence}")
    println("Reasoning: ${recommendation.reasoning}")
    
    // Batch score multiple items
    val results = inferenceEngine.batchScore(listOf("id1", "id2", "id3"))
}
```

## Testing

Tests are provided for all components:

- **AIPropertiesTest**: Configuration validation
- **MediaFeaturesTest**: Feature serialization/deserialization
- **DisabledInferenceEngineTest**: Ensures proper rejection when disabled

Run tests with:
```bash
./gradlew test --tests "*ai*"
```

## Integration Points

When implemented, the AI engine will integrate with:

1. **StatsService**: Source of viewing history data
2. **ServarrService**: Media metadata for feature engineering
3. **MediaServerService**: Media server data
4. **CleanupSchedule**: Inject AI recommendations into cleanup logic

## Architecture Documentation

For detailed information about the planned architecture:
- [AI/ML Engine Architecture (English)](../../../../docs/AI_ML_ENGINE_ARCHITECTURE.md)
- [Arquitectura del Motor IA/ML (Spanish)](../../../../docs/ARQUITECTURA_MOTOR_IA_ML.md)

## Future Implementation Phases

The following implementation phases are under consideration and may be added in future releases:

### Phase 1: Data Collection (2-3 months) - Under Consideration
- [ ] Implement data collection pipeline (may be added)
- [ ] Create training data storage (SQLite/H2) (under consideration)
- [ ] Build feature engineering pipeline (may be implemented)
- [ ] Integrate with existing stats services (under consideration)

### Phase 2: Core ML Models (3-4 months) - Under Consideration
- [ ] Implement ContentScoringModel with Kotlin ML or DJL (may be added)
- [ ] Build training pipeline (under consideration)
- [ ] Add model validation and testing (may be implemented)
- [ ] Create model persistence layer (under consideration)

### Phase 3: Intelligence Features (2-3 months) - Under Consideration
- [ ] Pattern recognition models (may be added)
- [ ] Predictive analytics (under consideration)
- [ ] Explanation engine (may be implemented)
- [ ] A/B testing framework (under consideration)

### Phase 4: UI Integration (2 months) - Under Consideration
- [ ] AI recommendations dashboard (may be added)
- [ ] Interactive feedback system (under consideration)
- [ ] Visualization of decisions (may be implemented)
- [ ] User preference tuning (under consideration)

### Phase 5: Advanced Features (3-4 months) - Under Consideration
- [ ] Natural language interface (may be added)
- [ ] Computer vision integration (under consideration)
- [ ] External API integrations (optional) (may be implemented)
- [ ] Continuous learning system (under consideration)

## Contributing

To contribute to AI/ML development:

1. Review the architecture documentation
2. Discuss proposed changes in GitHub Issues
3. Follow existing code patterns and test coverage
4. Ensure privacy and ethical considerations are addressed

## Privacy & Ethics

The AI engine is designed with these principles:

- **Local Processing**: All ML runs locally, no external data sharing
- **Anonymization**: User IDs hashed before processing
- **Transparency**: All decisions include explanations
- **User Control**: Easy opt-out and override capability
- **Data Retention**: Training data purged after 90 days

## Technology Choices (TBD)

Potential ML libraries:
- [Kotlin for ML](https://github.com/Kotlin/kotlindl)
- [Deep Java Library (DJL)](https://djl.ai/)
- Alternative: Python microservice with gRPC

Model format:
- ONNX for portability and interoperability

## Questions?

- See the [FAQ](../../../../docs/wiki/en/FAQ.md#aiml-features)
- Review [Configuration Guide](../../../../docs/wiki/en/Configuration-Guide.md#aiml-intelligence-engine-future-feature)
- Start a [GitHub Discussion](https://github.com/carcheky/janitorr/discussions)

---

**Note**: This is a placeholder implementation. All AI features are disabled by default and require explicit opt-in when implemented.
