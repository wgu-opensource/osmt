variable "config" {
  type = object({
    alb = object({
      certificate_arn            = string
      container_name             = string
      enable_deletion_protection = bool
    })
    ecs = object({
      task = object({
        app = object({
          cpu           = number
          desired_count = number
          docker_image  = string
          docker_tag    = string
          memory        = number
        })
        elasticsearch = object({
          cpu           = number
          desired_count = number
          docker_image  = string
          docker_tag    = string
          memory        = number
        })
      })
    })
    env = string
    elasticache = object({
      engine_version       = string
      node_type            = string
      parameter_group_name = string
    })
    rds = object({
      db_name                = string
      engine_version         = string
      instance_class         = string
      multi_az               = bool
      parameter_group_family = string
      skip_final_snapshot    = bool
    })
    vpc = object({
      azs = list(string)
      subnets = object({
        private = list(string)
        public  = list(string)
      })
      vpc_cidr = string
    })
  })
}
