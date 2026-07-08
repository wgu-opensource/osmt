#!/bin/bash
# Version consistency validation script
# Validates that versions match across .sdkmanrc, .nvmrc, Dockerfiles, pom.xml, and GitHub workflows
# Usage: validate-versions.sh [--fix]

set -euo pipefail

# Parse arguments
FIX_MODE=false
if [[ "${1:-}" == "--fix" ]]; then
  FIX_MODE=true
fi

# GitHub Actions output helpers
if [ -n "${GITHUB_ACTIONS:-}" ]; then
  GITHUB_OUTPUT="${GITHUB_STEP_SUMMARY:-}"
  GITHUB_ANNOTATIONS=true
else
  GITHUB_OUTPUT=""
  GITHUB_ANNOTATIONS=false
fi

github_notice() {
  local file="$1"
  local line="${2:-}"
  local title="$3"
  local message="$4"
  if [ "$GITHUB_ANNOTATIONS" = true ]; then
    if [ -n "$line" ]; then
      echo "::notice file=$file,line=$line,title=$title::$message" >&2
    else
      echo "::notice file=$file,title=$title::$message" >&2
    fi
  fi
}

github_warning() {
  local file="$1"
  local line="${2:-}"
  local title="$3"
  local message="$4"
  if [ "$GITHUB_ANNOTATIONS" = true ]; then
    if [ -n "$line" ]; then
      echo "::warning file=$file,line=$line,title=$title::$message" >&2
    else
      echo "::warning file=$file,title=$title::$message" >&2
    fi
  fi
}

github_error() {
  local file="$1"
  local line="${2:-}"
  local title="$3"
  local message="$4"
  if [ "$GITHUB_ANNOTATIONS" = true ]; then
    if [ -n "$line" ]; then
      echo "::error file=$file,line=$line,title=$title::$message" >&2
    else
      echo "::error file=$file,title=$title::$message" >&2
    fi
  fi
}

# Colors for output (when not in GitHub Actions)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

ERROR_COUNT=0
WARNING_COUNT=0
FIX_COUNT=0

# Helper functions
error() {
  local msg="$1"
  local file="${2:-}"
  local line="${3:-}"
  echo -e "${RED}ERROR:${NC} $msg" >&2
  if [ -n "$file" ]; then
    github_error "$file" "$line" "Version Mismatch" "$msg"
  fi
  ERROR_COUNT=$((ERROR_COUNT + 1)) || true
}

warning() {
  local msg="$1"
  local file="${2:-}"
  echo -e "${YELLOW}WARNING:${NC} $msg" >&2
  if [ -n "$file" ]; then
    github_warning "$file" "" "Version Warning" "$msg"
  fi
  WARNING_COUNT=$((WARNING_COUNT + 1)) || true
}

info() {
  local msg="$1"
  echo -e "${GREEN}INFO:${NC} $msg"
}

fix_info() {
  local msg="$1"
  echo -e "${BLUE}FIX:${NC} $msg"
}

# Extract Java version from .sdkmanrc (format: 17.0.10-tem -> 17)
extract_java_major() {
  local version="$1"
  echo "$version" | cut -d'.' -f1
}

# Extract Node major.minor version (format: 18.18.2 -> 18.18)
extract_node_major_minor() {
  local version="$1"
  echo "$version" | cut -d'.' -f1-2
}

# Extract version number from Dockerfile node tag (format: 18.10-alpine3.15 -> 18.10)
extract_node_from_docker() {
  local tag="$1"
  echo "$tag" | cut -d'-' -f1
}

# Fix functions
fix_java_pom() {
  local target_version="$1"
  if [ -f "pom.xml" ]; then
    local temp_file=$(mktemp)
    sed -E "s/(<java\.version>)[^<]*(<\/java\.version>)/\1${target_version}\2/" pom.xml > "$temp_file"
    mv "$temp_file" pom.xml
    fix_info "Updated pom.xml: java.version -> $target_version"
    FIX_COUNT=$((FIX_COUNT + 1)) || true
  fi
}

fix_java_workflows() {
  local target_version="$1"
  local workflow_files=$(find .github/workflows -name "*.yml" -type f 2>/dev/null || true)
  for file in $workflow_files; do
    if grep -q "java-version:" "$file" 2>/dev/null; then
      local temp_file=$(mktemp)
      sed -E "s/(java-version:[[:space:]]*['\"])[0-9]+(['\"])/\1${target_version}\2/" "$file" > "$temp_file"
      mv "$temp_file" "$file"
      fix_info "Updated $file: java-version -> $target_version"
      FIX_COUNT=$((FIX_COUNT + 1)) || true
    fi
  done
}

fix_node_dockerfile() {
  local target_version="$1"
  if [ -f "ui/Dockerfile" ]; then
    local current_tag=$(grep "FROM node:" ui/Dockerfile | sed 's/.*node:\([^ ]*\).*/\1/' | head -1)
    local base_image=$(echo "$current_tag" | cut -d'-' -f2-)
    if [ -z "$base_image" ] || [ "$base_image" = "$current_tag" ]; then
      # No base image specified, use alpine
      base_image="alpine3.15"
    fi
    local new_tag="${target_version}-${base_image}"
    local temp_file=$(mktemp)
    sed "s|FROM node:.*|FROM node:${new_tag} AS build|" ui/Dockerfile > "$temp_file"
    mv "$temp_file" ui/Dockerfile
    fix_info "Updated ui/Dockerfile: node -> $new_tag"
    FIX_COUNT=$((FIX_COUNT + 1)) || true
  fi
}

fix_node_pom() {
  local target_version="$1"
  if [ -f "ui/pom.xml" ]; then
    local temp_file=$(mktemp)
    sed -E "s/(<nodeVersion>v?)[^<]*(<\/nodeVersion>)/\1${target_version}\2/" ui/pom.xml > "$temp_file"
    mv "$temp_file" ui/pom.xml
    fix_info "Updated ui/pom.xml: nodeVersion -> v${target_version}"
    FIX_COUNT=$((FIX_COUNT + 1)) || true
  fi
}

fix_maven_dockerfile() {
  local target_version="$1"
  if [ -f "api/Dockerfile" ]; then
    local temp_file=$(mktemp)
    sed -E "s/(ENV M2_VERSION=)[0-9.]+/\1${target_version}/" api/Dockerfile > "$temp_file"
    mv "$temp_file" api/Dockerfile
    fix_info "Updated api/Dockerfile: M2_VERSION -> $target_version"
    FIX_COUNT=$((FIX_COUNT + 1)) || true
  fi
}

fix_kotlin_pom() {
  local target_version="$1"
  if [ -f "api/pom.xml" ]; then
    local temp_file=$(mktemp)
    sed -E "s/(<kotlin\.version>)[^<]*(<\/kotlin\.version>)/\1${target_version}\2/" api/pom.xml > "$temp_file"
    mv "$temp_file" api/pom.xml
    fix_info "Updated api/pom.xml: kotlin.version -> $target_version"
    FIX_COUNT=$((FIX_COUNT + 1)) || true
  fi
}

fix_kotlin_vscode_settings() {
  local target_version="$1"
  if [ -f ".vscode/settings.json" ]; then
    local temp_file=$(mktemp)
    sed -E "s/(\"kotlin\.compiler\.jvm\.target\":[[:space:]]*\")([0-9]+)(\")/\1${target_version}\3/" .vscode/settings.json > "$temp_file"
    mv "$temp_file" .vscode/settings.json
    fix_info "Updated .vscode/settings.json: kotlin.compiler.jvm.target -> $target_version"
    FIX_COUNT=$((FIX_COUNT + 1)) || true
  fi
}

# Main validation logic
if [ "$FIX_MODE" = false ]; then
  echo "## 🔍 Version Consistency Validation" >&2
  if [ -n "$GITHUB_OUTPUT" ]; then
    echo "## 🔍 Version Consistency Validation" >> "$GITHUB_OUTPUT"
  fi
  echo "" >&2
else
  echo "## 🔧 Fixing Version Inconsistencies" >&2
  if [ -n "$GITHUB_OUTPUT" ]; then
    echo "## 🔧 Fixing Version Inconsistencies" >> "$GITHUB_OUTPUT"
  fi
  echo "" >&2
  echo "Using .sdkmanrc and .nvmrc as source of truth..." >&2
  echo "" >&2
fi

# Check if required files exist
if [ ! -f ".sdkmanrc" ]; then
  error ".sdkmanrc file not found"
  exit 1
fi

if [ ! -f ".nvmrc" ]; then
  error ".nvmrc file not found"
  exit 1
fi

# Extract versions from .sdkmanrc (source of truth)
JAVA_SDKMANRC=$(grep "^java=" .sdkmanrc | cut -d'=' -f2 | cut -d'-' -f1 || echo "")
JAVA_MAJOR_SDKMANRC=$(extract_java_major "$JAVA_SDKMANRC")
MAVEN_SDKMANRC=$(grep "^maven=" .sdkmanrc | cut -d'=' -f2 || echo "")
KOTLIN_SDKMANRC=$(grep "^kotlin=" .sdkmanrc | cut -d'=' -f2 || echo "")

# Extract versions from .nvmrc (source of truth)
NODE_NVMRC=$(cat .nvmrc | tr -d '[:space:]' || echo "")
NODE_MAJOR_MINOR_NVMRC=$(extract_node_major_minor "$NODE_NVMRC")

# Extract current versions from other files
if [ -f "pom.xml" ]; then
  JAVA_POM=$(grep "<java.version>" pom.xml | sed -E 's/.*<java\.version>([^<]+)<\/java\.version>.*/\1/' | head -1 || echo "")
  if [ -z "$JAVA_POM" ]; then
    warning "Could not extract Java version from pom.xml"
  fi
else
  warning "pom.xml not found"
  JAVA_POM=""
fi

if [ -f "ui/Dockerfile" ]; then
  NODE_DOCKERFILE_TAG=$(grep "FROM node:" ui/Dockerfile | sed 's/.*node:\([^ ]*\).*/\1/' | head -1 || echo "")
  NODE_DOCKERFILE=$(extract_node_from_docker "$NODE_DOCKERFILE_TAG")
  NODE_MAJOR_MINOR_DOCKERFILE=$(extract_node_major_minor "$NODE_DOCKERFILE")
else
  warning "ui/Dockerfile not found"
  NODE_DOCKERFILE=""
  NODE_MAJOR_MINOR_DOCKERFILE=""
fi

if [ -f "ui/pom.xml" ]; then
  NODE_POM=$(grep "<nodeVersion>" ui/pom.xml | sed -E 's/.*<nodeVersion>v?([^<]+)<\/nodeVersion>.*/\1/' | head -1 || echo "")
  NODE_MAJOR_MINOR_POM=$(extract_node_major_minor "$NODE_POM")
else
  warning "ui/pom.xml not found"
  NODE_POM=""
  NODE_MAJOR_MINOR_POM=""
fi

if [ -f "api/Dockerfile" ]; then
  MAVEN_DOCKERFILE=$(grep "M2_VERSION=" api/Dockerfile | sed 's/.*M2_VERSION=\([0-9.]*\).*/\1/' | head -1 || echo "")
else
  warning "api/Dockerfile not found"
  MAVEN_DOCKERFILE=""
fi

JAVA_WORKFLOWS=""
if [ -d ".github/workflows" ]; then
  JAVA_WORKFLOWS=$(grep -h "java-version:" .github/workflows/*.yml 2>/dev/null | sed -E "s/.*java-version:[[:space:]]*['\"]([0-9]+)['\"].*/\1/" | head -1 || echo "")
  if [ -z "$JAVA_WORKFLOWS" ]; then
    JAVA_WORKFLOWS=$(grep -h "java-version:" .github/workflows/*.yml 2>/dev/null | sed -E "s/.*java-version:[[:space:]]*([0-9]+).*/\1/" | head -1 || echo "")
  fi
fi

if [ -f "api/pom.xml" ]; then
  KOTLIN_POM=$(grep "<kotlin.version>" api/pom.xml | sed -E 's/.*<kotlin\.version>([^<]+)<\/kotlin\.version>.*/\1/' | head -1 || echo "")
else
  warning "api/pom.xml not found"
  KOTLIN_POM=""
fi

if [ -f ".vscode/settings.json" ]; then
  KOTLIN_VSCODE_TARGET=$(grep "\"kotlin.compiler.jvm.target\"" .vscode/settings.json | sed -E 's/.*"kotlin\.compiler\.jvm\.target":[[:space:]]*"([0-9]+)".*/\1/' | head -1 || echo "")
else
  KOTLIN_VSCODE_TARGET=""
fi

# Build markdown output for GitHub
if [ "$FIX_MODE" = false ]; then
  {
    echo "### Detected Versions" >&2
    echo "" >&2
    echo "| Tool | Source File | Version |" >&2
    echo "|------|-------------|---------|" >&2
    echo "| **Java** | .sdkmanrc | \`$JAVA_SDKMANRC\` (major: \`$JAVA_MAJOR_SDKMANRC\`) |" >&2
    [ -n "$JAVA_POM" ] && echo "| | pom.xml | \`$JAVA_POM\` |" >&2
    [ -n "$JAVA_WORKFLOWS" ] && echo "| | GitHub workflows | \`$JAVA_WORKFLOWS\` |" >&2
    echo "| **Node.js** | .nvmrc | \`$NODE_NVMRC\` (major.minor: \`$NODE_MAJOR_MINOR_NVMRC\`) |" >&2
    [ -n "$NODE_DOCKERFILE" ] && echo "| | ui/Dockerfile | \`$NODE_DOCKERFILE\` (major.minor: \`$NODE_MAJOR_MINOR_DOCKERFILE\`) |" >&2
    [ -n "$NODE_POM" ] && echo "| | ui/pom.xml | \`$NODE_POM\` (major.minor: \`$NODE_MAJOR_MINOR_POM\`) |" >&2
    echo "| **Maven** | .sdkmanrc | \`$MAVEN_SDKMANRC\` |" >&2
    [ -n "$MAVEN_DOCKERFILE" ] && echo "| | api/Dockerfile | \`$MAVEN_DOCKERFILE\` |" >&2
    echo "| **Kotlin** | .sdkmanrc | \`$KOTLIN_SDKMANRC\` |" >&2
    [ -n "$KOTLIN_POM" ] && echo "| | api/pom.xml | \`$KOTLIN_POM\` |" >&2
    [ -n "$KOTLIN_VSCODE_TARGET" ] && echo "| | .vscode/settings.json (JVM target) | \`$KOTLIN_VSCODE_TARGET\` |" >&2
    echo "" >&2
  }
  if [ -n "$GITHUB_OUTPUT" ]; then
    cat > "$GITHUB_OUTPUT" <<EOF
### Detected Versions

| Tool | Source File | Version |
|------|-------------|---------|
| **Java** | .sdkmanrc | \`$JAVA_SDKMANRC\` (major: \`$JAVA_MAJOR_SDKMANRC\`) |
EOF
    [ -n "$JAVA_POM" ] && echo "| | pom.xml | \`$JAVA_POM\` |" >> "$GITHUB_OUTPUT"
    [ -n "$JAVA_WORKFLOWS" ] && echo "| | GitHub workflows | \`$JAVA_WORKFLOWS\` |" >> "$GITHUB_OUTPUT"
    echo "| **Node.js** | .nvmrc | \`$NODE_NVMRC\` (major.minor: \`$NODE_MAJOR_MINOR_NVMRC\`) |" >> "$GITHUB_OUTPUT"
    [ -n "$NODE_DOCKERFILE" ] && echo "| | ui/Dockerfile | \`$NODE_DOCKERFILE\` (major.minor: \`$NODE_MAJOR_MINOR_DOCKERFILE\`) |" >> "$GITHUB_OUTPUT"
    [ -n "$NODE_POM" ] && echo "| | ui/pom.xml | \`$NODE_POM\` (major.minor: \`$NODE_MAJOR_MINOR_POM\`) |" >> "$GITHUB_OUTPUT"
    echo "| **Maven** | .sdkmanrc | \`$MAVEN_SDKMANRC\` |" >> "$GITHUB_OUTPUT"
    [ -n "$MAVEN_DOCKERFILE" ] && echo "| | api/Dockerfile | \`$MAVEN_DOCKERFILE\` |" >> "$GITHUB_OUTPUT"
    echo "| **Kotlin** | .sdkmanrc | \`$KOTLIN_SDKMANRC\` |" >> "$GITHUB_OUTPUT"
    [ -n "$KOTLIN_POM" ] && echo "| | api/pom.xml | \`$KOTLIN_POM\` |" >> "$GITHUB_OUTPUT"
    [ -n "$KOTLIN_VSCODE_TARGET" ] && echo "| | .vscode/settings.json (JVM target) | \`$KOTLIN_VSCODE_TARGET\` |" >> "$GITHUB_OUTPUT"
    echo "" >> "$GITHUB_OUTPUT"
  fi
fi

# Validation and fixing
echo "" >&2
echo "### Validation Results" >&2
if [ -n "$GITHUB_OUTPUT" ]; then
  echo "" >> "$GITHUB_OUTPUT"
  echo "### Validation Results" >> "$GITHUB_OUTPUT"
fi

# Validate/Fix Java versions
if [ -n "$JAVA_POM" ] && [ "$JAVA_MAJOR_SDKMANRC" != "$JAVA_POM" ]; then
  if [ "$FIX_MODE" = true ]; then
    fix_java_pom "$JAVA_MAJOR_SDKMANRC"
  else
    error "Java major version mismatch: .sdkmanrc=$JAVA_MAJOR_SDKMANRC, pom.xml=$JAVA_POM" "pom.xml"
  fi
fi

if [ -n "$JAVA_WORKFLOWS" ] && [ "$JAVA_MAJOR_SDKMANRC" != "$JAVA_WORKFLOWS" ]; then
  if [ "$FIX_MODE" = true ]; then
    fix_java_workflows "$JAVA_MAJOR_SDKMANRC"
  else
    error "Java major version mismatch: .sdkmanrc=$JAVA_MAJOR_SDKMANRC, GitHub workflows=$JAVA_WORKFLOWS" ".github/workflows"
  fi
fi

# Validate/Fix Node.js versions
if [ -n "$NODE_DOCKERFILE" ] && [ "$NODE_MAJOR_MINOR_NVMRC" != "$NODE_MAJOR_MINOR_DOCKERFILE" ]; then
  if [ "$FIX_MODE" = true ]; then
    fix_node_dockerfile "$NODE_MAJOR_MINOR_NVMRC"
  else
    error "Node.js major.minor version mismatch: .nvmrc=$NODE_NVMRC ($NODE_MAJOR_MINOR_NVMRC), ui/Dockerfile=$NODE_DOCKERFILE ($NODE_MAJOR_MINOR_DOCKERFILE)" "ui/Dockerfile"
  fi
fi

if [ -n "$NODE_POM" ] && [ "$NODE_MAJOR_MINOR_NVMRC" != "$NODE_MAJOR_MINOR_POM" ]; then
  if [ "$FIX_MODE" = true ]; then
    fix_node_pom "$NODE_MAJOR_MINOR_NVMRC"
  else
    error "Node.js major.minor version mismatch: .nvmrc=$NODE_NVMRC ($NODE_MAJOR_MINOR_NVMRC), ui/pom.xml=$NODE_POM ($NODE_MAJOR_MINOR_POM)" "ui/pom.xml"
  fi
fi

# Validate/Fix Maven versions
if [ -n "$MAVEN_DOCKERFILE" ] && [ "$MAVEN_SDKMANRC" != "$MAVEN_DOCKERFILE" ]; then
  if [ "$FIX_MODE" = true ]; then
    fix_maven_dockerfile "$MAVEN_SDKMANRC"
  else
    error "Maven version mismatch: .sdkmanrc=$MAVEN_SDKMANRC, api/Dockerfile=$MAVEN_DOCKERFILE" "api/Dockerfile"
  fi
fi

# Validate/Fix Kotlin versions
if [ -n "$KOTLIN_POM" ] && [ "$KOTLIN_SDKMANRC" != "$KOTLIN_POM" ]; then
  if [ "$FIX_MODE" = true ]; then
    fix_kotlin_pom "$KOTLIN_SDKMANRC"
  else
    error "Kotlin version mismatch: .sdkmanrc=$KOTLIN_SDKMANRC, api/pom.xml=$KOTLIN_POM" "api/pom.xml"
  fi
fi

# Validate/Fix Kotlin JVM target in VS Code settings
if [ -n "$KOTLIN_VSCODE_TARGET" ] && [ "$JAVA_MAJOR_SDKMANRC" != "$KOTLIN_VSCODE_TARGET" ]; then
  if [ "$FIX_MODE" = true ]; then
    fix_kotlin_vscode_settings "$JAVA_MAJOR_SDKMANRC"
  else
    error "Kotlin JVM target mismatch: .sdkmanrc Java major=$JAVA_MAJOR_SDKMANRC, .vscode/settings.json kotlin.compiler.jvm.target=$KOTLIN_VSCODE_TARGET" ".vscode/settings.json" "5"
  fi
fi

# Summary
echo "" >&2
echo "---" >&2
if [ "$FIX_MODE" = true ]; then
  if [ $FIX_COUNT -gt 0 ]; then
    echo "✅ **Fixed $FIX_COUNT version inconsistency(ies)**" >&2
    if [ -n "$GITHUB_OUTPUT" ]; then
      echo "" >> "$GITHUB_OUTPUT"
      echo "---" >> "$GITHUB_OUTPUT"
      echo "✅ **Fixed $FIX_COUNT version inconsistency(ies)**" >> "$GITHUB_OUTPUT"
    fi
    exit 0
  else
    echo "✅ **No fixes needed - all versions are consistent!**" >&2
    if [ -n "$GITHUB_OUTPUT" ]; then
      echo "" >> "$GITHUB_OUTPUT"
      echo "---" >> "$GITHUB_OUTPUT"
      echo "✅ **No fixes needed - all versions are consistent!**" >> "$GITHUB_OUTPUT"
    fi
    exit 0
  fi
else
  if [ $ERROR_COUNT -eq 0 ] && [ $WARNING_COUNT -eq 0 ]; then
    echo "✅ **All versions are consistent!**" >&2
    if [ -n "$GITHUB_OUTPUT" ]; then
      echo "" >> "$GITHUB_OUTPUT"
      echo "---" >> "$GITHUB_OUTPUT"
      echo "✅ **All versions are consistent!**" >> "$GITHUB_OUTPUT"
    fi
    exit 0
  elif [ $ERROR_COUNT -eq 0 ]; then
    echo "⚠️ **Versions are consistent, but $WARNING_COUNT warning(s) were found**" >&2
    if [ -n "$GITHUB_OUTPUT" ]; then
      echo "" >> "$GITHUB_OUTPUT"
      echo "---" >> "$GITHUB_OUTPUT"
      echo "⚠️ **Versions are consistent, but $WARNING_COUNT warning(s) were found**" >> "$GITHUB_OUTPUT"
    fi
    exit 0
  else
    echo "❌ **Found $ERROR_COUNT error(s) and $WARNING_COUNT warning(s)**" >&2
    echo "" >&2
    echo "💡 **To automatically fix these issues, run:**" >&2
    echo "   \`\`\`bash" >&2
    echo "   ./scripts/validate-versions.sh --fix" >&2
    echo "   \`\`\`" >&2
    if [ -n "$GITHUB_OUTPUT" ]; then
      echo "" >> "$GITHUB_OUTPUT"
      echo "---" >> "$GITHUB_OUTPUT"
      echo "❌ **Found $ERROR_COUNT error(s) and $WARNING_COUNT warning(s)**" >> "$GITHUB_OUTPUT"
      echo "" >> "$GITHUB_OUTPUT"
      echo "💡 **To automatically fix these issues, run:**" >> "$GITHUB_OUTPUT"
      echo "\`\`\`bash" >> "$GITHUB_OUTPUT"
      echo "./scripts/validate-versions.sh --fix" >> "$GITHUB_OUTPUT"
      echo "\`\`\`" >> "$GITHUB_OUTPUT"
    fi
    exit 1
  fi
fi
