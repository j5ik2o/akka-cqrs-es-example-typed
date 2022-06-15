module "akka" {
  source = "./akka-persistence"
  enabled = var.akka_persistence_enabled
  prefix = var.prefix
  owner = var.owner
  journal_name = var.akka_persistence_journal_name
  journal_gsi_name = var.akka_persistence_journal_gsi_name
  snapshot_name = var.akka_persistence_snapshot_name
  snapshot_gsi_name = var.akka_persistence_snapshot_gsi_name
}