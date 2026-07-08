# Phase 3: Database - Read-Only User Setup

## Scope of Phase

Create the `osmt_ro` database user with SELECT-only permissions on the `osmt_db` database.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later

## Implementation Details

### Option A: Terraform (preferred for IaC)

Add to RDS Terraform or as a separate SQL provisioner:

**File**: `/Users/yona/dev/skybridge/osmt/infra/aws/terraform/module/rds.tf`

Add a `null_resource` with `local-exec` to run SQL:

```hcl
resource "null_resource" "create_ro_user" {
  triggers = {
    db_instance_id = aws_db_instance.osmt.id
  }

  provisioner "local-exec" {
    command = <<-EOF
      mysql -h ${aws_db_instance.osmt.address} \
            -u ${var.secrets.rds.master_username} \
            -p'${var.secrets.rds.master_password}' \
            -e "${local.create_ro_user_sql}"
    EOF
  }

  depends_on = [aws_db_instance.osmt]
}

locals {
  create_ro_user_sql = <<-SQL
    CREATE USER IF NOT EXISTS 'osmt_ro'@'%' IDENTIFIED BY '${var.secrets.osmt_ro_db_password}';
    GRANT SELECT ON ${var.config.rds.db_name}.* TO 'osmt_ro'@'%';
    FLUSH PRIVILEGES;
  SQL
}
```

### Option B: Manual SQL (for quick demo)

**File**: `docs/plans/2026-04-13-staging-split-deployment/db-user-setup.sql`

```sql
-- Run this against the staging RDS instance as master user
-- Replace <password> with secure password, store in SSM

CREATE USER IF NOT EXISTS 'osmt_ro'@'%' IDENTIFIED BY '<password>';

-- Grant SELECT on all tables in osmt_db
GRANT SELECT ON osmt_db.* TO 'osmt_ro'@'%';

-- Verify
SHOW GRANTS FOR 'osmt_ro'@'%';

FLUSH PRIVILEGES;
```

Run manually:

```bash
mysql -h <rds-endpoint> -u <master_user> -p < db-user-setup.sql
```

Then store the password in SSM:

```bash
aws ssm put-parameter \
  --name "/staging/osmt/OSMT_RO_DB_PASSWORD" \
  --type SecureString \
  --value "<password>"
```

### Option C: Terraform MySQL Provider

Add MySQL provider to Terraform:

```hcl
provider "mysql" {
  endpoint = "${aws_db_instance.osmt.address}:3306"
  username = var.secrets.rds.master_username
  password = var.secrets.rds.master_password
}

resource "mysql_user" "osmt_ro" {
  user     = "osmt_ro"
  host     = "%"
  plaintext_password = var.secrets.osmt_ro_db_password
}

resource "mysql_grant" "osmt_ro" {
  user       = mysql_user.osmt_ro.user
  host       = mysql_user.osmt_ro.host
  database   = var.config.rds.db_name
  privileges = ["SELECT"]
}
```

## Validate

Connect as read-only user and verify:

```bash
mysql -h <rds-endpoint> -u osmt_ro -p -e "SELECT COUNT(*) FROM osmt_db.skills;"
```

Should succeed. Verify writes are blocked:

```bash
mysql -h <rds-endpoint> -u osmt_ro -p -e "INSERT INTO osmt_db.skills (id) VALUES (1);"
```

Should fail with permission denied.
