resource "aws_dynamodb_table" "journal-table" {
  count = var.enabled ? 1 : 0
  name = "${var.prefix}-${var.journal_name}"

  hash_key  = "pkey"
  range_key = "skey"

  billing_mode   = "PROVISIONED"
  read_capacity  = var.journal_read_capacity
  write_capacity = var.journal_write_capacity

  # DynamoDB Streamsのシャードを8とかにしたい場合は、↓を指定するといい
  # write_capacity = 4000

  attribute {
    name = "pkey"
    type = "S"
  }

  attribute {
    name = "skey"
    type = "S"
  }

  attribute {
    name = "persistence-id"
    type = "S"
  }

  attribute {
    name = "sequence-nr"
    type = "N"
  }

  /*
    attribute {
      name = "tags"
      type = "S"
    }
  */

  global_secondary_index {
    name            = "${var.prefix}-${var.journal_gsi_name}"
    hash_key        = "persistence-id"
    range_key       = "sequence-nr"
    write_capacity  = 1800
    read_capacity   = 400
    projection_type = "ALL"
  }

  /*
    global_secondary_index {
      name            = "${var.prefix}-TagsIndex"
      hash_key        = "tags"
      write_capacity  = 1800
      read_capacity   = 1
      projection_type = "ALL"
    }
  */

  stream_enabled = true
  stream_view_type = "NEW_IMAGE"

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name  = "${var.prefix}-${var.journal_name}"
    Owner = var.owner
  }

}

resource "aws_dynamodb_table" "snapshot-table" {
  count = var.enabled ? 1 : 0
  name = "${var.prefix}-${var.snapshot_name}"

  hash_key  = "persistence-id"
  range_key = "sequence-nr"

  billing_mode   = "PROVISIONED"
  read_capacity  = var.snapshot_read_capacity
  write_capacity = var.snapshot_write_capacity
  # DynamoDB Streamsのシャードを8とかにしたい場合は、↓を指定するといい
  # write_capacity = 4000

  attribute {
    name = "persistence-id"
    type = "S"
  }

  attribute {
    name = "sequence-nr"
    type = "N"
  }

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name  = "${var.prefix}-${var.snapshot_name}"
    Owner = var.owner
  }

}

#module "s3_bucket" {
#  source = "terraform-aws-modules/s3-bucket/aws"
#
#  bucket = "${var.prefix}-${var.snapshot_name}"
#  acl    = "private"
#
#  tags = {
#    Name  = "${var.prefix}-${var.snapshot_name}"
#    Owner = var.owner
#  }
#
#}