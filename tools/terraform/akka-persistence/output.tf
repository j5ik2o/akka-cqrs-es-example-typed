output "aws_dynamodb_table_journal_table_name" {
  value = concat(aws_dynamodb_table.journal-table.*.name, [""])[0]
}

output "aws_dynamodb_table_journal_gsi_name" {
  value = "${var.prefix}-${var.journal_gsi_name}"
}

output "aws_dynamodb_table_snapshot_table_name" {
  value = concat(aws_dynamodb_table.snapshot-table.*.name, [""])[0]
}