# Phase 2: ECS Task Definition - Dual Containers

## Scope of Phase

Update the ECS task definition to run two OSMT containers:
1. `osmt-public` on port 8080 (read-only, blue branding)
2. `osmt-staff` on port 8081 (writable, orange branding, OAuth)

Increase task size to accommodate both containers.

## Code Organization Reminders

- Prefer a granular file structure, one concept per file
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later

## Implementation Details

### 1. Update Task Definition

**File**: `/Users/yona/dev/skybridge/osmt/infra/aws/terraform/module/ecs.tf`

Update task definition with two containers:

```hcl
resource "aws_ecs_task_definition" "osmt" {
  family                   = local.identity-prefix
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  # Bump CPU/memory for two containers
  cpu    = "1024"  # Was 512
  memory = "2048"  # Was 1024
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name  = "osmt-public"
      image = "${var.config.ecr_repository_url}:${var.config.docker_image_tag}"
      essential = true
      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]
      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "apiserver,readonly" },
        { name = "DB_HOST", value = aws_db_instance.osmt.address },
        { name = "DB_NAME", value = var.config.rds.db_name },
        { name = "DB_USER", value = "osmt_ro" },
        { name = "OSMT_INSTANCE_TYPE", value = "read-only" },
        { name = "OSMT_WRITABLE_INSTANCE_URL", value = "https://osmt-staff.staging.prettygoodskills.com" },
        { name = "OSMT_BRAND_COLOR", value = "#1e40af" },
        { name = "APP_BASE_DOMAIN", value = "osmt.staging.prettygoodskills.com" },
        { name = "APP_BASE_URL", value = "https://osmt.staging.prettygoodskills.com" },
      ]
      secrets = [
        { name = "DB_PASSWORD", valueFrom = aws_ssm_parameter.osmt_ro_db_password.arn },
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.osmt_public.name
          awslogs-region        = data.aws_region.current.name
          awslogs-stream-prefix = "osmt-public"
        }
      }
    },
    {
      name  = "osmt-staff"
      image = "${var.config.ecr_repository_url}:${var.config.docker_image_tag}"
      essential = true
      portMappings = [
        {
          containerPort = 8081
          protocol      = "tcp"
        }
      ]
      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "apiserver,oauth2" },
        { name = "DB_HOST", value = aws_db_instance.osmt.address },
        { name = "DB_NAME", value = var.config.rds.db_name },
        { name = "DB_USER", value = var.secrets.rds.master_username },
        { name = "OSMT_INSTANCE_TYPE", value = "writable" },
        { name = "OSMT_BRAND_COLOR", value = "#e65100" },
        { name = "OSMT_AUTHORING_WELCOME_MESSAGE", value = "Staff authoring instance" },
        { name = "APP_BASE_DOMAIN", value = "osmt-staff.staging.prettygoodskills.com" },
        { name = "APP_BASE_URL", value = "https://osmt-staff.staging.prettygoodskills.com" },
        { name = "APP_LOGIN_URL", value = "https://osmt-staff.staging.prettygoodskills.com/login" },
      ]
      secrets = [
        { name = "DB_PASSWORD", valueFrom = aws_ssm_parameter.db_password.arn },
        { name = "OAUTH_GOOGLE_CLIENT_ID", valueFrom = aws_ssm_parameter.oauth_google_client_id.arn },
        { name = "OAUTH_GOOGLE_CLIENT_SECRET", valueFrom = aws_ssm_parameter.oauth_google_client_secret.arn },
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.osmt_staff.name
          awslogs-region        = data.aws_region.current.name
          awslogs-stream-prefix = "osmt-staff"
        }
      }
    }
  ])
}

# Separate CloudWatch log groups
resource "aws_cloudwatch_log_group" "osmt_public" {
  name              = "/ecs/${local.identity-prefix}-public"
  retention_in_days = 7
}

resource "aws_cloudwatch_log_group" "osmt_staff" {
  name              = "/ecs/${local.identity-prefix}-staff"
  retention_in_days = 7
}
```

### 2. Add SSM Parameters for Read-Only DB User

**File**: `/Users/yona/dev/skybridge/osmt/infra/aws/terraform/module/secrets.tf` (or add to existing)

```hcl
resource "aws_ssm_parameter" "osmt_ro_db_password" {
  name  = "/${var.config.env}/osmt/OSMT_RO_DB_PASSWORD"
  type  = "SecureString"
  value = var.secrets.osmt_ro_db_password
}
```

Add to `variables.tf`:

```hcl
variable "secrets" {
  type = object({
    # ... existing fields ...
    osmt_ro_db_password = string
  })
}
```

## Validate

After applying Terraform:

```bash
cd /Users/yona/dev/skybridge/infra/environments/staging
terraform plan
terraform apply
```

Verify in AWS Console:
1. ECS Task Definition shows two containers
2. CPU/Memory bumped to 1024/2048
3. Both CloudWatch log groups exist
