# Staging Split Deployment Infrastructure

## Scope of Work

Deploy the OSMT split deployment (read-only public + writable staff instances) to staging environment.

### Key Deliverables

1. **New domain**: `osmt-staff.staging.prettygoodskills.com` for the authoring instance
2. **New OSMT instance**: ECS service for staff (writable) instance in the same VPC/cluster
3. **Reconfigure existing OSMT**: Make `osmt.staging.prettygoodskills.com` read-only
4. **Database setup**: Read-only database user for public instance
5. **Auth configuration**: OAuth2 on staff instance, no auth on public instance

### Current Staging Infrastructure

- **Platform**: AWS ECS Fargate
- **Current OSMT**: `osmt.staging.prettygoodskills.com` (single instance with auth)
- **Database**: RDS MySQL (single master user currently)
- **Domain**: Route53 `staging.prettygoodskills.com` zone
- **Deployment**: Terraform Cloud + deploy script

## Questions

### Q1: Should the read-only instance keep OAuth2 enabled or be fully open?

**Context**: The split deployment plan has two options:
- Option A: Public instance has no auth at all (completely open)
- Option B: Public instance keeps OAuth2 but blocks writes via 403

**Current state**: The existing staging OSMT has Google OAuth2 enabled.

**Answer**: Option A - public instance has no authentication at all. No login, no OAuth2, completely open for reading only.

### Q2: How should we handle the database users?

**Context**: Currently using a single RDS master user. For split deployment:
- Read-only instance needs a user with only SELECT permissions
- Staff instance needs full permissions

**Options**:

**Option A: Separate database users**
- Create `osmt_ro` user with SELECT-only
- Keep existing master user for staff instance
- Different DB credentials per instance via SSM parameters

**Option B: Same user, application-level enforcement**
- Both instances use same DB user
- Read-only instance blocks writes at application layer (Spring Security)
- Simpler but less defense-in-depth

**Answer**: Option A - separate database users for defense-in-depth. Create `osmt_ro` with SELECT-only permissions.

### Q3: Should we share the ECS cluster or create a new one?

**Context**: The easy approach is another ECS service in the same cluster. But there are trade-offs.

**Options**:

**Option A: Same ECS cluster, separate services**
- Public service: `osmt-staging` (read-only)
- Staff service: `osmt-staff-staging` (writable)
- Shared ALB with host-based routing
- Simpler networking, less cost

**Option B: Separate clusters**
- More isolation but overkill for staging demo
- More complexity and cost

**Answer**: Run two containers in the same ECS task definition. Port 8080 for public (read-only), port 8081 for staff (writable). ALB routes by Host header to different target groups (same task, different ports). Minimal infra changes.

### Q4: How do we route between the two instances?

**Context**: We need two domains pointing to the same ALB but routing to different ECS services.

**Current setup**: 
- ALB with listener rules
- Single ECS service target group

**Options**:

**Option A: Host-based routing on ALB**
- `osmt.staging.prettygoodskills.com` → public service
- `osmt-staff.staging.prettygoodskills.com` → staff service
- Single ALB, two target groups
- Clean URL structure

**Option B: Path-based routing**
- `osmt.staging.prettygoodskills.com` → public
- `osmt.staging.prettygoodskills.com/staff` → staff
- Messier URLs, harder for authors

**Answer**: Option A - host-based routing on ALB. Two target groups, one per container port. Clean intuitive URLs.

### Q5: What branding/color should the staff instance use?

**Context**: The staff instance should be visually distinct per the feature spec.

**Options**:

**Option A: Orange/warning color**
- `#e65100` (material orange 900)
- Clear "this is special" indicator
- Easy to implement

**Option B: Keep existing blue for public, change staff to something else**
- Public: Blue (`#1e40af`)
- Staff: Green or other color
- Less "warning" vibe

**Answer**: Option A - orange/warning color (`#e65100`) for staff instance. Standard blue for public. Visual distinction makes it obvious which instance you're on.

### Q6: Should we migrate existing data or start fresh?

**Context**: Staging has some test data currently.

**Options**:

**Option A: Keep existing data**
- Both instances share the same database
- Public becomes read-only view of existing data
- Staff can edit existing data

**Option B: Start fresh for demo purposes**
- Clean slate
- Not representative of real migration

**Answer**: Option A - keep existing data. Both instances share the same RDS instance; public becomes read-only view, staff can edit.

## Notes

- The `osmt-cli.sh` script in the main OSMT repo uses profiles like `dev,apiserver,oauth2`
- For the read-only instance, we'd use `apiserver,readonly` (no oauth2)
- Need to update the Terraform module to support multiple OSMT instances per environment
- Need to create the read-only DB user via Terraform or manual SQL
