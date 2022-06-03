output "vpc_subnets" {
  value = module.vpc.public_subnets
}
output "alb_lb_dns_name" {
  value = module.alb.aws_lb_dns_name
}