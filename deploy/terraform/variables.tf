variable "AWS_ACCESS_KEY" {
  type = string
  description = "AWS access key"
  sensitive = true
}

variable "AWS_SECRET_KEY" {
  type = string
  description = "AWS secret key"
  sensitive = true
}

variable "AWS_SESSION_TOKEN" {
  type = string
  description = "AWS Session Token"
  sensitive = true
}

variable "AWS_REGION" {
  type = string
  description = "AWS region to use for resources"
  default = "us-west-2"
}


