# Phase 4: Deploy and Verify

## Scope of Phase

Deploy the new OSMT version with split deployment support and verify both instances work correctly.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later

## Implementation Details

### 1. Build and Push New OSMT Image

Ensure the OSMT image includes the split deployment code:

```bash
cd /Users/yona/dev/skybridge/feature/osmt-monorepo/wrappers/osmt

# Check VERSION file has the split deployment commit
# Update if needed

# Build and push
./build.sh  # or make build
```

### 2. Deploy via Terraform

```bash
cd /Users/yona/dev/skybridge/infra/environments/staging

# Update terraform.tfvars.json with:
# - New container config
# - Read-only DB password reference
# - Updated task CPU/memory

terraform init
terraform plan
terraform apply
```

### 3. Verify Deployment

#### Check Public Instance (Read-Only)

```bash
# DNS resolves
dig osmt.staging.prettygoodskills.com

# HTTPS works
curl -s https://osmt.staging.prettygoodskills.com/health

# Whitelabel shows read-only
curl -s https://osmt.staging.prettygoodskills.com/whitelabel/whitelabel.json | jq

# Expected:
# {
#   "instanceType": "read-only",
#   "writableInstanceUrl": "https://osmt-staff.staging.prettygoodskills.com",
#   "authMode": "read-only",
#   "colorBrandAccent1": "#1e40af"
# }

# Login page shows read-only message
curl -s https://osmt.staging.prettygoodskills.com/login | grep -i "public skill browser"

# No login button in UI (check manually)

# Write operations blocked
curl -s -X POST https://osmt.staging.prettygoodskills.com/api/v3/skills \
  -H "Content-Type: application/json" \
  -d '{}'
# Expected: 403 Forbidden with "read-only" message
```

#### Check Staff Instance (Writable)

```bash
# DNS resolves
dig osmt-staff.staging.prettygoodskills.com

# HTTPS works
curl -s https://osmt-staff.staging.prettygoodskills.com/health

# Whitelabel shows writable
curl -s https://osmt-staff.staging.prettygoodskills.com/whitelabel/whitelabel.json | jq

# Expected:
# {
#   "instanceType": "writable",
#   "authMode": "oauth2",
#   "authProviders": [...],
#   "colorBrandAccent1": "#e65100",
#   "authoringWelcomeMessage": "Staff authoring instance"
# }

# Login page shows OAuth and/or single-auth
curl -s https://osmt-staff.staging.prettygoodskills.com/login | grep -i "sign in"

# Visual check: orange branding (manual)

# Can authenticate (manual test with Google OAuth)
```

#### Cross-Instance Verification

1. **Create skill on staff instance** (via UI or API with auth)
2. **Verify it appears on public instance** immediately (same DB)
3. **Verify public cannot edit** (403 error)
4. **Verify staff can edit** (200 OK)

### 4. Test Database Permissions

```bash
# Connect as read-only user (from ECS task or locally)
mysql -h <rds-endpoint> -u osmt_ro -p -e "SELECT COUNT(*) FROM skills;"
# Should work

mysql -h <rds-endpoint> -u osmt_ro -p -e "INSERT INTO skills (id) VALUES (99999);"
# Should fail: ERROR 1142 (42000): INSERT command denied
```

## Validate Commands Summary

```bash
# Full validation script
#!/bin/bash
set -e

PUBLIC_URL="https://osmt.staging.prettygoodskills.com"
STAFF_URL="https://osmt-staff.staging.prettygoodskills.com"

echo "=== Public Instance (Read-Only) ==="
echo "Health check:"
curl -sf "${PUBLIC_URL}/health" || exit 1

echo "Instance type:"
curl -sf "${PUBLIC_URL}/whitelabel/whitelabel.json" | jq '.instanceType'

echo "Write blocked (expect 403):"
curl -sf -X POST "${PUBLIC_URL}/api/v3/skills" -d '{}' && exit 1 || echo "✓ Write blocked"

echo ""
echo "=== Staff Instance (Writable) ==="
echo "Health check:"
curl -sf "${STAFF_URL}/health" || exit 1

echo "Instance type:"
curl -sf "${STAFF_URL}/whitelabel/whitelabel.json" | jq '.instanceType'

echo "Auth configured:"
curl -sf "${STAFF_URL}/whitelabel/whitelabel.json" | jq '.authMode, .authProviders | length'

echo ""
echo "=== All Checks Passed ==="
```

## Rollback Plan

If issues occur:

1. **Revert to single container**:
   ```bash
   terraform apply -var='enable_split_deployment=false'
   ```

2. **Or rollback to previous image version**:
   ```bash
   # Update task definition with previous image tag
   # Force new deployment
   ```

3. **Database is unchanged** - both instances share same DB, so no data migration needed
