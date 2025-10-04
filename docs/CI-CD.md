# CI/CD Documentation

This document describes the Continuous Integration and Continuous Deployment (CI/CD) setup for Janitorr.

## Overview

Janitorr uses GitHub Actions for automated building, testing, and releasing. The CI/CD pipeline is designed to be fully automated, requiring minimal manual intervention.

## Workflows

### 1. CI/CD Pipeline (`ci-cd.yml`)

The main workflow that runs on every push and pull request to `main` and `develop` branches.

**Jobs:**

- **commitlint**: Validates commit messages against conventional commit standards (PRs only)
- **build**: Builds the application using Gradle and runs tests
- **release**: Creates automated releases using semantic-release (main/develop branches only)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests targeting `main` or `develop` branches
- Manual dispatch via `workflow_dispatch`

### 2. JVM Image Build (`jvm-image.yml`)

Builds multi-platform JVM Docker images for x86_64 and ARM64 architectures.

**Jobs:**
- **build-jvm-x86**: Builds JVM image for x86_64
- **build-jvm-aarch64**: Builds JVM image for ARM64
- **combine-images**: Creates multi-arch manifest

**Output Images:**
- `ghcr.io/carcheky/janitorr:jvm-main` and `ghcr.io/carcheky/janitorr:main` (main branch)
- `ghcr.io/carcheky/janitorr:jvm-develop` and `ghcr.io/carcheky/janitorr:develop` (develop branch)
- `ghcr.io/carcheky/janitorr:jvm-stable`, `ghcr.io/carcheky/janitorr:latest`, `ghcr.io/carcheky/janitorr:jvm-v1.x.x`, and `ghcr.io/carcheky/janitorr:v1.x.x` (tagged releases)

### 3. Native Image Build (`native-image.yml`)

Builds multi-platform GraalVM native Docker images for x86_64 and ARM64 architectures.

**Jobs:**
- **build-native-x86**: Builds native image for x86_64
- **build-native-aarch64**: Builds native image for ARM64
- **combine-images**: Creates multi-arch manifest

**Output Images:**
- `ghcr.io/carcheky/janitorr:native-main` (main branch)
- `ghcr.io/carcheky/janitorr:native-develop` (develop branch)
- `ghcr.io/carcheky/janitorr:native-stable`, `ghcr.io/carcheky/janitorr:native-latest`, and `ghcr.io/carcheky/janitorr:native-v1.x.x` (tagged releases)

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

```
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

| Branch/Tag | JVM Image | Native Image |
|------------|-----------|--------------|
| `main` | `jvm-main`, `main` | `native-main` |
| `develop` | `jvm-develop`, `develop` | `native-develop` |
| `v1.0.0` tag | `jvm-stable`, `latest`, `jvm-v1.0.0`, `v1.0.0` | `native-stable`, `native-latest`, `native-v1.0.0` |

### Using Images

```yaml
# Latest stable JVM image (recommended)
image: ghcr.io/carcheky/janitorr:latest
# Or explicitly:
image: ghcr.io/carcheky/janitorr:jvm-stable

# Latest main branch (JVM)
image: ghcr.io/carcheky/janitorr:main

# Development JVM image
image: ghcr.io/carcheky/janitorr:develop

# Latest stable native image (deprecated)
image: ghcr.io/carcheky/janitorr:native-latest

# Specific version (JVM)
image: ghcr.io/carcheky/janitorr:v1.0.0
# Or with prefix:
image: ghcr.io/carcheky/janitorr:jvm-v1.0.0

# Specific native version
image: ghcr.io/carcheky/janitorr:native-v1.0.0
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
