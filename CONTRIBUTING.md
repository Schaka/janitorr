# Contributing to Janitorr

Thank you for your interest in contributing to Janitorr! This guide will help you get started.

## Commit Message Convention

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) specification. All commit messages must be formatted according to this convention.

### Format

```
<type>[(<scope>)]: <subject>

[body]

[footer]
```

**Note:** Only `<type>` and `<subject>` are required. Scope, body, and footer are all optional.

### Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests or correcting existing tests
- **build**: Changes that affect the build system or external dependencies
- **ci**: Changes to our CI configuration files and scripts
- **chore**: Other changes that don't modify src or test files
- **revert**: Reverts a previous commit

### Examples

```
feat(media): add support for Plex media server

- Add Plex API integration
- Implement Plex authentication
- Add configuration options for Plex

Closes #123
```

```
fix(cleanup): resolve issue with deletion of symlinks

The cleanup process was not properly handling symlinks in the
leaving-soon directory.

Fixes #456
```

```
docs: update Docker Compose examples

Add examples for different deployment scenarios
```

```
feat: add email notification support

Notify users when cleanup occurs.
```

### Breaking Changes

Breaking changes should be indicated by:
1. Adding `BREAKING CHANGE:` in the commit footer, or
2. Adding `!` after the type/scope

Example:
```
feat(api)!: change API response format

BREAKING CHANGE: The API now returns data in a different structure.
Clients will need to update their parsing logic.
```

## Development Workflow

1. **Fork the repository** and clone it locally
2. **Create a new branch** for your feature or fix: `git checkout -b feat/my-feature`
3. **Make your changes** following the coding standards
4. **Test your changes** thoroughly
5. **Commit your changes** using conventional commit messages
6. **Push to your fork** and create a Pull Request

## Setting Up Development Environment

### Prerequisites

- Java 25 (Temurin distribution)
- Gradle 9.1.0 or higher
- Docker (for testing container builds)

### Build the project

```bash
./gradlew build
```

### Run tests

```bash
./gradlew test
```

### Run locally

```bash
./gradlew bootRun
```

## Pull Request Process

1. Ensure all tests pass and the build succeeds
2. Update documentation if you're adding new features
3. Follow the commit message convention for all commits
4. The PR title should also follow the conventional commit format
5. Link any related issues in the PR description
6. Wait for review and address any feedback

## CI/CD Pipeline

The project uses GitHub Actions for CI/CD:

- **Commit Validation**: All PR commits are validated against conventional commit standards
- **Build & Test**: Automated building and testing on every push and PR
- **Semantic Release**: Automated versioning and release creation on main/develop branches
- **Docker Images**: Automated building and publishing of Docker images to GHCR

### Release Process

Releases are fully automated using semantic-release:

- **main branch**: Production releases (e.g., v1.0.0, v1.1.0)
- **develop branch**: Pre-release versions (e.g., v1.1.0-develop.1)

The version is automatically determined based on commit messages:
- `feat:` → Minor version bump (0.X.0)
- `fix:` → Patch version bump (0.0.X)
- `BREAKING CHANGE:` → Major version bump (X.0.0)

## Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions focused and small
- Write tests for new features

## Getting Help

- Open an issue for bugs or feature requests
- Check existing issues and discussions
- Review the documentation in the [docs](docs/) directory

Thank you for contributing to Janitorr! 🎉
