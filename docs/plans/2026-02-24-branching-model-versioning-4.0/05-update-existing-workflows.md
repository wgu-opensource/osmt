# Phase 5: Update Existing Workflows

## Scope of phase

Update `.github/workflows/ci.yml` and `.github/workflows/trigger-monorepo-version-bump.yml` to use `main` instead of `develop`.

## Code Organization Reminders

- Minimal, targeted changes
- Preserve existing structure and conditions

## Implementation Details

### 1. CI workflow (`.github/workflows/ci.yml`)

Current:
```yaml
on:
  pull_request:
    branches:
      - "develop"
      - "feature/*"
      - "renovate/*"
```

Change to:
```yaml
on:
  pull_request:
    branches:
      - "main"
      - "feature/*"
      - "renovate/*"
```

### 2. Monorepo trigger (`.github/workflows/trigger-monorepo-version-bump.yml`)

Current:
```yaml
on:
  push:
    branches:
      - develop
```

Change to:
```yaml
on:
  push:
    branches:
      - main
```

### 3. No other workflow changes
- Do not modify job steps, conditions, or other triggers
- Only change branch names

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt

# CI references main
grep -A3 "branches:" .github/workflows/ci.yml | grep -q main

# Monorepo trigger references main
grep -A3 "branches:" .github/workflows/trigger-monorepo-version-bump.yml | grep -q main

# No references to develop in workflow triggers
grep -r "develop" .github/workflows/ || true  # Should show no workflow trigger for develop
```
