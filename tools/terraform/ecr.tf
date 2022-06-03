module "ecr_akka_http_kamon_example" {
  source   = "./ecr"
  prefix   = var.prefix
  owner    = var.owner
  enabled  = true
  name     = var.name
}
