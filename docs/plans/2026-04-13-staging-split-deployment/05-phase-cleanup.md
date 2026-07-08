# Phase 5: Cleanup and Documentation

## Scope of Phase

Final cleanup and documentation for the staging split deployment.

## Cleanup Checklist

### 1. Remove TODO Comments

Search for any TODOs added during implementation:

```bash
cd /Users/yona/dev/skybridge/infra
rg "TODO.*split.*deploy" environments/staging/
rg "TODO" osmt/infra/aws/terraform/module/
```

### 2. Verify No Debug Logging

Check for debug prints or verbose logging:

```bash
# No stdout debug statements in Terraform
# No verbose logging enabled in ECS
```

### 3. Documentation Updates

**Update staging environment README**:

File: `/Users/yona/dev/skybridge/infra/environments/staging/README.md`

Add section:

```markdown
## OSMT Split Deployment

Staging runs two OSMT instances:

- **Public (Read-Only)**: https://osmt.staging.prettygoodskills.com
  - No authentication required
  - Can browse skills and collections
  - Writes blocked at application and database level
  - Blue branding (`#1e40af`)

- **Staff (Writable)**: https://osmt-staff.staging.prettygoodskills.com
  - Google OAuth2 authentication
  - Full CRUD operations
  - Orange branding (`#e65100`) for visual distinction

Both share:
- Same RDS MySQL database (`osmt_db`)
- Same ElastiCache Redis
- Same Elasticsearch (sidecar)
- Different DB users (master vs osmt_ro)

### Architecture

Single ECS task with two containers:
- Port 8080: public container (`readonly` profile)
- Port 8081: staff container (`oauth2` profile)

ALB routes by Host header to different target groups.
```

### 4. Add Summary to Plan

**File**: `docs/plans/2026-04-13-staging-split-deployment/summary.md`

```markdown
# Staging Split Deployment Summary

## Completed Work

Deployed OSMT split deployment to staging with two instances sharing infrastructure:

### Infrastructure Changes
- Added Route53 record for `osmt-staff.staging.prettygoodskills.com`
- Added second ALB target group (port 8081)
- Added host-based ALB listener rules
- Updated ECS task definition with two containers
- Bumped task size: 1024 CPU / 2048 Memory
- Created `osmt_ro` database user with SELECT-only permissions

### Container Configuration

**Public Container (osmt-public)**:
- Port: 8080
- Profile: `apiserver,readonly`
- DB User: `osmt_ro` (SELECT-only)
- Auth: None
- Brand Color: `#1e40af` (blue)
- Domain: osmt.staging.prettygoodskills.com

**Staff Container (osmt-staff)**:
- Port: 8081
- Profile: `apiserver,oauth2`
- DB User: master (full permissions)
- Auth: Google OAuth2
- Brand Color: `#e65100` (orange)
- Domain: osmt-staff.staging.prettygoodskills.com

### URLs
- Public: https://osmt.staging.prettygoodskills.com
- Staff: https://osmt-staff.staging.prettygoodskills.com

### Security
- Defense in depth: DB-level read-only user
- Application-level write blocking via Spring Security
- Separate secrets for each DB user in SSM Parameter Store
```

## Move Plan to Done

After verification:

```bash
mkdir -p docs/plans-done
cp -r docs/plans/2026-04-13-staging-split-deployment docs/plans-done/
```

## Commit

Commit the infrastructure changes with conventional commit message:

```
feat(infra): split deployment for osmt staging

- Add second OSMT container to ECS task (public + staff)
- Add osmt-staff.staging.prettygoodskills.com domain
- Add ALB host-based routing to different target groups
- Create read-only DB user (osmt_ro) for public instance
- Configure orange branding for staff instance
- Keep existing data shared between both instances
```
