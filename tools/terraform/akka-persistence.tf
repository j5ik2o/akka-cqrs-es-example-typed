module "akka" {
  source = "./akka-persistence"
  enabled = var.akka_persistence_enabled
  prefix = var.prefix
  owner = var.owner
  journal_name = var.akka_persistence_journal_name
  snapshot_name = var.akka_persistence_snapshot_name
  journal_gsi_name = var.akka_persistence_journal_gsi_name
  journal_read_capacity = var.akka_persistence_journal_read_capacity
  journal_write_capacity = var.akka_persistence_journal_write_capacity
  snapshot_read_capacity = var.akka_persistence_snapshot_read_capacity
  snapshot_write_capacity = var.akka_persistence_snapshot_write_capacity

}