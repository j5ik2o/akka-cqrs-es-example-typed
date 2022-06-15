locals {
  enabled = var.enabled
}

resource "aws_lb" "main" {
  count              = local.enabled ? 1 : 0
  name               = "${var.prefix}-alb-${var.name}"
  internal           = false
  load_balancer_type = "application"
  security_groups    = var.alb_security_groups
  subnets            = var.subnets

  enable_deletion_protection = false

  tags = {
    Name        = "${var.prefix}-alb-${var.name}"
    Environment = var.name
  }
}

resource "aws_alb_target_group" "main" {
  count       = local.enabled ? 1 : 0
  name        = "${var.prefix}-tg-${var.name}"
  port        = 80
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    healthy_threshold   = "3"
    interval            = "60"
    port                = var.health_check_port
    protocol            = "HTTP"
    matcher             = "200"
    timeout             = "30"
    path                = var.health_check_path
    unhealthy_threshold = "3"
  }

  tags = {
    Name        = "${var.prefix}-tg-${var.name}"
    Environment = var.prefix
  }
}

# Redirect to https listener
resource "aws_alb_listener" "http" {
  count = local.enabled ? 1 : 0
  load_balancer_arn = aws_lb.main[0].id
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "forward"
    target_group_arn = aws_alb_target_group.main[0].id
  }

#  default_action {
#    type = "redirect"
#
#    redirect {
#      port        = 443
#      protocol    = "HTTPS"
#      status_code = "HTTP_301"
#    }
#  }
}

# Redirect traffic to target group
#resource "aws_alb_listener" "https" {
#  load_balancer_arn = aws_lb.main.id
#  port              = 443
#  protocol          = "HTTPS"
#
#  ssl_policy        = "ELBSecurityPolicy-2016-08"
#  certificate_arn   = var.alb_tls_cert_arn
#
#  default_action {
#    target_group_arn = aws_alb_target_group.main.id
#    type             = "forward"
#  }
#}

output "aws_alb_target_group_arn" {
  value = concat(aws_alb_target_group.main.*.arn, [""])[0]
}

output "aws_lb_dns_name" {
  value = concat(aws_lb.main.*.dns_name, [""])[0]
}