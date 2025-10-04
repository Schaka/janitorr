# CI/CD Workflow Diagram

## Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Developer Workflow                            │
└─────────────────────────────────────────────────────────────────┘

  Developer            GitHub              CI/CD Pipeline
     │                   │                       │
     │  1. Create PR     │                       │
     ├──────────────────>│                       │
     │                   │                       │
     │                   │  2. Trigger CI        │
     │                   ├──────────────────────>│
     │                   │                       │
     │                   │                       │  3. Validate Commits
     │                   │                       │     (commitlint)
     │                   │                       │
     │                   │                       │  4. Build & Test
     │                   │                       │     (Gradle)
     │                   │                       │
     │                   │  5. Report Status     │
     │                   │<──────────────────────│
     │                   │                       │
     │  6. Review        │                       │
     │<──────────────────│                       │
     │                   │                       │
     │  7. Merge to main │                       │
     ├──────────────────>│                       │
     │                   │                       │
     │                   │  8. Trigger Release   │
     │                   ├──────────────────────>│
     │                   │                       │
     │                   │                       │  9. Semantic Release
     │                   │                       │     - Analyze commits
     │                   │                       │     - Determine version
     │                   │                       │     - Generate changelog
     │                   │                       │     - Create release
     │                   │                       │
     │                   │  10. Push changes     │
     │                   │<──────────────────────│
     │                   │      & Create tag     │
     │                   │                       │
     │                   │  11. Build Docker     │
     │                   ├──────────────────────>│
     │                   │      Images           │
     │                   │                       │  12. Publish to GHCR
     │                   │                       │      - JVM image
     │                   │                       │      - Native image
     │                   │                       │
     │  13. Notify       │                       │
     │<──────────────────│                       │
     │   (Release created)                       │
     │                   │                       │
```

## Branch Strategy

```
main (production)
  │
  ├── v1.0.0 ────> GitHub Release + Docker Images (jvm-stable, native-stable)
  │
  ├── v1.1.0 ────> GitHub Release + Docker Images
  │
  └── v2.0.0 ────> GitHub Release + Docker Images


develop (pre-release)
  │
  ├── v1.1.0-develop.1 ────> Pre-release + Docker Images (jvm-develop, native-develop)
  │
  ├── v1.1.0-develop.2 ────> Pre-release + Docker Images
  │
  └── Merge to main ────> Triggers stable release (v1.1.0)
```

## Version Determination Flow

```
┌─────────────────────────┐
│  Analyze Commits        │
│  Since Last Release     │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Contains BREAKING       │
│ CHANGE or ! ?           │
└────┬────────────┬───────┘
     │ YES        │ NO
     ▼            ▼
┌─────────┐  ┌──────────────────┐
│ MAJOR   │  │ Contains feat: ? │
│ X.0.0   │  └────┬─────────┬───┘
└─────────┘       │ YES     │ NO
                  ▼         ▼
              ┌─────────┐ ┌──────────────────┐
              │ MINOR   │ │ Contains fix: ?  │
              │ 0.X.0   │ └────┬─────────┬───┘
              └─────────┘      │ YES     │ NO
                               ▼         ▼
                           ┌─────────┐ ┌─────────┐
                           │ PATCH   │ │ No      │
                           │ 0.0.X   │ │ Release │
                           └─────────┘ └─────────┘
```

## Commit Message Impact

```
Type                 Scope      Impact          Example Version
────────────────────────────────────────────────────────────────
fix:                 Any        Patch bump      1.0.0 → 1.0.1
feat:                Any        Minor bump      1.0.0 → 1.1.0
BREAKING CHANGE:     Any        Major bump      1.0.0 → 2.0.0
feat!:               Any        Major bump      1.0.0 → 2.0.0
docs:                Any        No release      -
chore:               Any        No release      -
style:               Any        No release      -
refactor:            Any        No release      -
test:                Any        No release      -
```

## Docker Image Tags

```
Branch/Tag          JVM Image (janitorr)          Native Image (janitorr-native)
────────────────────────────────────────────────────────────────────────────────
main               main                           main
develop            develop                        develop
v1.0.0 (tag)       latest, 1.0.0                 latest, 1.0.0
v1.1.0-develop.1   develop                        develop
```

## Workflow Jobs

```
┌──────────────────────────────────────────────────────────────┐
│                      CI/CD Workflow                           │
└──────────────────────────────────────────────────────────────┘

Pull Request:
  ┌─────────────────┐
  │  Commitlint     │  Validates commit messages
  └─────────────────┘
          │
          ▼
  ┌─────────────────┐
  │  Build & Test   │  Gradle build + tests
  └─────────────────┘


Push to main/develop:
  ┌─────────────────┐
  │  Build & Test   │  Gradle build + tests
  └─────────────────┘
          │
          ▼
  ┌─────────────────┐
  │ Semantic        │  Create release if needed
  │ Release         │  - Version bump
  └─────────────────┘  - Changelog
          │            - Git tag
          │            - GitHub release
          ▼
  ┌─────────────────┐
  │ Docker Images   │  Build & publish
  │ (Existing)      │  - JVM images (x86, ARM64)
  └─────────────────┘  - Native images (x86, ARM64)
```

## Success Criteria

```
✅ All commits follow conventional format
✅ Build passes
✅ Tests pass
✅ Version determined automatically
✅ Changelog updated
✅ Release created on GitHub
✅ Docker images published to GHCR
✅ No manual intervention required
```

## Error Handling

```
Commit Format Error:
  PR Check Fails → Developer fixes → Re-push → Re-validate

Build/Test Error:
  CI Fails → Developer fixes → Re-push → Re-run

No Releasable Commits:
  Semantic Release → Skip → No new version created
```

## Getting Started

```
Step 1: Make changes
   │
   ▼
Step 2: Commit with conventional format
   │    git commit -m "feat: add new feature"
   │
   ▼
Step 3: Push and create PR
   │
   ▼
Step 4: CI validates commits
   │
   ▼
Step 5: Merge after approval
   │
   ▼
Step 6: Automatic release! 🎉
```

---

For more details, see:
- [CI/CD Documentation](CI-CD.md)
- [Contributing Guide](../CONTRIBUTING.md)
- [Commit Reference](COMMIT-REFERENCE.md)
