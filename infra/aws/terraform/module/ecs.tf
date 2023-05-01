resource "aws_ecs_cluster" "this" {
    name = local.identity-prefix
}


resource "aws_ecs_service" "app" {
  name            = "${local.identity-prefix}-app"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.config.ecs.task.app.desired_count
  #iam_role        = aws_iam_role.ecs.arn
  #depends_on      = [aws_iam_role_policy.foo] // TODO

  load_balancer {
    target_group_arn = aws_lb_target_group.this.arn
    container_name   = var.config.alb.container_name
    container_port   = 80
  }

  network_configuration {
    security_groups = [aws_security_group.ecs.id]
    subnets         = module.vpc.private_subnets
  }

  lifecycle {
    ignore_changes = [
      desired_count,
      task_definition
    ]
  }
}


resource "aws_ecs_service" "elasticsearch" {
  name            = "${local.identity-prefix}-elasticsearch"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.elasticsearch.arn
  desired_count   = var.config.ecs.task.elasticsearch.desired_count
  #iam_role        = aws_iam_role.ecs.arn
  #depends_on      = [aws_iam_role_policy.foo] // TODO

  load_balancer {
    target_group_arn = aws_lb_target_group.this.arn
    container_name   = var.config.alb.container_name
    container_port   = 80
  }

  network_configuration {
    security_groups = [aws_security_group.ecs.id]
    subnets         = module.vpc.private_subnets
  }

  lifecycle {
    ignore_changes = [
      desired_count,
      task_definition
    ]
  }
}


resource "aws_ecs_task_definition" "app" {
    family                   = "${local.identity-prefix}-app"
    cpu                      = var.config.ecs.task.app.cpu
    execution_role_arn       = aws_iam_role.ecs_task_execution.arn
    memory                   = var.config.ecs.task.app.memory
    network_mode             = "awsvpc"
    requires_compatibilities = ["FARGATE"]
    task_role_arn            = aws_iam_role.ecs_task_execution.arn
    
    container_definitions = templatefile("${path.module}/templates/container_definition_app.json.tpl", {
      aws_region            = data.aws_region.current.name
      base_domain           = var.secrets.app.base_domain
      container_name        = "app"
      cpu                   = var.config.ecs.task.app.cpu
      db_name               = "osmt_db"
      db_password           = var.secrets.rds.master_password
      db_uri                = aws_db_instance.this.endpoint
      db_user               = var.secrets.rds.master_username
      docker_tag            = var.config.ecs.task.app.docker_tag
      docker_image          = var.config.ecs.task.app.docker_image
      elasticsearch_uri     = "elasticsearch:9200"
      environment           = var.secrets.app.environment
      frontend_url          = var.secrets.app.frontend_url
      log_group             = aws_cloudwatch_log_group.app.name
      log_stream_prefix     = "${local.identity_prefix_path}/app"
      memory                = var.config.ecs.task.elasticsearch.cpu
      migrations_enabled    = var.secrets.app.migrations_enabled
      oauth_audience        = var.secrets.app.oauth_audience 
      oauth_clientid        = var.secrets.app.oauth_clientid
      oauth_issuer          = var.secrets.app.oauth_issuer
      oauth_secret          = var.secrets.app.oauth_secret
      port                  = 80
      rds_endpoint          = aws_db_instance.this.endpoint
      redis_uri             = aws_elasticache_replication_group.this.primary_endpoint_address
      reindex_elasticsearch = var.secrets.app.reindex_elasticsearch
      skip_metadata_import  = var.secrets.app.skip_metadata_import
    })
}


resource "aws_ecs_task_definition" "elasticsearch" {
    family                   = "${local.identity-prefix}-elasticsearch"
    cpu                      = var.config.ecs.task.elasticsearch.cpu
    execution_role_arn       = aws_iam_role.ecs_task_execution.arn
    memory                   = var.config.ecs.task.elasticsearch.memory
    network_mode             = "awsvpc"
    requires_compatibilities = ["FARGATE"]
    task_role_arn            = aws_iam_role.ecs_task_execution.arn

    container_definitions = templatefile("${path.module}/templates/container_definition_elasticsearch.json.tpl", {
      aws_region        = data.aws_region.current.name
      container_name    = "elasticsearch"
      cpu               = var.config.ecs.task.elasticsearch.cpu
      docker_tag        = var.config.ecs.task.elasticsearch.docker_tag
      docker_image      = var.config.ecs.task.elasticsearch.docker_image
      log_group         = aws_cloudwatch_log_group.elasticsearch.name
      log_stream_prefix = "${local.identity_prefix_path}/elasticsearch"
      memory            = var.config.ecs.task.elasticsearch.cpu
      port              = 9200
    })
}