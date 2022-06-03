module "ecr_akka_app" {
  source   = "./ecr"
  prefix   = var.prefix
  owner    = var.owner
  enabled  = true
  name     = var.name
}
