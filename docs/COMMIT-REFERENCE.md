# Quick Reference: Conventional Commits

## Commit Message Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

## Common Types

| Type | When to Use | Version Impact |
|------|-------------|----------------|
| `feat` | New feature | Minor (0.X.0) |
| `fix` | Bug fix | Patch (0.0.X) |
| `docs` | Documentation only | No release |
| `style` | Code style/formatting | No release |
| `refactor` | Code restructuring | No release |
| `perf` | Performance improvement | Patch (0.0.X) |
| `test` | Adding/updating tests | No release |
| `build` | Build system changes | No release |
| `ci` | CI/CD changes | No release |
| `chore` | Maintenance tasks | No release |

## Quick Examples

```bash
# New feature
git commit -m "feat(media): add Plex integration"

# Bug fix
git commit -m "fix(cleanup): resolve deletion of symlinks"

# Documentation
git commit -m "docs: update Docker setup guide"

# Breaking change (Method 1: with !)
git commit -m "feat(api)!: change response format"

# Breaking change (Method 2: with footer)
git commit -m "feat(api): change response format

BREAKING CHANGE: API response structure has changed from array to object"

# Multiple types in one commit (use the most significant)
git commit -m "feat(ui): add new dashboard with tests"
```

## Breaking Changes

Two ways to indicate breaking changes:

1. **Add `!` after type/scope:**
   ```
   feat(api)!: change endpoint structure
   ```

2. **Use `BREAKING CHANGE:` in footer:**
   ```
   feat(api): change endpoint structure
   
   BREAKING CHANGE: Endpoints now use /v2/ prefix
   ```

Both trigger a **major** version bump (X.0.0)

## Scope

Optional but recommended. Examples:
- `(media)` - Media server related
- `(cleanup)` - Cleanup functionality
- `(api)` - API changes
- `(config)` - Configuration
- `(docker)` - Docker related
- `(ci)` - CI/CD related

## Rules

✅ **Do:**
- Use lowercase for type
- Use present tense ("add" not "added")
- Be concise in subject line (<100 chars)
- Add body for complex changes
- Reference issues in footer (`Closes #123`)

❌ **Don't:**
- Use uppercase for type (FEAT, FIX)
- Use past tense
- Include period at end of subject
- Make subject too long

## Testing Your Commit Message

Before committing, test your message:

```bash
# Local validation (if Husky is installed)
npm install
# Then commit normally - it will be validated automatically

# Manual test
echo "feat: my commit message" | npx commitlint
```

## Common Mistakes

| ❌ Wrong | ✅ Correct |
|----------|-----------|
| `Added new feature` | `feat: add new feature` |
| `FEAT: new feature` | `feat: add new feature` |
| `Fix bug.` | `fix: resolve bug` |
| `feat no colon` | `feat: add feature` |
| `Feature: new thing` | `feat: add new thing` |

## Need Help?

- Read the full guide: [CONTRIBUTING.md](../CONTRIBUTING.md)
- Conventional Commits spec: https://www.conventionalcommits.org/
- Ask in PR/issue if unsure!

---

**Remember:** Good commit messages help generate meaningful changelogs and determine correct version numbers automatically!
