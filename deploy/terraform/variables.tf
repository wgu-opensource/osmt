variable "db_user" {
  type = string
  description = "osmt_db user"
  sensitive = true
}

variable "db_password" {
  type = string
  description = "osmt_db password"
  sensitive = true
}

variable "db_name" {
  type = string
  description = "osmt_db name"
  sensitive = true
}

variable "default_sg" {
  type = string
  description = "Default security group"
}

variable "subnet_id" {
  type = string
  description = "Subnet-1"
}

variable "aws_profile" {
  type = string
  description = "AWS profile for authentication"
}

variable "osmt_dev_cluster" {
  type = string
  description = "Name of ECS Cluster"
  default = "osmt-dev-cluster"
}

variable "environment_tag" {
  type = string
  description = "Tag for default env"
  default = "dev"
}

variable "rds_identifier" {
  type = string
  description = "Identifier for RDS"
  default = "osmt-dev-rds"
}
