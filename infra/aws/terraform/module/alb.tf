resource "aws_lb" "this" {
    name               = local.identity-prefix
    internal           = false
    load_balancer_type = "application"
    security_groups    = [aws_security_group.alb.id]
    subnets            = module.vpc.public_subnets

    enable_deletion_protection = var.config.alb.enable_deletion_protection
}


resource "aws_lb_target_group" "this" {
    name        = local.identity-prefix
    port        = 80
    protocol    = "HTTP"
    target_type = "ip"
    vpc_id      = module.vpc.vpc_id

    health_check {
        healthy_threshold   = 3
        interval            = 15
        path                = "/"
        port                = 80
        protocol            = "HTTP"
        timeout             = 5
        unhealthy_threshold = 2
    }
}


resource "aws_lb_listener" "http" { 
    load_balancer_arn = aws_lb.this.arn
    port              = 80
    protocol          = "HTTP"

    default_action {
        type = "redirect"

        redirect {
            port        = "443"
            protocol    = "HTTPS"
            status_code = "HTTP_301"
        }
    }
}


resource "aws_lb_listener" "https" {
    certificate_arn   = var.config.alb.certificate_arn
    load_balancer_arn = aws_lb.this.arn
    port              = 443
    protocol          = "HTTPS"

    default_action {
        type             = "forward"
        target_group_arn = aws_lb_target_group.this.arn
    }
}