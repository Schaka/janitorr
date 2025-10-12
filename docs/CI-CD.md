# CI/CD Documentation

This document describes the Continuous Integration and Continuous Deployment (CI/CD) setup for Janitorr.

## Overview

Janitorr uses GitHub Actions for automated building, testing, and releasing. The CI/CD pipeline is designed to be fully automated, requiring minimal manual intervention.

## Workflows

### 1. CI/CD Pipeline (`ci-cd.yml`)

The main workflow that handles continuous integration and deployment automation.

**Purpose:**

- Validates code quality and commit standards
- Runs comprehensive build and test suite
- Automates semantic versioning and releases

**Jobs:**

- **commitlint**: Validates commit messages against conventional commit standards (PRs only)
- **build**: Builds the application using Gradle and runs complete test suite (excludes Docker image building)
- **release**: Creates automated releases using semantic-release (main/develop branches only)

**Triggers:**

- Push to `main` or `develop` branches
- Pull requests targeting `main` or `develop` branches
- Manual dispatch via `workflow_dispatch`

**Note:** This workflow focuses purely on CI/CD and does not build Docker images for efficiency.

### 2. JVM Image Build (`jvm-image.yml`)

Dedicated workflow for building and publishing multi-platform JVM Docker images.

**Purpose:**

- Builds production-ready Docker images
- Supports multi-architecture (AMD64/ARM64)
- Handles proper image tagging and publishing

**Jobs:**

- **build-jvm-x86**: Builds JVM image for x86_64 (native build on AMD64 runner)
- **build-jvm-aarch64**: Builds JVM image for ARM64 (using QEMU emulation on AMD64 runner)
- **combine-images**: Creates multi-arch manifest

**Triggers:**

- Push to `main` or `develop` branches (only when source code changes)
- Pull requests with relevant file changes (fork-safe with permission checks)
- New release publications (via semantic-release)
- Manual dispatch via `workflow_dispatch`
- Git tags (for version-specific builds)

**Note:** ARM64 images are built using QEMU emulation on standard GitHub-hosted runners, which allows cross-platform builds without requiring ARM64 hardware.

**Output Images:**

- `ghcr.io/carcheky/janitorr:main` (main branch)
- `ghcr.io/carcheky/janitorr:develop` (develop branch)
- `ghcr.io/carcheky/janitorr:latest`, `ghcr.io/carcheky/janitorr:1.x.x` (tagged releases)

## Semantic Release

Janitorr uses [semantic-release](https://semantic-release.gitbook.io/) to automate the versioning and release process.

### How It Works

1. **Commit Analysis**: Analyzes commit messages to determine the next version number
2. **Release Notes**: Automatically generates release notes from commits
3. **Changelog**: Updates `CHANGELOG.md` with the new version
4. **Git Tag**: Creates a Git tag for the new version
5. **GitHub Release**: Creates a GitHub release with the release notes

### Version Determination

The version is automatically determined based on commit types:

| Commit Type | Example | Version Impact |
|-------------|---------|----------------|
| `fix:` | `fix: resolve memory leak` | Patch (0.0.X) |
| `feat:` | `feat: add Plex support` | Minor (0.X.0) |
| `BREAKING CHANGE:` | `feat!: change API format` | Major (X.0.0) |
| `chore:`, `docs:`, etc. | `docs: update README` | No release |

### Branch Strategy

- **main**: Production releases (e.g., `v1.0.0`, `v1.1.0`)
- **develop**: Pre-release versions (e.g., `v1.1.0-develop.1`, `v1.1.0-develop.2`)

## Commit Message Convention

All commit messages must follow the [Conventional Commits](https://www.conventionalcommits.org/) specification.

### Format

```text
<type>[(<scope>)]: <subject>
```

**Note:** Scope (the part in parentheses) is optional. You can use either:

- `feat: add new feature` (without scope)
- `feat(media): add new feature` (with scope)

### Valid Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `build`: Build system changes
- `ci`: CI/CD changes
- `chore`: Maintenance tasks
- `revert`: Revert previous commit

### Examples

```bash
# Feature (with scope)
git commit -m "feat(media): add support for Plex"

# Feature (without scope)
git commit -m "feat: add support for user profiles"

# Bug fix (with scope)
git commit -m "fix(cleanup): resolve symlink deletion issue"

# Bug fix (without scope)
git commit -m "fix: resolve memory leak"

# Documentation (without scope)
git commit -m "docs: update Docker setup guide"

# Breaking change
git commit -m "feat(api)!: change response format

BREAKING CHANGE: API response structure has changed"
```

## Commit Validation

### Pull Requests

All commits in pull requests are validated using `commitlint` to ensure they follow the conventional commit format. PRs with invalid commit messages will fail the CI check.

### Local Development (Optional)

You can set up local commit validation using Husky:

```bash
# Install dependencies
npm install

# Husky will automatically set up git hooks
```

This will validate commit messages before they are committed locally.

## Docker Image Publishing

Docker images are automatically built and published to GitHub Container Registry (GHCR) when:

1. Code is pushed to `main` or `develop` branches
2. A new release tag is created

### Image Tagging Strategy

| Branch/Tag | JVM Image (janitorr) | Native Image (janitorr-native) |
|------------|----------------------|--------------------------------|
| `main` | `main` | `main` |
| `develop` | `develop` | `develop` |
| `v1.0.0` tag | `latest`, `1.0.0` | `latest`, `1.0.0` |

### Using Images

```yaml
# Latest stable JVM image (recommended)
image: ghcr.io/carcheky/janitorr:latest

# Specific version JVM image
image: ghcr.io/carcheky/janitorr:1.0.0

# Latest main branch (JVM)
image: ghcr.io/carcheky/janitorr:main

# Development JVM image
image: ghcr.io/carcheky/janitorr:develop

# Latest stable native image (deprecated)
image: ghcr.io/carcheky/janitorr-native:latest

# Latest main branch native image
image: ghcr.io/carcheky/janitorr-native:main

# Specific native version
image: ghcr.io/carcheky/janitorr-native:1.0.0
```

## Manual Workflow Triggers

All workflows support manual triggering via the GitHub Actions UI:

1. Go to **Actions** tab in GitHub
2. Select the workflow you want to run
3. Click **Run workflow**
4. Select the branch and click **Run workflow**

## Secrets Required

The following secrets must be configured in GitHub repository settings:

- `GITHUB_TOKEN`: Automatically provided by GitHub Actions

## Troubleshooting

### Commit Validation Failures

If your PR fails commit validation:

1. Review the failed checks to see which commits are invalid
2. Either:
   - Amend the commit messages: `git commit --amend`
   - Use interactive rebase: `git rebase -i HEAD~N` (where N is the number of commits)
3. Force push: `git push --force-with-lease`

### Release Not Created

If a release is not created after merging to main:

1. Check that commits follow conventional commit format
2. Verify at least one commit has `feat:`, `fix:`, or `BREAKING CHANGE:`
3. Check the Actions log for semantic-release errors

### Docker Build Failures

If Docker image builds fail:

1. Check the Actions logs for specific errors
2. Ensure sufficient disk space in runners
3. Check for Gradle build errors

## Best Practices

1. **Always use conventional commits**: This ensures proper versioning
2. **Write meaningful commit messages**: They become part of the changelog
3. **Group related changes**: Use feature branches for related changes
4. **Test locally**: Run `./gradlew build test` before pushing
5. **Keep PRs focused**: Smaller PRs are easier to review and merge

## Additional Resources

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Release](https://semantic-release.gitbook.io/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Commitlint](https://commitlint.js.org/)
