# Janitorr Documentation Index

Welcome to the Janitorr documentation! This directory contains comprehensive guides for contributing, understanding the CI/CD pipeline, and working with the project.

## 📑 Quick Navigation

### For Contributors

- **[Contributing Guide](../CONTRIBUTING.md)** - Start here! Learn how to contribute to Janitorr
- **[Commit Reference](COMMIT-REFERENCE.md)** - Quick reference for conventional commit format
- **[Workflow Diagram](WORKFLOW-DIAGRAM.md)** - Visual guide to the CI/CD pipeline

### For Maintainers

- **[CI/CD Documentation](CI-CD.md)** - Comprehensive CI/CD pipeline documentation
- **[Setup Summary](SETUP-SUMMARY.md)** - Overview of the CI/CD implementation

### For Users

- **[Wiki Documentation](wiki/README.md)** - User guides in English and Spanish
  - [Docker Compose Setup (EN)](wiki/en/Docker-Compose-Setup.md)
  - [Configuration Guide (EN)](wiki/en/Configuration-Guide.md)
  - [FAQ (EN)](wiki/en/FAQ.md)
  - [Troubleshooting (EN)](wiki/en/Troubleshooting.md)
  - [Configuración Docker Compose (ES)](wiki/es/Configuracion-Docker-Compose.md)
  - [Guía de Configuración (ES)](wiki/es/Guia-Configuracion.md)
  - [Preguntas Frecuentes (ES)](wiki/es/Preguntas-Frecuentes.md)
  - [Solución de Problemas (ES)](wiki/es/Solucion-Problemas.md)

## 🚀 Quick Start for Different Audiences

### I Want to Contribute Code

1. Read [CONTRIBUTING.md](../CONTRIBUTING.md)
2. Review [COMMIT-REFERENCE.md](COMMIT-REFERENCE.md) for commit format
3. Check [WORKFLOW-DIAGRAM.md](WORKFLOW-DIAGRAM.md) to understand the process

### I Want to Understand the CI/CD

1. Start with [SETUP-SUMMARY.md](SETUP-SUMMARY.md) for overview
2. Read [CI-CD.md](CI-CD.md) for detailed documentation
3. View [WORKFLOW-DIAGRAM.md](WORKFLOW-DIAGRAM.md) for visual guide

### I Want to Deploy Janitorr

1. Check [Docker Compose Setup](wiki/en/Docker-Compose-Setup.md)
2. Review [Configuration Guide](wiki/en/Configuration-Guide.md)
3. Read [FAQ](wiki/en/FAQ.md) for common questions

## 📚 Documentation Structure

```
docs/
├── README.md                   # This file - Documentation index
├── CI-CD.md                    # Comprehensive CI/CD documentation
├── COMMIT-REFERENCE.md         # Quick commit message reference
├── SETUP-SUMMARY.md            # CI/CD setup overview
├── WORKFLOW-DIAGRAM.md         # Visual workflow diagrams
└── wiki/                       # User documentation (multilingual)
    ├── README.md               # Wiki index
    ├── en/                     # English documentation
    │   ├── Home.md
    │   ├── Configuration-Guide.md
    │   ├── Docker-Compose-Setup.md
    │   ├── FAQ.md
    │   └── Troubleshooting.md
    └── es/                     # Spanish documentation
        ├── Home.md
        ├── Configuracion-Docker-Compose.md
        ├── Guia-Configuracion.md
        ├── Preguntas-Frecuentes.md
        └── Solucion-Problemas.md
```

## 🎯 Documentation Highlights

### CI/CD Features

- ✅ Automated semantic versioning
- ✅ Conventional commits enforcement
- ✅ Automatic changelog generation
- ✅ Multi-architecture Docker builds
- ✅ Continuous integration and deployment

### Commit Standards

All commits must follow [Conventional Commits](https://www.conventionalcommits.org/):

```bash
# New feature
git commit -m "feat(media): add Plex support"

# Bug fix
git commit -m "fix(cleanup): resolve deletion issue"

# Breaking change
git commit -m "feat(api)!: change response format"
```

See [COMMIT-REFERENCE.md](COMMIT-REFERENCE.md) for full guide.

### Version Bumping

| Commit Type | Version Change | Example |
|-------------|----------------|---------|
| `fix:` | Patch (0.0.X) | 1.0.0 → 1.0.1 |
| `feat:` | Minor (0.X.0) | 1.0.0 → 1.1.0 |
| `BREAKING CHANGE:` | Major (X.0.0) | 1.0.0 → 2.0.0 |

## 🔗 External Links

- [Main Repository](https://github.com/carcheky/janitorr)
- [Docker Images](https://github.com/carcheky/janitorr/pkgs/container/janitorr)
- [Issues](https://github.com/carcheky/janitorr/issues)
- [Pull Requests](https://github.com/carcheky/janitorr/pulls)
- [Releases](https://github.com/carcheky/janitorr/releases)

## 📝 Contributing to Documentation

Documentation improvements are always welcome! To contribute:

1. Follow the same [conventional commit](COMMIT-REFERENCE.md) format
2. Update both English and Spanish versions when applicable
3. Keep documentation clear and concise
4. Add examples where helpful
5. Test any code snippets or commands

Example commit for documentation:
```bash
git commit -m "docs: update Docker setup guide with new configuration options"
```

## 🆘 Getting Help

- **General Questions**: Check [FAQ](wiki/en/FAQ.md)
- **Setup Issues**: See [Troubleshooting](wiki/en/Troubleshooting.md)
- **CI/CD Questions**: Read [CI-CD.md](CI-CD.md)
- **Bug Reports**: Open an [issue](https://github.com/carcheky/janitorr/issues)
- **Feature Requests**: Open an [issue](https://github.com/carcheky/janitorr/issues) with `feat:` in title

## 🌐 Language Support

Documentation is available in:
- 🇬🇧 English - [wiki/en/](wiki/en/)
- 🇪🇸 Español - [wiki/es/](wiki/es/)

Contributions for additional languages are welcome!

## ✨ Recent Updates

Check [CHANGELOG.md](../CHANGELOG.md) for the latest changes and releases.

---

**Last Updated**: This documentation is continuously updated. See git history for changes.

**Maintainers**: For CI/CD updates, see [SETUP-SUMMARY.md](SETUP-SUMMARY.md) for the implementation details.
