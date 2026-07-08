# Phase 4: Create .github/workflows/main-push.yml

## Scope of phase

Create the post-merge workflow that triggers on push to `main`, runs `bin/tag-next-version.sh`, and creates/pushes the next patch version tag.

## Code Organization Reminders

- Keep workflow focused: one responsibility (version tagging)
- Use concurrency to prevent race conditions on rapid merges

## Implementation Details

### 1. Workflow structure
```yaml
name: Main push - Version tag

on:
  push:
    branches:
      - main

concurrency:
  group: main-push-${{ github.ref }}
  cancel-in-progress: false

permissions:
  contents: write   # required to push tags
```

### 2. Job: tag-next-version
- `runs-on: ubuntu-latest`
- Checkout with `fetch-depth: 0` (need full history for tags)
- Set env: `BRANCH: main`, `CI: "true"`
- Run: `bin/tag-next-version.sh`
- Use `actions/checkout@v4` with `fetch-depth: 0`

### 3. Concurrency
- Single workflow run per ref; do not cancel in progress
- Ensures only one tag is created per merge

### 4. Permissions
- `contents: write` - required to push tags to the repo

### 5. Outputs (optional)
- Emit `app_version` for future use (e.g., Docker builds)
- Use `scripts/print-app-version.sh` pattern from monorepo, or extract from tag in same job

For simplicity, this phase can skip outputs; they can be added when Docker publish is implemented.

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt

# Workflow is valid YAML
python3 -c "
import yaml
with open('.github/workflows/main-push.yml') as f:
    yaml.safe_load(f)
" 2>/dev/null || (echo "Install pyyaml or use another validator" && true)

# Required keys present
grep -q "on:" .github/workflows/main-push.yml
grep -q "branches:" .github/workflows/main-push.yml
grep -q "main" .github/workflows/main-push.yml
grep -q "tag-next-version" .github/workflows/main-push.yml
```
