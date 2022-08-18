module "adceet-ecr-write-api-server-kotlin" {
  source   = "./ecr"
  prefix   = var.prefix
  application_name = var.name
  owner    = var.owner
  enabled  = true
  name     = "write-api-server-kotlin"
}

module "adceet-ecr-write-api-server-scala" {
  source   = "./ecr"
  prefix   = var.prefix
  application_name = var.name
  owner    = var.owner
  enabled  = true
  name     = "write-api-server-scala"
}
