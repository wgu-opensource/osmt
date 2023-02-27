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

############ DATABASES ##########
resource "aws_db_instance" "osmt_db" {
  allocated_storage    = 20
  db_name              = "osmt_db"
  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = "db.t3.micro"
  username             = "osmt_user"
  password             = var.db_password
  port                 = 3306
  identifier           = "osmt-dev-rds"
  auto_minor_version_upgrade = true
  skip_final_snapshot  = true
  vpc_security_group_ids = [aws_security_group.rds_sg]
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

######### SECURITY GROUPS #########
resource "aws_security_group" "rds_sg" {
  name = "rds-sg"
  description = "RDS security group"
  vpc_id = data.aws_vpc.opensource_vpc.id

  ingress {
    from_port = 3306
    protocol  = "tcp"
    to_port   = 3306
  }
}

resource "aws_security_group" "elasticsearch_sg" {
  name = "elasticsearch-sg"
  description = "ElasticSearch instance security group"
  vpc_id = data.aws_vpc.opensource_vpc.id

  ingress {
    from_port = 9200
    protocol  = "tcp"
    to_port   = 9200
    cidr_blocks = [data.aws_vpc.opensource_vpc.cidr_block]
  }

  egress {
    from_port = 0
    protocol  = -1
    to_port   = 0
    cidr_blocks = ["0.0.0.0/0"]
  }
}

########## EC2 INSTANCES ##########
resource "aws_instance" "elasticsearch" {
  ami                     = nonsensitive(data.aws_ssm_parameter.ami.value)
  count                   = 2
  instance_type           = "t2.micro"
  subnet_id               = data.aws_subnet.subnet_1.id
  vpc_security_group_ids  = [aws_security_group.elasticsearch_sg.id]
  root_block_device {
    volume_size = 40
    volume_type = "gp3"
  }
  user_data = <<EOF
#!/bin/bash
sudo yum update -y
sudo yum install jq -y

# Get the elasticsearch package and checksum
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$elasticsearch_version-x86_64.rpm
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-$elasticsearch_version-x86_64.rpm.sha512

# Test to see if the checksum matches. !! We need to be sure not to proceed if this fails.
if sha512sum -c elasticsearch-$elasticsearch_version-x86_64.rpm.sha512 | grep -q 'OK'
then
  sudo rpm --install elasticsearch-$elasticsearch_version-x86_64.rpm
  sudo echo y | /usr/share/elasticsearch/bin/elasticsearch-plugin install -s discovery-ec2

  # Configure cloud-aws plugin

  host_ips=`aws ec2 describe-instances --region=$REGION --filters "Name=instance-state-name,Values=running" --query 'Reservations[].Instances[].[PrivateIpAddress]' --output json`

  sudo echo 'discovery.seed_providers: ec2' | sudo tee -a /etc/elasticsearch/elasticsearch.yml
  sudo echo 'discovery.ec2.tag.Application: osmt-dev' | sudo tee -a /etc/elasticsearch/elasticsearch.yml
  sudo echo 'cloud.node.auto_attributes: true' | sudo tee -a /etc/elasticsearch/elasticsearch.yml
  sudo echo 'network.host: 0.0.0.0' | sudo tee -a /etc/elasticsearch/elasticsearch.yml
  #sudo echo 'cluster.initial_master_nodes: '$ip_list | sudo tee -a /etc/elasticsearch/elasticsearch.yml

  # Set elasticsearch to run as a service
  sudo systemctl daemon-reload
  sudo systemctl enable elasticsearch

  # Start the service
  sudo systemctl start elasticsearch
  # Check the status of the service to make sure it's running and if no print a message to the console. Printing to the console should put the
  # message in Cloudwatch.
  status=`curl -XGET -H "Content: application/json" "$IPV4:9200/_cluster/health?pretty" | jq -r ."status"`
  if [ "$status" != "green" ]
  then
      echo "The Elasticsearch service failed to start. The status returned is "$status
  else
      echo "The Elasticsearch service was started successfully. The status returned is "$status
  fi

else
  echo "Checksum failed on elasticsearch-"$elasticsearch_version"-x86_64.rpm"
fi

EOF

}

