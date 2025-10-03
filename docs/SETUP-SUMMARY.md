# GitHub Actions CI/CD Setup Summary

This document provides a quick overview of the GitHub Actions CI/CD setup implemented for Janitorr.

## What Was Added

### 1. Semantic Release Configuration

**Files:**
- `package.json` - Node.js package configuration with semantic-release dependencies
- `.releaserc.json` - Semantic-release configuration
- `.npmrc` - NPM configuration to prevent package-lock.json creation

**Purpose:** Automatically version the project and create releases based on conventional commits.

### 2. Commit Message Validation

**Files:**
- `.commitlintrc.json` - Commitlint configuration for enforcing conventional commits
- `.husky/commit-msg` - Git hook for local commit validation (optional)

**Purpose:** Ensure all commits follow the Conventional Commits specification.

### 3. GitHub Actions Workflows

**Files:**
- `.github/workflows/ci-cd.yml` - Main CI/CD pipeline
- `.github/workflows/jvm-image.yml` - Updated with workflow_dispatch trigger
- `.github/workflows/native-image.yml` - Updated with workflow_dispatch trigger

**Purpose:** Automate building, testing, and releasing.

### 4. Documentation

**Files:**
- `CONTRIBUTING.md` - Contributor guidelines with commit conventions
- `docs/CI-CD.md` - Comprehensive CI/CD documentation
- `CHANGELOG.md` - Changelog (will be automatically updated)
- `README.md` - Updated with badges and contributing section

**Purpose:** Help contributors understand the development workflow.

### 5. Configuration Updates

**Files:**
- `.gitignore` - Updated to exclude node_modules and npm artifacts

## How It Works

### For Pull Requests

1. **Commit Validation**: All commits are validated against conventional commit standards
2. **Build & Test**: The application is built and tested
3. **Manual Review**: Maintainers review and approve the PR

### For Main/Develop Branch

1. **Build & Test**: Automated build and test
2. **Semantic Release**: 
   - Analyzes commits since last release
   - Determines next version number
   - Generates changelog
   - Creates GitHub release
   - Pushes changes back to repository
3. **Docker Images**: Existing workflows build and publish Docker images

## Branch Strategy

- **main**: Production releases (e.g., v1.0.0, v1.1.0, v1.2.0)
- **develop**: Pre-release versions (e.g., v1.1.0-develop.1, v1.1.0-develop.2)

## Versioning Rules

Based on commit messages:

| Commit Type | Version Impact | Example |
|-------------|----------------|---------|
| `fix:` | Patch (0.0.X) | v1.0.0 → v1.0.1 |
| `feat:` | Minor (0.X.0) | v1.0.0 → v1.1.0 |
| `BREAKING CHANGE:` or `!` | Major (X.0.0) | v1.0.0 → v2.0.0 |

## Commit Message Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**Valid types:** feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert

**Examples:**

```bash
feat(media): add Plex support
fix(cleanup): resolve symlink deletion issue
docs: update Docker setup guide
feat(api)!: change response format

BREAKING CHANGE: API structure has changed
```

## Getting Started for Contributors

### Option 1: With Local Commit Validation (Recommended)

```bash
# Clone the repository
git clone https://github.com/carcheky/janitorr.git
cd janitorr

# Install Node.js dependencies (for commit validation)
npm install

# Make your changes
# Husky will automatically validate your commits

# Commit using conventional commit format
git commit -m "feat(feature): add new feature"
```

### Option 2: Without Local Validation

```bash
# Clone the repository
git clone https://github.com/carcheky/janitorr.git
cd janitorr

# Make your changes

# Commit using conventional commit format
git commit -m "feat(feature): add new feature"

# Note: Commits will be validated in CI when you open a PR
```

## Testing Your Changes

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Build Docker image (if needed)
./gradlew bootBuildImage
```

## Workflow Triggers

All workflows can be triggered:
- Automatically on push/PR
- Manually via GitHub Actions UI (workflow_dispatch)

## Required Secrets

The following secrets should be configured in GitHub repository settings:

- `GITHUB_TOKEN` - Automatically provided by GitHub Actions
- `DOCKERHUB_USER` - Docker Hub username (for rate limit avoidance)
- `DOCKERHUB_PASSWORD` - Docker Hub password/token

## Troubleshooting

### Commits Rejected in PR

If your commits don't follow the conventional format:
1. View the error message in the Actions log
2. Fix the commit message with interactive rebase: `git rebase -i HEAD~N`
3. Force push: `git push --force-with-lease`

### No Release Created

Make sure:
1. At least one commit has `feat:`, `fix:`, or `BREAKING CHANGE:`
2. Commits are on `main` or `develop` branch
3. Check the semantic-release logs in GitHub Actions

## Next Steps

The workflow is now ready to use! When the first PR with conventional commits is merged to `main`, semantic-release will:
1. Create the first release (likely v1.0.0)
2. Update CHANGELOG.md
3. Create a Git tag
4. Create a GitHub release

## Additional Resources

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Release](https://semantic-release.gitbook.io/)
- [Commitlint](https://commitlint.js.org/)
- [Full CI/CD Documentation](docs/CI-CD.md)
- [Contributing Guide](CONTRIBUTING.md)
