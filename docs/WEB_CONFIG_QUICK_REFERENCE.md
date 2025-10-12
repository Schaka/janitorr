# Web Configuration UI - Quick Reference

## 🚀 Quick Start

### Access the Configuration UI
```
http://<your-server>:8978/config.html
```

### First Time Setup
1. Click **⚙️ Configuration** from Management UI dashboard
2. Go to **Services** tab → Enter API keys
3. Click **🔍 Test All Connections** to verify
4. Configure **Cleanup** rules (start conservative!)
5. Set **File System** paths
6. Enable **Dry-Run Mode** in General tab
7. Click **💾 Save Configuration**
8. Restart Janitorr: `docker restart janitorr`

---

## 📋 Interface Sections

| Tab | Purpose | Key Settings |
|-----|---------|--------------|
| 🔌 **Services** | External service connections | Sonarr, Radarr, Jellyfin, Emby, Jellyseerr, Jellystat, Streamystats, Bazarr |
| 🧹 **Cleanup** | Deletion rules | Media deletion, Tag-based deletion, Episode deletion |
| 📁 **File System** | Paths and access | Leaving Soon dirs, Free space check dir, Seeding validation |
| ⚡ **General** | App behavior | Dry-run mode, Exclusion tags, Run once mode |
| 💾 **Backup** | Config management | Create backup, Export/Import, Restore, Reset |

---

## 🔧 API Endpoints

### Configuration Management
```
GET    /api/management/config           # Get current config
PUT    /api/management/config           # Save configuration
GET    /api/management/config/export    # Download YAML
POST   /api/management/config/import    # Upload YAML
```

### Connection Testing
```
POST   /api/management/config/test              # Test all services
POST   /api/management/config/test/sonarr       # Test Sonarr
POST   /api/management/config/test/radarr       # Test Radarr
POST   /api/management/config/test/jellyfin     # Test Jellyfin
POST   /api/management/config/test/emby         # Test Emby
POST   /api/management/config/test/jellyseerr   # Test Jellyseerr
```

### Backup Management
```
POST   /api/management/config/backup       # Create backup
GET    /api/management/config/backups      # List backups
POST   /api/management/config/restore      # Restore from backup
POST   /api/management/config/reset        # Reset to defaults
```

---

## ⚠️ Important Notes

### Before Production Use
1. ✅ Always enable **Dry-Run Mode** first
2. ✅ Test all connections before saving
3. ✅ Start with conservative cleanup rules
4. ✅ Review logs to see what would be deleted
5. ✅ Create a backup before making changes

### After Saving Changes
```bash
# Restart required for changes to take effect
docker restart janitorr

# Or with docker-compose
docker-compose restart janitorr
```

### Path Consistency
Ensure paths match across all containers:
```yaml
# ❌ Wrong - paths don't match
janitorr: /data/media/movies
jellyfin: /media/movies

# ✅ Correct - paths match
janitorr: /data/media/movies
jellyfin: /data/media/movies
```

---

## 🎯 Common Workflows

### Adding a New Service
1. Go to **Services** tab
2. Find the service section
3. Check **Enabled** checkbox
4. Enter **URL** and **API Key**
5. Click **🔍 Test Connection**
6. If ✅ success → Click **💾 Save Configuration**
7. Restart Janitorr

### Configuring Media Deletion
1. Go to **Cleanup** tab
2. Check **Enable Media Deletion**
3. Add **Movie Expiration** rules:
   - Example: `10% → 30 days`
   - Means: Delete movies >30 days old when disk <10% free
4. Add **Season Expiration** rules similarly
5. Click **💾 Save Configuration**
6. Restart with **Dry-Run enabled** to test
7. Review logs, then disable Dry-Run if satisfied

### Creating a Backup
1. Go to **Backup** tab
2. Click **Create Backup Now**
3. Backup appears in **Available Backups** list
4. Can restore anytime with **Restore** button

### Exporting Configuration
1. Go to **Backup** tab
2. Click **📥 Export Configuration**
3. File downloads as `application.yml`
4. Can import on another instance or keep as backup

---

## 🔍 Connection Test Results

| Status | Meaning |
|--------|---------|
| ✅ Connected successfully | Service is accessible and authenticated |
| ❌ Authentication failed | Check API key |
| ❌ Cannot reach... | Check URL and network connectivity |
| ❌ HTTP error: 404 | Wrong endpoint or service version |
| ❌ HTTP error: 401 | Invalid API key or credentials |

---

## 📊 Configuration Sections Summary

### Services Tab
- **Sonarr**: URL, API Key, Delete Empty Shows, Import Exclusions, Determine Age By
- **Radarr**: URL, API Key, Only Delete Files, Import Exclusions, Determine Age By
- **Jellyfin/Emby**: URL, API Key, Username, Password, Delete Permission, Leaving Soon Settings
- **Jellyseerr**: URL, API Key, Match Server
- **Jellystat/Streamystats**: URL, API Key, Whole TV Show
- **Bazarr**: URL, API Key

### Cleanup Tab
- **Media Deletion**: Movie/Season expiration rules (disk % → days)
- **Tag-Based Deletion**: Minimum free disk %, Tag schedules
- **Episode Deletion**: Tag, Max episodes, Max age

### File System Tab
- Enable file system access
- Validate seeding
- Leaving Soon directories (Janitorr and Media Server)
- Free space check directory
- Rebuild from scratch

### General Tab
- Dry-run mode (⚠️ ALWAYS start with this enabled!)
- Run once and exit
- Whole TV show mode
- Whole show seeding check
- Leaving soon warning days
- Exclusion tags

### Backup Tab
- Create manual backup
- Export configuration (download YAML)
- Import configuration (upload YAML)
- Reset to defaults
- List and restore from backups

---

## 🛠️ Troubleshooting

### Configuration Not Saving
```bash
# Check permissions
ls -la /config/application.yml

# Check logs
docker logs janitorr

# Verify volume mapping
docker inspect janitorr | grep -A 10 Mounts
```

### Connection Tests Failing
1. Verify service is running: `docker ps`
2. Check if accessible from Janitorr container:
   ```bash
   docker exec janitorr wget -O- http://sonarr:8989/api/v3/system/status
   ```
3. Verify API key is correct
4. Check Docker network configuration

### Changes Not Applied
1. Did you restart Janitorr?
2. Check for errors in logs
3. Verify changes saved to `/config/application.yml`

### UI Not Loading
1. Verify Management UI is enabled
2. Don't use `leyden` profile at runtime
3. Check browser console for errors
4. Clear browser cache

---

## 📖 Documentation Links

### English
- [Web Configuration Guide](docs/wiki/en/Web-Configuration-Guide.md)
- [Configuration Guide](docs/wiki/en/Configuration-Guide.md)
- [Docker Compose Setup](docs/wiki/en/Docker-Compose-Setup.md)

### Español
- [Guía de Configuración Web](docs/wiki/es/Guia-Configuracion-Web.md)
- [Guía de Configuración](docs/wiki/es/Guia-Configuracion.md)
- [Configuración Docker Compose](docs/wiki/es/Configuracion-Docker-Compose.md)

---

## 🎓 Best Practices

1. **Always use Dry-Run first** - Test without deleting anything
2. **Test connections before saving** - Verify everything works
3. **Start conservative** - Use longer expiration times initially
4. **Create backups regularly** - Especially before major changes
5. **Document your setup** - Export config and save externally
6. **Review logs regularly** - Monitor what Janitorr is doing
7. **Use exclusion tags** - Protect important media
8. **Monitor disk space** - Adjust rules based on your storage

---

## 💡 Tips & Tricks

### Testing Without Risk
1. Enable **Dry-Run Mode**
2. Save and restart
3. Check logs: `docker logs janitorr | grep "Would delete"`
4. Adjust rules based on what you see
5. Only disable Dry-Run when confident

### Sharing Configs Between Instances
1. Export from primary instance
2. Import to other instances
3. Adjust instance-specific settings (ports, paths)
4. Test and save

### Quick Rollback
If you made a mistake:
1. Go to **Backup** tab
2. Find the last backup before your changes
3. Click **Restore**
4. Restart Janitorr

### Bulk Service Testing
Instead of testing each service individually:
1. Configure all services
2. Click **🔍 Test All Connections** at top
3. Review all results at once
4. Fix any failures
5. Test again until all pass

---

**Need Help?** Check the [FAQ](docs/wiki/en/FAQ.md) or [Troubleshooting Guide](docs/wiki/en/Troubleshooting.md)
