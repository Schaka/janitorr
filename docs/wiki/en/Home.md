# Janitorr Wiki

Welcome to the Janitorr wiki! This documentation will help you set up and configure Janitorr to manage and clean up your media library.

## 📚 Table of Contents

### Getting Started
- [Docker Compose Setup](Docker-Compose-Setup.md) - Complete guide to deploying Janitorr with Docker Compose
- [Configuration Guide](Configuration-Guide.md) - How to configure your application.yml file
- [FAQ](FAQ.md) - Frequently asked questions
- [Troubleshooting](Troubleshooting.md) - Common issues and solutions

### Languages
- 🇬🇧 **English** (current)
- 🇪🇸 [Español](../es/Home.md)

## What is Janitorr?

**Janitorr** manages your media and cleans up after you.

- Do you hate being the janitor of your server?
- Do you have a lot of media that never gets watched?
- Do your users constantly request media, and let it sit there afterward never to be touched again?

Then you need Janitorr for Jellyfin and Emby. It's THE solution for cleaning up your server and freeing up space before you run into issues.

## Quick Links

- [GitHub Repository](https://github.com/carcheky/janitorr)
- [Docker Images](https://github.com/carcheky/janitorr/pkgs/container/janitorr)
- [Discussions](https://github.com/carcheky/janitorr/discussions)
- [Issues](https://github.com/carcheky/janitorr/issues)

## Key Features

- **Web-based Management UI** - Monitor status and manually trigger cleanup functions
- Remote deletion, disk space aware deletion as well as tag-based delete schedules
- Exclude items from deletion via tags in Sonarr/Radarr
- Configure expiration times for your media in the *arrs - optionally via Jellystat
- Season by season removal for TV shows, removing entire shows or only keep a minimum number of episodes
- Clear requests from Jellyseerr and clean up leftover metadata in Jellyfin
- Show a "Leaving soon" collection on the Jellyfin home screen before deletion

## Support

Before creating a new issue:
1. Check [Troubleshooting](Troubleshooting.md) for common problems
2. Review the [FAQ](FAQ.md)
3. Search existing [issues](https://github.com/carcheky/janitorr/issues)
4. Start a [discussion](https://github.com/carcheky/janitorr/discussions) for questions
