variable "aws_access_key" {
  type = string
  description = "AWS access key"
  sensitive = true
}

variable "aws_secret_key" {
  type = string
  description = "AWS secret key"
  sensitive = true
}

variable "aws_region" {
  type = string
  description = "AWS region to use for resources"
  default = "us-west-2"
}


