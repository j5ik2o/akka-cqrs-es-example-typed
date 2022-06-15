module "adceet-ecr-write-api-server" {
  source   = "./ecr"
  prefix   = var.prefix
  owner    = var.owner
  enabled  = true
  name     = "write-api-server"
}
