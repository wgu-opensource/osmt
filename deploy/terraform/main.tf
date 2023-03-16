##################################################################################
# PROVIDERS
##################################################################################

# NOTE before others start working on the TF files, we need to setup
# remote backend. TF does not recommend checking in tfstate  and lock file
# into git so we must configure a S3 bucket that will host the tfstate and lock file
# that way we all pull the same state file when someone makes changes to TF.

terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "~> 4.53.0"
    }
  }
}

provider "aws" {
  profile = var.aws_profile
}
##################################################################################
# DATA
##################################################################################

data "aws_subnet" "subnet_1" {
  id = var.subnet_id
}

data "aws_security_group" "default_sg" {
  id = var.default_sg
}

data "aws_ssm_parameter" "ami" {
  name = "/aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2"
}

##################################################################################
# RESOURCES
##################################################################################

########### Container service ##########
resource "aws_ecs_cluster" "osmt_container_cluster" {
  name = "osmt-dev-cluster"
}

############ DATABASES ##########
resource "aws_db_instance" "osmt_db" {
  allocated_storage    = 20
  db_name              = var.db_name
  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = "db.t3.micro"
  username             = var.db_user
  password             = var.db_password
  port                 = 3306
  identifier           = "osmt-dev-rds"
  auto_minor_version_upgrade = true
  skip_final_snapshot  = true
  tags = {
    env: "dev"
  }
}

resource "aws_elasticache_cluster" "redis_db" {
  cluster_id           = "redis-task-queue"
  engine               = "redis"
  node_type            = "cache.m4.large"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis6.x"
  engine_version       = "6.x"
  port                 = 6379
  tags = {
    env: "dev"
  }
}

########## EC2 INSTANCES ##########
resource "aws_instance" "elasticsearch" {
  ami                     = nonsensitive(data.aws_ssm_parameter.ami.value)
  count                   = 1
  instance_type           = "m5d.xlarge"
  subnet_id               = data.aws_subnet.subnet_1.id
  vpc_security_group_ids  = [data.aws_security_group.default_sg.id]
  key_name = "deploy-key"
  tags = {
    env: "dev"
  }
  root_block_device {
    delete_on_termination = true
    volume_size = 10
    volume_type = "gp2"
  }
  ebs_block_device {
    device_name = "/dev/sdh"
    delete_on_termination = true
    volume_size = 10
    volume_type = "gp2"
  }
  user_data = <<EOF
    #!/bin/bash
    yum update -y
    yum install jq -y

    # Get the elasticsearch package and checksum
    wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.17.4-x86_64.rpm
    wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.17.4-x86_64.rpm.sha512

    # Test to see if the checksum matches. !! We need to be sure not to proceed if this fails.
    if shasum -a 512 -c elasticsearch-7.17.4-x86_64.rpm.sha512  | grep -q 'OK'
    then
      rpm --install elasticsearch-7.17.4-x86_64.rpm
      echo y | /usr/share/elasticsearch/bin/elasticsearch-plugin install -s discovery-ec2

      # Configure cloud-aws plugin

      echo 'discovery.seed_providers: ec2' | tee -a /etc/elasticsearch/elasticsearch.yml
      echo 'discovery.ec2.tag.Application: osmt-dev' | tee -a /etc/elasticsearch/elasticsearch.yml
      echo 'cloud.node.auto_attributes: true' | tee -a /etc/elasticsearch/elasticsearch.yml
      echo 'network.host: 0.0.0.0' | tee -a /etc/elasticsearch/elasticsearch.yml

      # Set elasticsearch to run as a service
      /bin/systemctl daemon-reload
      /bin/systemctl enable elasticsearch.service

      # Start the service
      systemctl start elasticsearch.service

    else
      echo "Checksum failed on elasticsearch-"7.17.4"-x86_64.rpm"
    fi

    EOF
}
