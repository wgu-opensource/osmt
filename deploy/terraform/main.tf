##################################################################################
# PROVIDERS
##################################################################################

provider "aws" {
  access_key = var.AWS_ACCESS_KEY
  secret_key = var.AWS_SECRET_KEY
  token = var.AWS_SESSION_TOKEN
  region = var.AWS_REGION
}

##################################################################################
# DATA
##################################################################################

data "aws_ssm_parameter" "ami" {
  name = "/aws/service/ami-amazon-linux-latest/amzn2-ami-hvm-x86_64-gp2"
}

##################################################################################
# RESOURCES
##################################################################################

# NETWORKING #
resource "aws_vpc" "opensource-vpc" {
  cidr_block           = "172.31.0.0/16"
  enable_dns_hostnames = true
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.opensource-vpc.id
}

resource "aws_subnet" "us-east-1b" {
  cidr_block              = "172.31.80.0/20"
  vpc_id                  = aws_vpc.opensource-vpc.id
  map_public_ip_on_launch = true
}

# ROUTING #
resource "aws_route_table" "rtb" {
  vpc_id = aws_vpc.opensource-vpc.id

  route {
    cidr_block = "172.31.0.0/16"
    gateway_id = aws_internet_gateway.igw.id
  }
}

resource "aws_route_table_association" "rta-subnet1" {
  subnet_id      = aws_subnet.us-east-1b.id
  route_table_id = aws_route_table.rtb.id
}

resource "aws_db_instance" "osmt_db" {
  allocated_storage    = 10
  db_name              = "osmt_db"
  engine               = "mysql"
  engine_version       = "5.7"
  instance_class       = "db.t3.micro"
  username             = "osmt_user"
  password             = "password"
  parameter_group_name = "default.mysql5.7"
  skip_final_snapshot  = true
}

# SECURITY GROUPS #
# Nginx security group
resource "aws_security_group" "nginx-sg" {
  name   = "nginx_sg"
  vpc_id = aws_vpc.opensource-vpc.id

  # HTTP access from anywhere
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.opensource-vpc.cidr_block]
  }
}

# INSTANCES #
resource "aws_instance" "nginx1" {
  ami                    = nonsensitive(data.aws_ssm_parameter.ami.value)
  instance_type          = "t2.micro"
  subnet_id              = aws_subnet.us-east-1b.id
  vpc_security_group_ids = [aws_security_group.nginx-sg.id]
}

