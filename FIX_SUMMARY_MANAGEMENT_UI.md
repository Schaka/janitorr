# Management UI 404 Error - Fix Summary

## Problem Description

The Management UI was returning a "Whitelabel Error Page" with HTTP 404 when accessing `http://localhost:8978/`.

### Symptoms
- ❌ Accessing root URL (`/`) resulted in 404 error
- ❌ "Whitelabel Error Page" displayed instead of the Management UI
- ✅ Static files existed in `src/main/resources/static/` directory
- ✅ ManagementController API endpoints were available at `/api/management/*`

## Root Cause

Spring Boot serves static resources from the `/static` directory by default, making them accessible at the root context. However, **there was no controller mapping for the root path "/" to serve the index.html file**.

Without an explicit mapping, Spring Boot's default error handling kicked in, resulting in a 404 error.

## Solution

The fix involved three minimal changes:

### 1. Created RootController

**File:** `src/main/kotlin/com/github/schaka/janitorr/api/RootController.kt`

```kotlin
package com.github.schaka.janitorr.api

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Profile("!leyden")
@Controller
class RootController {

    @GetMapping("/")
    fun index(): String {
        return "forward:/index.html"
    }
}
```

**Key Points:**
- Uses `@Controller` (not `@RestController`) to enable view resolution
- `@GetMapping("/")` handles root path requests
- `forward:/index.html` forwards to the static index.html file
- `@Profile("!leyden")` excludes from AOT build profile (consistent with ManagementController)

### 2. Added Runtime Hints for Native Image

**File:** `src/main/kotlin/com/github/schaka/janitorr/JanitorrApplication.kt`

Added static resource registration in the `Hints` class:

```kotlin
// Register static resources for Management UI
hints.resources().registerPattern("static/*")
```

This ensures static resources are included when building native images with GraalVM.

### 3. Created Test Coverage

**File:** `src/test/kotlin/com/github/schaka/janitorr/api/RootControllerTest.kt`

```kotlin
@WebMvcTest(RootController::class)
@ActiveProfiles("test")
@Import(RootController::class)
class RootControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `root path should forward to index html`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(forwardedUrl("/index.html"))
    }
}
```

## Technical Details

### Why `forward:` instead of `redirect:`?

- `forward:/index.html` - Server-side forward, keeps original URL, faster
- `redirect:/index.html` - Client-side redirect, changes URL, additional round-trip

Using `forward:` is more efficient and keeps the user on the root path.

### Why `@Controller` instead of `@RestController`?

- `@Controller` - Enables view resolution and forwarding
- `@RestController` - Returns response body directly, cannot forward to views

### Why exclude from leyden profile?

The `leyden` profile is used during AOT (Ahead-of-Time) compilation build process, not at runtime. The Management UI and all cleanup schedules are excluded from this profile because:

1. They're not needed during the training run
2. They depend on application configuration that isn't available at build time
3. At runtime, the leyden profile is NOT active, so these components are available

## Testing the Fix

### Unit Tests
```bash
./gradlew test --tests RootControllerTest
```

### Integration Testing
1. Build the application:
   ```bash
   ./gradlew build
   ```

2. Run the application:
   ```bash
   ./gradlew bootRun
   ```

3. Access the Management UI:
   ```
   http://localhost:8978/
   ```

### Expected Behavior

After the fix:
- ✅ Root URL (`/`) displays the Management UI
- ✅ `index.html`, `app.js`, and `styles.css` load correctly
- ✅ Status information displays from `/api/management/status`
- ✅ Cleanup buttons trigger `/api/management/cleanup/*` endpoints
- ✅ Works in both JVM and native Docker images

## Files Changed

1. **NEW**: `src/main/kotlin/com/github/schaka/janitorr/api/RootController.kt`
2. **NEW**: `src/test/kotlin/com/github/schaka/janitorr/api/RootControllerTest.kt`
3. **MODIFIED**: `src/main/kotlin/com/github/schaka/janitorr/JanitorrApplication.kt`

Total lines changed: ~50 lines (minimal, surgical fix)

## Impact

### Positive Impact
- ✅ Management UI now accessible at root path
- ✅ Users can monitor and control Janitorr through web interface
- ✅ No breaking changes to existing functionality
- ✅ Consistent with existing code patterns
- ✅ Properly tested with unit tests

### No Negative Impact
- ✅ Existing API endpoints unchanged
- ✅ No performance impact
- ✅ No security implications (UI had no auth before and still doesn't)
- ✅ Backward compatible

## Related Documentation

- [Management UI Documentation](MANAGEMENT_UI.md)
- [Docker Compose Setup](docs/wiki/en/Docker-Compose-Setup.md)
- [Configuración Docker Compose](docs/wiki/es/Configuracion-Docker-Compose.md)

## Future Considerations

1. Consider adding authentication to Management UI (currently open access)
2. Consider making Management UI optional via configuration
3. Consider adding health check endpoint for UI availability

## References

- **Issue**: Management UI not loading - 404 error
- **Priority**: Critical
- **Fix Type**: Feature addition (new controller)
- **Complexity**: Low
- **Testing**: Unit tested
