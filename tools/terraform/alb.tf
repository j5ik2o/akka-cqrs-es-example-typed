module "alb" {
  source              = "./alb"
  enabled             = var.alb_enabled
  prefix              = var.prefix
  name                = var.name
  vpc_id              = module.vpc.vpc_id
  subnets             = [element(module.vpc.public_subnets, 1), element(module.vpc.public_subnets, 2)]
  alb_security_groups = [module.security_groups.alb_id]
  health_check_port   = var.health_check_port
  health_check_path   = var.health_check_path
  # alb_tls_cert_arn    = var.tls_certificate_arn
}
