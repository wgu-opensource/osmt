resource "aws_db_parameter_group" "this" {
  name   = local.identity-prefix
  family = var.config.rds.parameter_group_family

  lifecycle {
    create_before_destroy = true
  }
}


resource "aws_db_subnet_group" "this" {
    name       = local.identity-prefix
    subnet_ids = module.vpc.private_subnets
}


resource "aws_db_instance" "this" {
    identifier             = local.identity-prefix
    allocated_storage      = 10
    availability_zone      = var.config.rds.multi_az ? null : module.vpc.azs[0]
    db_name                = var.config.rds.db_name
    db_subnet_group_name   = aws_db_subnet_group.this.name
    engine                 = "mysql"
    engine_version         = var.config.rds.engine_version
    instance_class         = var.config.rds.instance_class
    multi_az               = var.config.rds.multi_az
    network_type           = "IPV4"
    parameter_group_name   = aws_db_parameter_group.this.name
    password               = var.secrets.rds.master_password
    skip_final_snapshot    = var.config.rds.skip_final_snapshot
    storage_type           = "gp2"
    username               = var.secrets.rds.master_username
    vpc_security_group_ids = [aws_security_group.mysql.id]
}
