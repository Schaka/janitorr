# Management UI Request Flow - Before and After Fix

## BEFORE Fix (404 Error)

```
User Browser
    |
    | GET http://localhost:8978/
    ↓
Spring Boot Application
    |
    | No controller mapped to "/"
    ↓
Spring Boot Error Handler
    |
    | Generates Whitelabel Error Page
    ↓
User Browser
    |
    | ❌ 404 Not Found - Whitelabel Error Page
```

## AFTER Fix (Working)

```
User Browser
    |
    | GET http://localhost:8978/
    ↓
RootController
    |
    | @GetMapping("/")
    | returns "forward:/index.html"
    ↓
Spring Boot Static Resource Handler
    |
    | Locates /static/index.html
    | Serves the file
    ↓
User Browser
    |
    | ✅ 200 OK - Management UI displays
    |
    | Loads additional resources:
    | - GET /styles.css → /static/styles.css ✅
    | - GET /app.js → /static/app.js ✅
    |
    | JavaScript makes API calls:
    | - GET /api/management/status ✅
    | - POST /api/management/cleanup/media ✅
    | - POST /api/management/cleanup/tag-based ✅
    | - POST /api/management/cleanup/episodes ✅
```

## Component Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Spring Boot App                       │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Controllers Layer                                │  │
│  │                                                    │  │
│  │  ┌─────────────────┐    ┌────────────────────┐  │  │
│  │  │ RootController  │    │ ManagementController│  │  │
│  │  │   GET /         │    │  /api/management/*  │  │  │
│  │  │ forward:index   │    │  - GET /status      │  │  │
│  │  │                 │    │  - POST /cleanup/*  │  │  │
│  │  └─────────────────┘    └────────────────────┘  │  │
│  │                                                    │  │
│  │  Both have @Profile("!leyden")                    │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Static Resources (/static)                       │  │
│  │                                                    │  │
│  │  - index.html  ←─── forwarded from RootController │  │
│  │  - app.js      ←─── loaded by index.html          │  │
│  │  - styles.css  ←─── loaded by index.html          │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Runtime Hints (for Native Image)                 │  │
│  │                                                    │  │
│  │  hints.resources().registerPattern("static/*")    │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## Profile Behavior

### Build Time (leyden profile ACTIVE)
```
AOT Compilation Process
    |
    | Spring profiles: leyden
    ↓
Components loaded:
    ✅ Core services
    ✅ Configuration
    ❌ RootController (excluded by @Profile("!leyden"))
    ❌ ManagementController (excluded by @Profile("!leyden"))
    ❌ Cleanup schedules (excluded by @Profile("!leyden"))
    |
    | Training run to generate AOT cache
    ↓
AOT Cache Created
```

### Runtime (leyden profile INACTIVE)
```
Docker Container Start
    |
    | Spring profiles: default (no leyden)
    ↓
Components loaded:
    ✅ Core services
    ✅ Configuration
    ✅ RootController (active because !leyden)
    ✅ ManagementController (active because !leyden)
    ✅ Cleanup schedules (active because !leyden)
    |
    | Normal application operation
    ↓
Management UI Available at /
```

## File Structure

```
janitorr/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/github/schaka/janitorr/
│   │   │       ├── api/
│   │   │       │   ├── RootController.kt          ← NEW ✨
│   │   │       │   └── ManagementController.kt
│   │   │       └── JanitorrApplication.kt         ← MODIFIED (runtime hints)
│   │   └── resources/
│   │       └── static/
│   │           ├── index.html   ← exists (UI entry point)
│   │           ├── app.js       ← exists (UI logic)
│   │           └── styles.css   ← exists (UI styling)
│   └── test/
│       └── kotlin/
│           └── com/github/schaka/janitorr/
│               └── api/
│                   └── RootControllerTest.kt      ← NEW ✨
└── FIX_SUMMARY_MANAGEMENT_UI.md                   ← NEW ✨ (this doc)
```

## Summary

**Problem**: No controller mapped "/" to serve the UI  
**Solution**: Added RootController with forward to index.html  
**Result**: Management UI now loads correctly at root path  
**Impact**: Minimal (3 new files, 1 modified, ~50 lines total)  
