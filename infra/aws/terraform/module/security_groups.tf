resource "aws_security_group" "alb" {
    name        = "${local.identity-prefix}-alb"
    description = "${local.identity-prefix}-alb"
    vpc_id      = module.vpc.vpc_id

    ingress {
        description      = "HTTP"
        from_port        = 80
        to_port          = 80
        protocol         = "tcp"
        cidr_blocks      = [module.vpc.vpc_cidr_block]
    }

    egress {
        from_port        = 0
        to_port          = 0
        protocol         = "-1"
        cidr_blocks      = ["0.0.0.0/0"]
    }
}


resource "aws_security_group" "ecs" {
    name        = "${local.identity-prefix}-ecs"
    description = "${local.identity-prefix}-ecs"
    vpc_id      = module.vpc.vpc_id

    ingress {
        description      = "HTTP"
        from_port        = 80
        to_port          = 80
        protocol         = "tcp"
        cidr_blocks      = [module.vpc.vpc_cidr_block]
    }

    ingress {
        description      = "Elasticsearch"
        from_port        = 9200
        to_port          = 9200
        protocol         = "tcp"
        cidr_blocks      = [module.vpc.vpc_cidr_block]
    }

    egress {
        from_port        = 0
        to_port          = 0
        protocol         = "-1"
        cidr_blocks      = ["0.0.0.0/0"]
    }
}


resource "aws_security_group" "mysql" {
    name        = "${local.identity-prefix}-mysql"
    description = "${local.identity-prefix}-mysql"
    vpc_id      = module.vpc.vpc_id

    ingress {
        description      = "MySQL"
        from_port        = 3306
        to_port          = 3306
        protocol         = "tcp"
        cidr_blocks      = [module.vpc.vpc_cidr_block]
    }

    egress {
        from_port        = 0
        to_port          = 0
        protocol         = "-1"
        cidr_blocks      = ["0.0.0.0/0"]
    }
}


resource "aws_security_group" "redis" {
    name        = "${local.identity-prefix}-redis"
    description = "${local.identity-prefix}-redis"
    vpc_id      = module.vpc.vpc_id

    ingress {
        description      = "Redis"
        from_port        = 6379
        to_port          = 6379
        protocol         = "tcp"
        cidr_blocks      = [module.vpc.vpc_cidr_block]
    }

    egress {
        from_port        = 0
        to_port          = 0
        protocol         = "-1"
        cidr_blocks      = ["0.0.0.0/0"]
    }
}