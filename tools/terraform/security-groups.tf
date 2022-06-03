module "security_groups" {
  source         = "./security-groups"
  name           = "${var.prefix}-sg-${var.name}"
  vpc_id         = module.vpc.vpc_id
  environment    = "${var.prefix}-sg-${var.name}"
  container_port = var.container_port
  akka_management_port = var.akka_management_port
  akka_remote_port = var.akka_remote_port
}