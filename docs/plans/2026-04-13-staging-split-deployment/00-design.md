# Staging Split Deployment Design

## Scope of Work

Deploy OSMT split deployment to staging: public read-only instance at existing domain, new writable staff instance at new domain, sharing the same ECS task, ALB, and RDS.

## File Structure

```
/Users/yona/dev/skybridge/infra/
├── environments/staging/
│   ├── main.tf                    # UPDATE: Second OSMT container + tg
│   ├── variables.tf               # UPDATE: Add staff domain
│   └── terraform.tfvars.json      # UPDATE: Read-only DB creds, branding
│
/Users/yona/dev/skybridge/feature/osmt-monorepo/
└── wrappers/osmt/
    ├── Dockerfile                 # UPDATE: Support dual-container task
    ├── init_osmt.sh               # UPDATE: Init script per instance
    └── task-def.tpl.json          # NEW: Dual-container task template

/Users/yona/dev/skybridge/osmt/
└── docs/plans/2026-04-13-staging-split-deployment/
    └── db-user-setup.sql          # NEW: Create osmt_ro user
```

## Conceptual Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                         Staging Environment                             │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                     Route53 (prettygoodskills.com)              │    │
│  │                                                                  │    │
│  │  A Record: osmt.staging.prettygoodskills.com ──┐                │    │
│  │  A Record: osmt-staff.staging.prettygoodskills.com ─┐          │    │
│  └──────────────────────────────────────────────────────┼──────────┘    │
│                                                         │               │
│  ┌──────────────────────────────────────────────────────▼──────────┐  │
│  │                    Application Load Balancer                      │  │
│  │                                                                  │  │
│  │  Listener Rule 1: Host=osmt.staging.prettygoodskills.com        │  │
│  │                   → Target Group: osmt-public (port 8080)       │  │
│  │                                                                  │  │
│  │  Listener Rule 2: Host=osmt-staff.staging.prettygoodskills.com  │  │
│  │                   → Target Group: osmt-staff (port 8081)        │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                         │               │
│  ┌──────────────────────────────────────────────────────▼──────────┐  │
│  │                      ECS Task (Fargate)                           │  │
│  │                      (bumped CPU/memory)                           │  │
│  │                                                                  │  │
│  │  ┌──────────────────────┐      ┌────────────────────────┐      │  │
│  │  │ Container: osmt-public│      │ Container: osmt-staff  │      │  │
│  │  │ Port: 8080             │      │ Port: 8081             │      │  │
│  │  │ Profile: readonly      │      │ Profile: oauth2        │      │  │
│  │  │ DB: osmt_ro (SELECT)   │      │ DB: master (full)      │      │  │
│  │  │ Color: #1e40af (blue)  │      │ Color: #e65100 (orange)│      │  │
│  │  │ Auth: None             │      │ Auth: Google OAuth2    │      │  │
│  │  └───────────┬───────────┘      └──────────┬─────────────┘      │  │
│  │              │                              │                   │  │
│  └──────────────┼──────────────┬───────────────┼───────────────────┘  │
│                 │              │               │                       │
│                 └──────────────┼───────────────┘                       │
│                                │                                       │
│  ┌─────────────────────────────▼───────────────────────────────┐      │
│  │                    RDS MySQL (existing)                      │      │
│  │                                                              │      │
│  │  DB: osmt_db                                                 │      │
│  │  Users: master (full), osmt_ro (SELECT-only)                 │      │
│  └──────────────────────────────────────────────────────────────┘      │
│                                                                          │
└────────────────────────────────────────────────────────────────────────┘
```

## Main Components and How They Interact

### 1. Route53 DNS

Two A records pointing to the same ALB:
- `osmt.staging.prettygoodskills.com` → ALB
- `osmt-staff.staging.prettygoodskills.com` → ALB

### 2. ALB Listener Rules

Host-based routing to different target groups:
- Host `osmt.staging.prettygoodskills.com` → TG port 8080
- Host `osmt-staff.staging.prettygoodskills.com` → TG port 8081

### 3. ECS Task Definition

Single task with two containers:
- **osmt-public**: Port 8080, `readonly` profile, blue branding, no auth
- **osmt-staff**: Port 8081, `oauth2` profile, orange branding, Google OAuth

### 4. Database

Single RDS instance with two users:
- **master**: Full permissions (for staff instance migrations and writes)
- **osmt_ro**: SELECT-only (for public instance)

### 5. Container Configuration

Each container gets different environment variables via `secrets.env`:

**Public container**:
```
SPRING_PROFILES_ACTIVE=apiserver,readonly
DB_USER=osmt_ro
DB_PASSWORD=<ro_password>
OSMT_INSTANCE_TYPE=read-only
OSMT_WRITABLE_INSTANCE_URL=https://osmt-staff.staging.prettygoodskills.com
OSMT_BRAND_COLOR=#1e40af
```

**Staff container**:
```
SPRING_PROFILES_ACTIVE=apiserver,oauth2
DB_USER=<master_user>
DB_PASSWORD=<master_password>
OSMT_INSTANCE_TYPE=writable
OSMT_BRAND_COLOR=#e65100
OSMT_AUTHORING_WELCOME_MESSAGE="Staff authoring instance"
OAUTH_GOOGLE_CLIENT_ID=...
OAUTH_GOOGLE_CLIENT_SECRET=...
```
