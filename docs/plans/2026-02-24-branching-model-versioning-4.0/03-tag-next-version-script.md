# Phase 3: Create bin/tag-next-version.sh

## Scope of phase

Create `bin/tag-next-version.sh` that reads the latest v4.0.X tag, increments the patch version, and creates/pushes a new git tag. Include version normalization that handles both `v4.0.0` and `4.0.0` formats.

## Code Organization Reminders

- Place more abstract/validation logic first
- Helper functions at the bottom
- One concept per function
- Max 50 executable lines per function
- Max 80 chars per line

## Implementation Details

### 1. Script structure
- Shebang: `#!/usr/bin/env bash`
- `set -euo pipefail`
- JSDoc-style header: purpose, usage, env vars

### 2. Normalize version function
```bash
# Accepts v4.0.0 or 4.0.0, outputs 4.0.0 (no v prefix)
normalize_version() {
  local v="$1"
  v="${v#v}"  # strip leading v if present
  echo "$v"
}
```

### 3. Get next version function
```bash
# Reads latest tag matching v4.0.*, increments patch
# If no tag exists, returns v4.0.0
# Uses: git tag -l 'v4.0.*' | sort -V | tail -1
```

### 4. Validation
- Must run on `main` branch (or BRANCH env if set for CI)
- No uncommitted changes
- Git tag pattern: `v4.0.*` (configurable via MAJOR_MINOR env, default 4.0)

### 5. CI setup
- If `CI=true`, configure git user.email and user.name for commits/tags
- Create tag and push to origin

### 6. Invocation
- Can be run locally (must be on main, clean working tree)
- Called by main-push.yml with `BRANCH=main CI=true`

### 7. Error handling
- Exit 1 if not on main
- Exit 1 if uncommitted changes
- Exit 1 if tag already exists for current commit
- Clear error messages

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt

# Script is executable
test -x bin/tag-next-version.sh

# Dry-run / help if supported, or just syntax check
bash -n bin/tag-next-version.sh

# Test normalize_version (if script exposes it or we add a --test flag)
# For now, syntax check suffices
```
