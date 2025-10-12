# 📱 Mobile App Companion - Roadmap y Guía de Implementación

## 🎯 Estado Actual

**Estado**: 📋 PLANIFICACIÓN - Requiere Proyecto Separado  
**Prioridad**: 🟡 MEDIA  
**Tipo**: Nueva Funcionalidad - Proyecto Independiente  

## 📋 Resumen Ejecutivo

La propuesta de una aplicación móvil companion para Janitorr (iOS/Android) es una **iniciativa estratégica importante** que requiere un enfoque de proyecto separado, no una modificación al backend existente de Spring Boot.

### ¿Por Qué Un Proyecto Separado?

1. **Stack Tecnológico Diferente**: Flutter/Dart vs Kotlin/Spring Boot
2. **Repositorio Independiente**: Ciclo de desarrollo y release separado
3. **Equipos Diferentes**: Skills mobile vs backend
4. **App Stores**: Proceso de distribución completamente diferente
5. **Testing**: Infraestructura de testing móvil específica

## 🏗️ Arquitectura Propuesta

### Backend (Janitorr Existente)
El backend actual ya tiene las capacidades necesarias:

```
✅ Management UI (web-based)
✅ REST API endpoints (/api/management/*)
✅ Status monitoring
✅ Manual cleanup triggers
✅ Configuration management
```

**Lo que FALTA para mobile:**
```
❌ Push notification service (FCM/APNS integration)
❌ Mobile-specific authentication (OAuth2/JWT)
❌ WebSocket support para real-time updates
❌ Mobile API versioning
❌ Rate limiting para mobile clients
```

### Mobile App (Nuevo Proyecto)
```
📱 janitorr-mobile/
├── android/               # Android native code
├── ios/                   # iOS native code
├── lib/                   # Flutter/Dart code
│   ├── api/              # API client
│   ├── models/           # Data models
│   ├── screens/          # UI screens
│   ├── widgets/          # Reusable components
│   └── services/         # Business logic
├── test/                 # Unit tests
└── integration_test/     # Integration tests
```

## 🔧 Backend Modifications Necesarias

### 1. Push Notification Infrastructure

**Nuevo servicio en backend:**
```kotlin
// src/main/kotlin/com/github/schaka/janitorr/notifications/

@Service
class PushNotificationService(
    private val fcmService: FirebaseCloudMessagingService,
    private val apnsService: ApplePushNotificationService
) {
    fun sendCleanupComplete(deviceToken: String, stats: CleanupStats)
    fun sendDiskSpaceAlert(deviceToken: String, percentage: Int)
    fun sendServiceOffline(deviceToken: String, serviceName: String)
}

@RestController
@RequestMapping("/api/mobile/notifications")
class MobileNotificationController(
    private val notificationService: PushNotificationService
) {
    @PostMapping("/register")
    fun registerDevice(@RequestBody deviceInfo: DeviceRegistration)
    
    @DeleteMapping("/unregister")
    fun unregisterDevice(@RequestParam deviceToken: String)
    
    @PutMapping("/preferences")
    fun updatePreferences(@RequestBody prefs: NotificationPreferences)
}
```

### 2. Mobile Authentication API

**Nuevo endpoint de autenticación:**
```kotlin
// src/main/kotlin/com/github/schaka/janitorr/api/mobile/

@RestController
@RequestMapping("/api/mobile/auth")
class MobileAuthController {
    
    @PostMapping("/login")
    fun login(@RequestBody credentials: LoginRequest): TokenResponse
    
    @PostMapping("/refresh")
    fun refreshToken(@RequestHeader("Authorization") refreshToken: String): TokenResponse
    
    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") accessToken: String)
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
```

### 3. Real-Time Updates (WebSocket)

**Agregar WebSocket support:**
```kotlin
// src/main/kotlin/com/github/schaka/janitorr/websocket/

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic")
        registry.setApplicationDestinationPrefixes("/app")
    }
    
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/mobile").setAllowedOrigins("*")
    }
}

@Controller
class LiveStatsController {
    
    @MessageMapping("/stats/subscribe")
    @SendTo("/topic/stats")
    fun subscribeToStats(): SystemStats
}
```

### 4. Mobile-Optimized API Endpoints

**Nuevo controller para mobile:**
```kotlin
// src/main/kotlin/com/github/schaka/janitorr/api/mobile/

@RestController
@RequestMapping("/api/mobile/v1")
class MobileApiController(
    private val cleanupService: CleanupService,
    private val statusService: StatusService
) {
    
    @GetMapping("/dashboard")
    fun getDashboard(): MobileDashboard {
        return MobileDashboard(
            stats = getCompactStats(),
            recentActivity = getRecentActivity(limit = 10),
            diskUsage = getDiskUsage(),
            serviceStatus = getServiceStatus()
        )
    }
    
    @GetMapping("/stats/compact")
    fun getCompactStats(): CompactStats
    
    @PostMapping("/cleanup/quick")
    fun triggerQuickCleanup(@RequestParam type: CleanupType): CleanupResult
    
    @GetMapping("/activity/recent")
    fun getRecentActivity(@RequestParam limit: Int = 20): List<ActivityItem>
}
```

## 📱 Mobile App Structure (Flutter)

### API Client
```dart
// lib/api/janitorr_api_service.dart

class JanitorrApiService {
  final String baseUrl;
  final Dio _dio;
  
  Future<DashboardData> getDashboard() async {
    final response = await _dio.get('/api/mobile/v1/dashboard');
    return DashboardData.fromJson(response.data);
  }
  
  Future<void> triggerCleanup(CleanupType type) async {
    await _dio.post('/api/mobile/v1/cleanup/quick', 
      queryParameters: {'type': type.name});
  }
  
  Stream<SystemStats> getRealTimeStats() {
    // WebSocket implementation
    return _statsChannel.stream;
  }
}
```

### State Management (BLoC)
```dart
// lib/blocs/dashboard_bloc.dart

class DashboardBloc extends Bloc<DashboardEvent, DashboardState> {
  final JanitorrApiService apiService;
  
  DashboardBloc(this.apiService) : super(DashboardInitial()) {
    on<LoadDashboard>(_onLoadDashboard);
    on<RefreshDashboard>(_onRefreshDashboard);
  }
  
  Future<void> _onLoadDashboard(
    LoadDashboard event,
    Emitter<DashboardState> emit,
  ) async {
    try {
      emit(DashboardLoading());
      final data = await apiService.getDashboard();
      emit(DashboardLoaded(data));
    } catch (e) {
      emit(DashboardError(e.toString()));
    }
  }
}
```

### Push Notifications
```dart
// lib/services/push_notification_service.dart

class PushNotificationService {
  final FirebaseMessaging _fcm = FirebaseMessaging.instance;
  final JanitorrApiService _api;
  
  Future<void> initialize() async {
    // Request permission
    NotificationSettings settings = await _fcm.requestPermission();
    
    if (settings.authorizationStatus == AuthorizationStatus.authorized) {
      // Get FCM token
      String? token = await _fcm.getToken();
      
      // Register with backend
      await _api.registerDevice(token!);
      
      // Handle foreground notifications
      FirebaseMessaging.onMessage.listen(_handleForegroundMessage);
      
      // Handle background notifications
      FirebaseMessaging.onBackgroundMessage(_handleBackgroundMessage);
    }
  }
  
  void _handleForegroundMessage(RemoteMessage message) {
    // Show local notification
    _showLocalNotification(message);
  }
}
```

## 📦 Dependencias Necesarias

### Backend (build.gradle.kts)
```kotlin
dependencies {
    // WebSocket support
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    
    // Firebase Admin SDK (para FCM)
    implementation("com.google.firebase:firebase-admin:9.2.0")
    
    // JWT para auth móvil
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Rate limiting
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:8.7.0")
}
```

### Mobile (pubspec.yaml)
```yaml
dependencies:
  flutter:
    sdk: flutter
  
  # State Management
  flutter_bloc: ^8.1.3
  
  # HTTP Client
  dio: ^5.3.3
  
  # Push Notifications
  firebase_messaging: ^14.7.6
  firebase_core: ^2.24.2
  flutter_local_notifications: ^16.1.0
  
  # Local Storage
  shared_preferences: ^2.2.2
  hive: ^2.2.3
  hive_flutter: ^1.1.0
  
  # UI Components
  flutter_slidable: ^3.0.0
  pull_to_refresh: ^2.0.0
  
  # Platform Channels
  flutter_platform_widgets: ^6.0.2
  
  # WebSocket
  web_socket_channel: ^2.4.0
```

## 🚀 Plan de Implementación Faseado

### Fase 1: Backend Preparation (2-3 semanas)
- [ ] Implementar authentication API (JWT-based)
- [ ] Crear endpoints mobile-optimized (/api/mobile/v1/*)
- [ ] Agregar WebSocket support para real-time updates
- [ ] Setup Firebase Admin SDK para push notifications
- [ ] Implementar device registration/management
- [ ] Rate limiting para mobile clients
- [ ] Testing de nuevos endpoints

### Fase 2: Mobile App MVP (4-6 semanas)
- [ ] Setup proyecto Flutter
- [ ] Implementar API client
- [ ] Crear UI básico (dashboard, stats, cleanup buttons)
- [ ] Implementar authentication flow
- [ ] Testing en iOS Simulator y Android Emulator
- [ ] Internal testing (TestFlight/Internal Track)

### Fase 3: Push Notifications (2-3 semanas)
- [ ] Integrar Firebase Cloud Messaging
- [ ] Implementar notification handling
- [ ] Configurar notification categories
- [ ] Testing de delivery de notificaciones
- [ ] Implementar quiet hours y preferences

### Fase 4: Advanced Features (3-4 semanas)
- [ ] Offline mode con caching
- [ ] Multi-server support
- [ ] Widgets (iOS/Android)
- [ ] Quick actions
- [ ] Biometric authentication
- [ ] Dark mode

### Fase 5: Beta Testing & Launch (2-3 semanas)
- [ ] Beta testing público (TestFlight/Open Testing)
- [ ] Bug fixes basados en feedback
- [ ] App Store submission
- [ ] Google Play submission
- [ ] Marketing materials
- [ ] Documentation

**Timeline Total Estimado**: 3-4 meses

## 💰 Consideraciones de Monetización

### Modelo Freemium Propuesto
```
Tier Gratuito:
✅ Dashboard básico
✅ Manual cleanup triggers
✅ Notificaciones básicas (max 10/día)
✅ Un servidor
❌ Advanced analytics
❌ Multi-server
❌ Custom themes
❌ Widgets

Tier Premium ($2.99/mes o $24.99/año):
✅ Todo del tier gratuito
✅ Notificaciones ilimitadas
✅ Advanced analytics
✅ Multi-server (hasta 5)
✅ Custom notification rules
✅ Premium themes
✅ Home screen widgets
✅ Priority support
```

### Implementación de In-App Purchases
```dart
// lib/services/subscription_service.dart

class SubscriptionService {
  final InAppPurchase _iap = InAppPurchase.instance;
  
  Future<bool> isPremium() async {
    // Check subscription status
  }
  
  Future<void> purchasePremium() async {
    // Handle purchase flow
  }
  
  Future<void> restorePurchases() async {
    // Restore previous purchases
  }
}
```

## 📊 Métricas de Éxito

### KPIs a Trackear
- **Adoption Rate**: % de usuarios web que instalan app móvil
- **DAU/MAU**: Daily/Monthly Active Users
- **Session Length**: Tiempo promedio por sesión
- **Retention**: Day-1, Day-7, Day-30 retention rates
- **Notification CTR**: Click-through rate de notificaciones
- **Conversion Rate**: Free to Premium conversion
- **App Store Rating**: Rating promedio en stores
- **Crash-Free Rate**: % de sesiones sin crashes

### Targets Iniciales (6 meses post-launch)
- 📱 Adoption: 40% de usuarios web instalan móvil
- 📊 DAU: 30% de usuarios instalados
- ⏱️ Session Length: 2+ minutos promedio
- 🔄 Day-7 Retention: 50%+
- 🌟 App Store Rating: 4.0+ estrellas
- 💥 Crash-Free: 99%+

## 🔐 Consideraciones de Seguridad

### Backend Security
1. **JWT Authentication**: Access tokens con expiración corta (15 min)
2. **Refresh Tokens**: Tokens de larga duración almacenados securely
3. **Rate Limiting**: Prevenir abuse de API
4. **Certificate Pinning**: Prevenir man-in-the-middle attacks
5. **API Versioning**: Deprecation strategy para cambios breaking

### Mobile Security
1. **Secure Storage**: Keychain (iOS) / Keystore (Android) para tokens
2. **Biometric Auth**: TouchID/FaceID/Fingerprint para unlock
3. **Auto-lock**: Lock app después de inactividad
4. **No Credentials in Logs**: Never log sensitive data
5. **SSL Pinning**: Verify server certificate

## 📚 Recursos Necesarios

### Equipo Mínimo
- **1 Backend Developer**: Modificaciones a Janitorr backend (Kotlin/Spring Boot)
- **1 Mobile Developer**: Flutter development (iOS/Android)
- **1 Designer**: UI/UX design para mobile
- **1 QA**: Testing en múltiples dispositivos
- **0.5 DevOps**: CI/CD setup, app store deployment

### Infraestructura
- **Firebase Project**: Para push notifications (Free tier OK para start)
- **Apple Developer Account**: $99/año
- **Google Play Developer Account**: $25 one-time
- **CI/CD**: GitHub Actions (ya existente)
- **Beta Testing**: TestFlight (gratis), Google Play Internal Testing (gratis)

### Costos Estimados (Primer Año)
```
Development (3-4 meses @ contractor rates):
  - Backend Developer: $15,000 - $20,000
  - Mobile Developer: $20,000 - $30,000
  - UI/UX Designer: $5,000 - $8,000
  - QA: $5,000 - $8,000
  - DevOps: $3,000 - $5,000
  TOTAL DEV: $48,000 - $71,000

Operational (anual):
  - Apple Developer: $99
  - Google Play: $25
  - Firebase (est.): $0 - $300
  - Server costs (push notifications): $0 - $500
  TOTAL OPS: $124 - $924

TOTAL YEAR 1: $48,124 - $71,924
```

## 🎯 Alternativas Más Económicas

### Opción 1: PWA (Progressive Web App)
**Ventajas:**
- ✅ Un solo codebase (el UI existente)
- ✅ No app stores
- ✅ Instalable en iOS/Android
- ✅ Mucho más económico
- ✅ Push notifications (limitado en iOS)

**Desventajas:**
- ❌ No widgets nativos
- ❌ No biometric auth nativo
- ❌ Push notifications limitados en iOS
- ❌ No acceso a todas las APIs del OS

**Implementación:**
```javascript
// Agregar a index.html existente
// service-worker.js
self.addEventListener('push', function(event) {
  const data = event.data.json();
  self.registration.showNotification(data.title, {
    body: data.body,
    icon: '/icon-192.png',
    badge: '/badge-72.png'
  });
});

// manifest.json
{
  "name": "Janitorr Mobile",
  "short_name": "Janitorr",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#1a1a1a",
  "theme_color": "#4a90e2",
  "icons": [...]
}
```

**Costo Estimado PWA**: $5,000 - $10,000 (1-2 semanas)

### Opción 2: React Native
Similar a Flutter pero con más recursos web disponibles.

**Pros:**
- JavaScript (más developers disponibles)
- Hot reload
- Large community

**Cons:**
- Performance generalmente peor que Flutter
- Más configuración nativa requerida

## 🔄 Integración con Proyecto Actual

### Repository Structure Propuesta
```
janitorr/                      (backend - repo actual)
└── src/main/kotlin/...

janitorr-mobile/               (nuevo repo)
├── android/
├── ios/
└── lib/

janitorr-docs/                 (opcional - docs compartidas)
├── api-specs/
└── user-guides/
```

### CI/CD Integration
```yaml
# .github/workflows/mobile-release.yml (en janitorr-mobile repo)
name: Mobile Release

on:
  push:
    tags:
      - 'mobile-v*'

jobs:
  build-ios:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - uses: subosito/flutter-action@v2
      - run: flutter build ios --release
      - name: Upload to TestFlight
        # ...

  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: subosito/flutter-action@v2
      - run: flutter build appbundle --release
      - name: Upload to Play Store
        # ...
```

## 📝 Next Steps Recomendados

### Paso 1: Validación (2-4 semanas)
1. **User Research**: Encuesta a usuarios actuales sobre interés en app móvil
2. **Competitor Analysis**: Analizar apps similares (Overseerr, Maintainerr)
3. **Feature Prioritization**: Definir MVP mínimo viable
4. **Technical Spike**: Prototype simple en Flutter

### Paso 2: Decision Point
Decidir entre:
- **Opción A**: PWA (quick win, bajo costo)
- **Opción B**: Native app (mejor UX, mayor inversión)
- **Opción C**: Postpone (focus en mejorar web UI actual)

### Paso 3: Preparación Backend (si se aprueba)
1. Implementar authentication API
2. Crear mobile endpoints
3. Setup push notification infrastructure
4. Documentation de API

### Paso 4: Mobile Development
1. Setup proyecto Flutter
2. MVP implementation
3. Internal testing
4. Beta release

## 📖 Referencias y Recursos

### Flutter Resources
- [Flutter Documentation](https://docs.flutter.dev/)
- [Flutter BLoC Pattern](https://bloclibrary.dev/)
- [Firebase for Flutter](https://firebase.google.com/docs/flutter/setup)

### Push Notifications
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [APNs Documentation](https://developer.apple.com/documentation/usernotifications)

### App Store Guidelines
- [Apple App Store Review Guidelines](https://developer.apple.com/app-store/review/guidelines/)
- [Google Play Store Policies](https://play.google.com/about/developer-content-policy/)

## ✅ Conclusión

El desarrollo de una aplicación móvil companion para Janitorr es **factible y estratégicamente valioso**, pero requiere:

1. ✅ **Proyecto Separado**: Nuevo repositorio para mobile app
2. ✅ **Modificaciones Backend**: Agregar APIs mobile-specific
3. ✅ **Recursos Significativos**: 3-4 meses de desarrollo
4. ✅ **Inversión**: $50k-$70k año 1
5. ✅ **Validación Previa**: User research antes de committed resources

**Recomendación Inmediata**: Comenzar con **PWA approach** para validar demanda con inversión mínima, luego escalar a native app si se justifica.

---

**Documento Creado**: 2024  
**Autor**: GitHub Copilot  
**Status**: Planificación - Pendiente de Aprobación  
**Última Actualización**: Octubre 2024
