resource "aws_elasticache_subnet_group" "this" {
    name        = local.identity-prefix
    description = local.identity-prefix
    subnet_ids  = module.vpc.private_subnets
}


resource "aws_elasticache_replication_group" "this" {
    description                 = local.identity-prefix
    automatic_failover_enabled  = true
    engine                      = "redis"
    engine_version              = var.config.elasticache.engine_version
    node_type                   = var.config.elasticache.node_type
    num_cache_clusters          = 2
    parameter_group_name        = var.config.elasticache.parameter_group_name
    port                        = 6379
    preferred_cache_cluster_azs = module.vpc.azs
    replication_group_id        = local.identity-prefix
    security_group_ids          = [aws_security_group.redis.id]
    subnet_group_name           = aws_elasticache_subnet_group.this.name
}