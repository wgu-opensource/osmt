# Phase 2: Update Version Files to Placeholder

## Scope of phase

Update `pom.xml`, `api/pom.xml`, and `ui/package.json` to use `0.0.0-SNAPSHOT` as the placeholder version.

## Code Organization Reminders

- Keep changes minimal and consistent across files
- One concept per change

## Implementation Details

### 1. Parent POM (`pom.xml`)
- Change `<version>3.1.0-SNAPSHOT</version>` to `<version>0.0.0-SNAPSHOT</version>`

### 2. API Module (`api/pom.xml`)
- Change `<version>3.1.0-SNAPSHOT</version>` to `<version>0.0.0-SNAPSHOT</version>`

### 3. UI Package (`ui/package.json`)
- Change `"version": "1.1.1-SNAPSHOT"` to `"version": "0.0.0-SNAPSHOT"`

### 4. Check test module
- If `test/pom.xml` has explicit version, update to `0.0.0-SNAPSHOT`
- If it inherits from parent, no change needed

## Validate

```bash
cd /Users/yona/dev/skybridge/osmt

# Verify placeholder versions
grep -E "<version>|"version":" pom.xml api/pom.xml ui/package.json

# Maven still resolves
sdk env 2>/dev/null || true
mvn help:evaluate -Dexpression=project.version -q -DforceStdout -pl .
mvn help:evaluate -Dexpression=project.version -q -DforceStdout -pl api

# UI builds
cd ui && npm run build-prod 2>/dev/null | tail -5
cd ..
```
