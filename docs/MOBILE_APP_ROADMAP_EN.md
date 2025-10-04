# 📱 Mobile App Companion - Roadmap and Implementation Guide

## 🎯 Current Status

**Status**: 📋 PLANNING - Requires Separate Project  
**Priority**: 🟡 MEDIUM  
**Type**: New Feature - Independent Project  

## 📋 Executive Summary

The proposal for a Janitorr mobile companion app (iOS/Android) is an **important strategic initiative** that requires a separate project approach, not a modification to the existing Spring Boot backend.

### Why a Separate Project?

1. **Different Tech Stack**: Flutter/Dart vs Kotlin/Spring Boot
2. **Independent Repository**: Separate development and release cycle
3. **Different Teams**: Mobile skills vs backend skills
4. **App Store Distribution**: Completely different distribution process
5. **Testing**: Mobile-specific testing infrastructure

## 🏗️ Proposed Architecture

### Backend (Existing Janitorr)
The current backend already has necessary capabilities:

```
✅ Management UI (web-based)
✅ REST API endpoints (/api/management/*)
✅ Status monitoring
✅ Manual cleanup triggers
✅ Configuration management
```

**What's MISSING for mobile:**
```
❌ Push notification service (FCM/APNS integration)
❌ Mobile-specific authentication (OAuth2/JWT)
❌ WebSocket support for real-time updates
❌ Mobile API versioning
❌ Rate limiting for mobile clients
```

### Mobile App (New Project)
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

## 🔧 Required Backend Modifications

### 1. Push Notification Infrastructure

**New backend service:**
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

**New authentication endpoint:**
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

**Add WebSocket support:**
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

**New mobile controller:**
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

## 📦 Required Dependencies

### Backend (build.gradle.kts)
```kotlin
dependencies {
    // WebSocket support
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    
    // Firebase Admin SDK (for FCM)
    implementation("com.google.firebase:firebase-admin:9.2.0")
    
    // JWT for mobile auth
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

## 🚀 Phased Implementation Plan

### Phase 1: Backend Preparation (2-3 weeks)
- [ ] Implement authentication API (JWT-based)
- [ ] Create mobile-optimized endpoints (/api/mobile/v1/*)
- [ ] Add WebSocket support for real-time updates
- [ ] Setup Firebase Admin SDK for push notifications
- [ ] Implement device registration/management
- [ ] Rate limiting for mobile clients
- [ ] Test new endpoints

### Phase 2: Mobile App MVP (4-6 weeks)
- [ ] Setup Flutter project
- [ ] Implement API client
- [ ] Create basic UI (dashboard, stats, cleanup buttons)
- [ ] Implement authentication flow
- [ ] Test on iOS Simulator and Android Emulator
- [ ] Internal testing (TestFlight/Internal Track)

### Phase 3: Push Notifications (2-3 weeks)
- [ ] Integrate Firebase Cloud Messaging
- [ ] Implement notification handling
- [ ] Configure notification categories
- [ ] Test notification delivery
- [ ] Implement quiet hours and preferences

### Phase 4: Advanced Features (3-4 weeks)
- [ ] Offline mode with caching
- [ ] Multi-server support
- [ ] Widgets (iOS/Android)
- [ ] Quick actions
- [ ] Biometric authentication
- [ ] Dark mode

### Phase 5: Beta Testing & Launch (2-3 weeks)
- [ ] Public beta testing (TestFlight/Open Testing)
- [ ] Bug fixes based on feedback
- [ ] App Store submission
- [ ] Google Play submission
- [ ] Marketing materials
- [ ] Documentation

**Total Estimated Timeline**: 3-4 months

## 💰 Monetization Considerations

### Proposed Freemium Model
```
Free Tier:
✅ Basic dashboard access
✅ Manual cleanup triggers
✅ Basic notifications (max 10/day)
✅ One server
❌ Advanced analytics
❌ Multi-server
❌ Custom themes
❌ Widgets

Premium Tier ($2.99/month or $24.99/year):
✅ All free tier features
✅ Unlimited notifications
✅ Advanced analytics
✅ Multi-server (up to 5)
✅ Custom notification rules
✅ Premium themes
✅ Home screen widgets
✅ Priority support
```

### In-App Purchase Implementation
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

## 📊 Success Metrics

### KPIs to Track
- **Adoption Rate**: % of web users who install mobile app
- **DAU/MAU**: Daily/Monthly Active Users
- **Session Length**: Average time per session
- **Retention**: Day-1, Day-7, Day-30 retention rates
- **Notification CTR**: Click-through rate of notifications
- **Conversion Rate**: Free to Premium conversion
- **App Store Rating**: Average rating in stores
- **Crash-Free Rate**: % of sessions without crashes

### Initial Targets (6 months post-launch)
- 📱 Adoption: 40% of web users install mobile
- 📊 DAU: 30% of installed users
- ⏱️ Session Length: 2+ minutes average
- 🔄 Day-7 Retention: 50%+
- 🌟 App Store Rating: 4.0+ stars
- 💥 Crash-Free: 99%+

## 🔐 Security Considerations

### Backend Security
1. **JWT Authentication**: Short-lived access tokens (15 min)
2. **Refresh Tokens**: Long-lived tokens stored securely
3. **Rate Limiting**: Prevent API abuse
4. **Certificate Pinning**: Prevent man-in-the-middle attacks
5. **API Versioning**: Deprecation strategy for breaking changes

### Mobile Security
1. **Secure Storage**: Keychain (iOS) / Keystore (Android) for tokens
2. **Biometric Auth**: TouchID/FaceID/Fingerprint for unlock
3. **Auto-lock**: Lock app after inactivity
4. **No Credentials in Logs**: Never log sensitive data
5. **SSL Pinning**: Verify server certificate

## 📚 Required Resources

### Minimum Team
- **1 Backend Developer**: Janitorr backend modifications (Kotlin/Spring Boot)
- **1 Mobile Developer**: Flutter development (iOS/Android)
- **1 Designer**: UI/UX design for mobile
- **1 QA**: Testing on multiple devices
- **0.5 DevOps**: CI/CD setup, app store deployment

### Infrastructure
- **Firebase Project**: For push notifications (Free tier OK to start)
- **Apple Developer Account**: $99/year
- **Google Play Developer Account**: $25 one-time
- **CI/CD**: GitHub Actions (already exists)
- **Beta Testing**: TestFlight (free), Google Play Internal Testing (free)

### Estimated Costs (First Year)
```
Development (3-4 months @ contractor rates):
  - Backend Developer: $15,000 - $20,000
  - Mobile Developer: $20,000 - $30,000
  - UI/UX Designer: $5,000 - $8,000
  - QA: $5,000 - $8,000
  - DevOps: $3,000 - $5,000
  TOTAL DEV: $48,000 - $71,000

Operational (annual):
  - Apple Developer: $99
  - Google Play: $25
  - Firebase (est.): $0 - $300
  - Server costs (push notifications): $0 - $500
  TOTAL OPS: $124 - $924

TOTAL YEAR 1: $48,124 - $71,924
```

## 🎯 More Economical Alternatives

### Option 1: PWA (Progressive Web App)
**Advantages:**
- ✅ Single codebase (existing UI)
- ✅ No app stores
- ✅ Installable on iOS/Android
- ✅ Much more economical
- ✅ Push notifications (limited on iOS)

**Disadvantages:**
- ❌ No native widgets
- ❌ No native biometric auth
- ❌ Limited push notifications on iOS
- ❌ No access to all OS APIs

**Implementation:**
```javascript
// Add to existing index.html
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

**Estimated PWA Cost**: $5,000 - $10,000 (1-2 weeks)

### Option 2: React Native
Similar to Flutter but with more web resources available.

**Pros:**
- JavaScript (more developers available)
- Hot reload
- Large community

**Cons:**
- Generally worse performance than Flutter
- More native configuration required

## 🔄 Integration with Current Project

### Proposed Repository Structure
```
janitorr/                      (backend - current repo)
└── src/main/kotlin/...

janitorr-mobile/               (new repo)
├── android/
├── ios/
└── lib/

janitorr-docs/                 (optional - shared docs)
├── api-specs/
└── user-guides/
```

### CI/CD Integration
```yaml
# .github/workflows/mobile-release.yml (in janitorr-mobile repo)
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

## 📝 Recommended Next Steps

### Step 1: Validation (2-4 weeks)
1. **User Research**: Survey current users about mobile app interest
2. **Competitor Analysis**: Analyze similar apps (Overseerr, Maintainerr)
3. **Feature Prioritization**: Define minimum viable MVP
4. **Technical Spike**: Simple prototype in Flutter

### Step 2: Decision Point
Decide between:
- **Option A**: PWA (quick win, low cost)
- **Option B**: Native app (better UX, higher investment)
- **Option C**: Postpone (focus on improving current web UI)

### Step 3: Backend Preparation (if approved)
1. Implement authentication API
2. Create mobile endpoints
3. Setup push notification infrastructure
4. API documentation

### Step 4: Mobile Development
1. Setup Flutter project
2. MVP implementation
3. Internal testing
4. Beta release

## 📖 References and Resources

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

## ✅ Conclusion

Development of a Janitorr mobile companion app is **feasible and strategically valuable**, but requires:

1. ✅ **Separate Project**: New repository for mobile app
2. ✅ **Backend Modifications**: Add mobile-specific APIs
3. ✅ **Significant Resources**: 3-4 months of development
4. ✅ **Investment**: $50k-$70k year 1
5. ✅ **Prior Validation**: User research before committed resources

**Immediate Recommendation**: Start with **PWA approach** to validate demand with minimal investment, then scale to native app if justified.

---

**Document Created**: 2024  
**Author**: GitHub Copilot  
**Status**: Planning - Pending Approval  
**Last Updated**: October 2024
