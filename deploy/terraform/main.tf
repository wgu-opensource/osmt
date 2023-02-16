##################################################################################
# PROVIDERS
##################################################################################

provider "aws" {
  region = var.AWS_REGION
  profile = "opensource"
}

##################################################################################
# DATA
##################################################################################

data "aws_vpc" "opensource_vpc" {
  id = var.vpc_id
}

data "aws_subnet" "subnet_1" {
  id = var.subnet_id
}

data "aws_ssm_parameter" "ami" {
  name = "/aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2"
}

##################################################################################
# RESOURCES
##################################################################################

# Container service #
#resource "aws_ecs_service" "osmt_container" {
#  name = "osmt_app"
#}

# DATABASE RDS #
resource "aws_db_instance" "osmt_db" {
  allocated_storage    = 10
  db_name              = "osmt_db"
  engine               = "mysql"
  engine_version       = "5.7"
  instance_class       = "db.t3.micro"
  username             = "osmt_user"
  password             = var.db_password
  parameter_group_name = "default.mysql5.7"
  skip_final_snapshot  = true
}

resource "aws_elasticache_cluster" "redis_db" {
  cluster_id           = "redis-task-queue"
  engine               = "redis"
  node_type            = "cache.r6gd.xlarge"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis6.0.6"
  engine_version       = "6.0"
  port                 = 6379
}
# WGU VPN
# SECURITY GROUPS #
# Nginx security group
resource "aws_security_group" "nginx-sg" {
  name   = "nginx_sg"
  vpc_id = data.aws_vpc.opensource_vpc.id

  # HTTP access from anywhere
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = [data.aws_vpc.opensource_vpc.cidr_block]
  }
}

# INSTANCES #
resource "aws_instance" "nginx1" {
  ami                    = nonsensitive(data.aws_ssm_parameter.ami.value)
  instance_type          = "t2.micro"
  subnet_id              = data.aws_subnet.subnet_1.id
  vpc_security_group_ids = [aws_security_group.nginx-sg.id]
}

