# Phase 1: Terraform - ALB Routing and Target Groups

## Scope of Phase

Update Terraform configuration to:
1. Add new Route53 record for `osmt-staff.staging.prettygoodskills.com`
2. Add second target group for port 8081
3. Add ALB listener rules for host-based routing
4. Update ECS task definition to support two containers

## Code Organization Reminders

- Prefer a granular file structure, one concept per file
- Place more abstract things, entry points, and tests **first**
- Place helper utility functions **at the bottom** of files
- Keep related functionality grouped together
- Any temporary code should have a TODO comment so we can find it later

## Implementation Details

### 1. Add Route53 Record (staging environment)

**File**: `/Users/yona/dev/skybridge/infra/environments/staging/route53.tf` (or wherever staging DNS is defined)

```hcl
resource "aws_route53_record" "osmt_staff" {
  zone_id = aws_route53_zone.staging.zone_id
  name    = "osmt-staff.staging.prettygoodskills.com"
  type    = "A"
  alias {
    name                   = module.osmt.alb_dns_name
    zone_id                = module.osmt.alb_zone_id
    evaluate_target_health = true
  }
}
```

### 2. Update OSMT Module - Target Groups

**File**: `/Users/yona/dev/skybridge/osmt/infra/aws/terraform/module/alb.tf`

Add second target group:

```hcl
resource "aws_lb_target_group" "osmt_staff" {
  name        = "${local.identity-prefix}-staff"
  port        = 8081
  protocol    = "HTTP"
  vpc_id      = module.vpc.vpc_id
  target_type = "ip"

  health_check {
    path                = "/health"
    port                = "traffic-port"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }
}

# Update existing public target group to be explicit
resource "aws_lb_target_group" "osmt_public" {
  name        = "${local.identity-prefix}-public"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = module.vpc.vpc_id
  target_type = "ip"
  # ... existing health check config
}
```

### 3. Update ALB Listener Rules

**File**: `/Users/yona/dev/skybridge/osmt/infra/aws/terraform/module/alb.tf`

Modify HTTPS listener rules:

```hcl
resource "aws_lb_listener_rule" "osmt_staff" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 100  # Higher priority than public

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.osmt_staff.arn
  }

  condition {
    host_header {
      values = ["osmt-staff.staging.prettygoodskills.com"]
    }
  }
}

# Update existing rule to be explicit about host
resource "aws_lb_listener_rule" "osmt_public" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 200

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.osmt_public.arn
  }

  condition {
    host_header {
      values = ["osmt.staging.prettygoodskills.com"]
    }
  }
}
```

### 4. Update ECS Service - Multiple Target Groups

**File**: `/Users/yona/dev/skybridge/osmt/infra/aws/terraform/module/ecs.tf`

Update ECS service to attach both target groups:

```hcl
resource "aws_ecs_service" "osmt" {
  name            = local.identity-prefix
  cluster         = aws_ecs_cluster.osmt.id
  task_definition = aws_ecs_task_definition.osmt.arn
  desired_count   = var.config.ecs.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = module.vpc.private_subnets
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = false
  }

  # Public container target group
  load_balancer {
    target_group_arn = aws_lb_target_group.osmt_public.arn
    container_name   = "osmt-public"
    container_port   = 8080
  }

  # Staff container target group
  load_balancer {
    target_group_arn = aws_lb_target_group.osmt_staff.arn
    container_name   = "osmt-staff"
    container_port   = 8081
  }

  depends_on = [aws_lb_listener.https]
}
```

## Validate

After applying Terraform:

```bash
cd /Users/yona/dev/skybridge/infra/environments/staging
terraform plan
terraform apply
```

Verify:
1. Both Route53 records resolve to ALB
2. Both target groups exist in AWS Console
3. ALB listener rules show host-based routing
