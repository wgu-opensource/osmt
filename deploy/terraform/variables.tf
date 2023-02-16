variable "AWS_REGION" {
  type = string
  description = "AWS region to use for resources"
  default = "us-east-1"
}

variable "db_password" {
  type = string
  description = "osmt_db password"
  sensitive = true
}

variable "vpc_id" {
  type = string
  description = "VPC ID"
  default = "vpc-095fce400b46c1bd2"
}

variable "igw" {
  type = string
  description = "Internet Gateway"
  default = "igw-0aa4bbf12b3719ce9"
}

variable "subnet_id" {
  type = string
  description = "Subnet us-east-1b"
  default = "subnet-0ea9c574d49cc2f7f"
}

variable "rtb" {
  type = string
  description = "Route Table"
  default = "rtb-0973bc04be30370ee"
}

