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
  description = "opensource default security group"
}

variable "subnet_id" {
  type = string
  description = "Subnet us-east-1b"
}

variable "aws_profile" {
  type = string
  description = "AWS profile to authenticate to"
}
