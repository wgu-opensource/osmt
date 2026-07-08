# Version Consistency Validation

## Current Version Inconsistencies

### Java

- `.sdkmanrc`: `17.0.10-tem`
- `pom.xml`: `17` (major version only)
- `.github/workflows/*.yml`: `17`
- `api/Dockerfile`: `java-17-openjdk` (package name, no specific version)

### Node.js

- `.nvmrc`: `18.18.2`
- `ui/Dockerfile`: `node:18.10-alpine3.15`
- `ui/pom.xml`: `v18.10.0` (frontend-maven-plugin)
- `README.md`: `v18.18.2` (documented requirement)

### Maven

- `.sdkmanrc`: `3.9.6`
- `api/Dockerfile`: `3.9.11` (M2_VERSION)
- `.github/workflows/*.yml`: No version specified (uses default)

### Kotlin

- `.sdkmanrc`: `1.7.21`
- `api/pom.xml`: `1.7.21` ✓ (consistent)

## Tools and Patterns for Version Validation

### 1. Custom Script-Based Validation

Create a validation script that checks version consistency:

**`.github/bin/validate-versions.sh`**

```bash
#!/bin/bash
set -e

# Extract versions from files
JAVA_SDKMANRC=$(grep "^java=" .sdkmanrc | cut -d'=' -f2 | cut -d'-' -f1)
JAVA_POM=$(grep "<java.version>" pom.xml | sed 's/.*<java.version>\(.*\)<\/java.version>.*/\1/')
JAVA_WORKFLOW=$(grep "java-version:" .github/workflows/*.yml | head -1 | sed 's/.*java-version:.*\([0-9]\+\).*/\1/')

NODE_NVMRC=$(cat .nvmrc | tr -d '[:space:]')
NODE_DOCKERFILE=$(grep "FROM node:" ui/Dockerfile | sed 's/.*node:\([0-9.]*\).*/\1/')
NODE_POM=$(grep "<nodeVersion>" ui/pom.xml | sed 's/.*<nodeVersion>v\([0-9.]*\).*/\1/')

MAVEN_SDKMANRC=$(grep "^maven=" .sdkmanrc | cut -d'=' -f2)
MAVEN_DOCKERFILE=$(grep "M2_VERSION=" api/Dockerfile | sed 's/.*M2_VERSION=\([0-9.]*\).*/\1/')

# Validate Java versions
if [ "$JAVA_SDKMANRC" != "$JAVA_POM" ]; then
  echo "ERROR: Java version mismatch: .sdkmanrc=$JAVA_SDKMANRC, pom.xml=$JAVA_POM"
  exit 1
fi

# Validate Node versions (allow minor version differences)
NODE_MAJOR_NVMRC=$(echo $NODE_NVMRC | cut -d'.' -f1-2)
NODE_MAJOR_DOCKERFILE=$(echo $NODE_DOCKERFILE | cut -d'.' -f1-2)
if [ "$NODE_MAJOR_NVMRC" != "$NODE_MAJOR_DOCKERFILE" ]; then
  echo "ERROR: Node major version mismatch: .nvmrc=$NODE_NVMRC, Dockerfile=$NODE_DOCKERFILE"
  exit 1
fi

# Validate Maven versions
if [ "$MAVEN_SDKMANRC" != "$MAVEN_DOCKERFILE" ]; then
  echo "ERROR: Maven version mismatch: .sdkmanrc=$MAVEN_SDKMANRC, Dockerfile=$MAVEN_DOCKERFILE"
  exit 1
fi

echo "✓ All versions are consistent"
```

### 2. GitHub Actions Workflow

Add a validation step to existing workflows:

**`.github/workflows/version-check.yml`**

```yaml
name: Version Consistency Check

on:
  pull_request:
    paths:
      - '.sdkmanrc'
      - '.nvmrc'
      - '**/Dockerfile*'
      - '**/pom.xml'
      - '.github/workflows/*.yml'
      - 'README.md'

jobs:
  validate-versions:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Validate version consistency
        run: |
          .github/bin/validate-versions.sh
```

### 3. Pre-commit Hook

Use pre-commit framework to validate before commits:

**.pre-commit-config.yaml**

```yaml
repos:
  - repo: local
    hooks:
      - id: validate-versions
        name: Validate version consistency
        entry: .github/bin/validate-versions.sh
        language: system
        pass_filenames: false
        always_run: true
```

### 4. YAML-based Version Definition

Create a single source of truth:

**`versions.yml`** (or in `pom.xml` properties)

```yaml
java:
  major: 17
  full: 17.0.10-tem
  
node:
  version: 18.18.2
  docker_tag: 18.18.2-alpine3.15
  
maven:
  version: 3.9.11
  
kotlin:
  version: 1.7.21
```

Then use templating or scripts to inject into files.

### 5. Using Renovate or Dependabot

Configure Renovate to update versions across files:

**`renovate.json`**

```json
{
  "extends": ["config:base"],
  "packageRules": [
    {
      "matchFileNames": [".sdkmanrc", "**/Dockerfile*", ".github/workflows/*.yml"],
      "matchPackageNames": ["java", "maven", "kotlin"],
      "groupName": "Java ecosystem versions"
    },
    {
      "matchFileNames": [".nvmrc", "**/Dockerfile*", "**/pom.xml"],
      "matchPackageNames": ["node"],
      "groupName": "Node.js versions"
    }
  ]
}
```

### 6. Custom GitHub Action

Create a reusable action:

**`.github/actions/version-check/action.yml`**

```yaml
name: 'Version Consistency Check'
description: 'Validates version consistency across configuration files'
inputs:
  java-version:
    description: 'Expected Java version'
    required: true
  node-version:
    description: 'Expected Node.js version'
    required: true
  maven-version:
    description: 'Expected Maven version'
    required: true

runs:
  using: 'composite'
  steps:
    - name: Check Java version
      shell: bash
      run: |
        # Validation logic
    - name: Check Node version
      shell: bash
      run: |
        # Validation logic
```

### 7. Using Makefile or Task Runner

**`Makefile`**

```makefile
.PHONY: check-versions

check-versions:
	@echo "Checking version consistency..."
	@.github/bin/validate-versions.sh

validate-pr: check-versions
	@echo "Running PR validation..."
```

### 8. JSON Schema Validation

For structured validation:

**`version-schema.json`**

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "java": {
      "type": "object",
      "properties": {
        "major": { "type": "string", "pattern": "^17$" },
        "full": { "type": "string", "pattern": "^17\\.0\\.10" }
      }
    },
    "node": {
      "type": "object",
      "properties": {
        "version": { "type": "string", "pattern": "^18\\.18\\.2$" }
      }
    }
  }
}
```

## Recommended Approach

For this project, I recommend:

1. **Immediate**: Create `.github/bin/validate-versions.sh` script
2. **Short-term**: Add version check to GitHub Actions workflows
3. **Long-term**: Consider centralizing versions in `pom.xml` properties and using Maven filtering/templating for Dockerfiles

### Implementation Priority

1. ✅ Fix current inconsistencies first
2. ✅ Add validation script
3. ✅ Add GitHub Actions workflow
4. ⚠️ Consider pre-commit hook (optional, may slow down commits)
5. ⚠️ Consider Renovate for automated updates (requires maintenance)

## Example: Complete Validation Script

See `.github/bin/validate-versions.sh` for a complete implementation.

