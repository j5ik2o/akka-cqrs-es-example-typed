module "adceet-ecr-write-api-server-kotlin" {
  source   = "./ecr"
  enabled = var.ecr_enabled
  prefix   = var.prefix
  application_name = var.name
  owner    = var.owner
  name     = "write-api-server-kotlin"
}

module "adceet-ecr-write-api-server-scala" {
  source   = "./ecr"
  enabled = var.ecr_enabled
  prefix   = var.prefix
  application_name = var.name
  owner    = var.owner
  name     = "write-api-server-scala"
}
