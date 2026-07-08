# Phase 1: Create docs/versioning.md

## Scope of phase

Create `docs/versioning.md` documenting the OSMT versioning strategy, placeholder versions, tag format, and version increment behavior.

## Code Organization Reminders

- Place more abstract concepts first in the document
- Keep related sections grouped together
- Be concise and factual

## Implementation Details

Create `docs/versioning.md` with the following content structure:

### 1. Overview
- Versioning is driven by git tags; source files use placeholders
- Not real semver (no downstream consumers)
- Time-based versioning preferred but deferred

### 2. Version Format
- Semantic version format: `MAJOR.MINOR.PATCH` (e.g., 4.0.0)
- Git tags use `v` prefix: `v4.0.0`, `v4.0.1`
- Scripts must normalize to handle both `v4.0.0` and `4.0.0`

### 3. Placeholder Versions
- Version files (pom.xml, ui/package.json) use `0.0.0-SNAPSHOT`
- Placeholders indicate version is set at publish/build time
- Real version comes from git tags

### 4. Version Increments
- **Patch**: Automatic on each merge to `main` (4.0.0 → 4.0.1 → 4.0.2)
- **Minor**: Manual, done as needed
- **Major**: Manual, done as needed

### 5. Docker/Build
- Docker image tags and builds use version from git tags
- Scripts should extract version from current git tag

### 6. References
- Link to CONTRIBUTING.md for branching workflow
- Mention `bin/tag-next-version.sh` for automated tagging

## Validate

```bash
# Verify file exists and has expected sections
grep -E "^(## |### )" docs/versioning.md

# Verify no broken internal links (if any)
cd /Users/yona/dev/skybridge/osmt && head -100 docs/versioning.md
```
