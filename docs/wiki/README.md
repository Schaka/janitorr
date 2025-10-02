# Janitorr Wiki Documentation

This directory contains comprehensive wiki documentation for Janitorr in multiple languages.

## 📂 Structure

```
docs/wiki/
├── en/                          # English documentation
│   ├── Home.md                  # Wiki home page
│   └── Docker-Compose-Setup.md  # Complete Docker Compose guide
├── es/                          # Spanish documentation (Español)
│   ├── Home.md                  # Página de inicio de la wiki
│   └── Configuracion-Docker-Compose.md  # Guía completa de Docker Compose
└── README.md                    # This file
```

## 🌍 Available Languages

- **🇬🇧 English** - Primary language
- **🇪🇸 Español** - Spanish translation

## 📖 How to Use This Documentation

### For GitHub Wiki

These markdown files are designed to be used in the GitHub Wiki:

1. Go to your repository's Wiki tab: `https://github.com/carcheky/janitorr/wiki`
2. Click "Create the first page" or edit an existing page
3. Copy the content from the markdown files in this directory
4. The file names correspond to the wiki page names (e.g., `Docker-Compose-Setup.md` → "Docker Compose Setup")

### For Direct Reading

You can also read these files directly:

- **English:** [docs/wiki/en/Home.md](en/Home.md)
- **Español:** [docs/wiki/es/Home.md](es/Home.md)

## 📝 Documentation Contents

### English (en/)

1. **Home.md** - Wiki homepage with overview and navigation
2. **Docker-Compose-Setup.md** - Complete guide covering:
   - Prerequisites and quick start
   - Configuration steps
   - Volume mapping strategies
   - Docker Compose examples (JVM and native images)
   - Environment variables
   - Full stack example with Jellyfin, Sonarr, Radarr, etc.
   - Troubleshooting common issues

### Español (es/)

1. **Home.md** - Página de inicio de la wiki con resumen y navegación
2. **Configuracion-Docker-Compose.md** - Guía completa que cubre:
   - Requisitos previos e inicio rápido
   - Pasos de configuración
   - Estrategias de mapeo de volúmenes
   - Ejemplos de Docker Compose (imágenes JVM y nativas)
   - Variables de entorno
   - Ejemplo de stack completo con Jellyfin, Sonarr, Radarr, etc.
   - Solución de problemas comunes

## 🚀 Quick Links

### English Documentation
- [Home](en/Home.md)
- [Docker Compose Setup](en/Docker-Compose-Setup.md)

### Documentación en Español
- [Inicio](es/Home.md)
- [Configuración Docker Compose](es/Configuracion-Docker-Compose.md)

## 🔄 Updating the Wiki

To update the GitHub Wiki with this documentation:

1. **Navigate to the Wiki:** Go to `https://github.com/carcheky/janitorr/wiki`
2. **Create/Edit Pages:** Create new pages or edit existing ones
3. **Copy Content:** Copy the markdown content from the files in this directory
4. **Maintain Structure:** Keep the same page names and links structure

## 🤝 Contributing

When adding new documentation:

1. **Add to both languages** - Maintain English and Spanish versions
2. **Update navigation** - Add links in Home.md files
3. **Keep consistent** - Use the same structure across languages
4. **Test links** - Verify all internal links work correctly

## 📋 Wiki Page Mapping

| File in Repository | GitHub Wiki Page Name |
|-------------------|----------------------|
| `en/Home.md` | Home (English) |
| `en/Docker-Compose-Setup.md` | Docker Compose Setup |
| `es/Home.md` | Home (Español) |
| `es/Configuracion-Docker-Compose.md` | Configuración Docker Compose |

## ✅ What's Included

Both language versions include:

✅ Complete Docker Compose setup guide
✅ Step-by-step configuration instructions
✅ Multiple examples (basic, native, full stack)
✅ Volume mapping best practices
✅ Environment variables documentation
✅ Health check configuration
✅ Available image tags explanation
✅ Troubleshooting section
✅ Cross-references between pages

## 🔗 External References

- [Main Repository](https://github.com/carcheky/janitorr)
- [Docker Images](https://github.com/carcheky/janitorr/pkgs/container/janitorr)
- [Configuration Template](https://github.com/carcheky/janitorr/blob/main/src/main/resources/application-template.yml)
- [Example Compose File](../examples/example-compose.yml)

---

**Note:** The GitHub Wiki is a separate repository from the main code. These files serve as the source of truth for wiki content and should be manually copied to the wiki when updates are made.
