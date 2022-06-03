variable "aws_region" {
  default = "ap-northeast-1"
}

variable "aws_profile" {
  default = "test1"
}

variable "prefix" {
  default = "test1"
}

variable "name" {
  default = "kamon-example"
}

variable "owner" {
  default = "test1"
}

variable "vpc_cidr" {
  default = "10.0.0.0/16"
}

variable "aws_subnet_public" {
  type = list(string)
  default = [
    "10.0.1.0/24",
    "10.0.2.0/24",
    "10.0.3.0/24"]
}

variable "aws_subnet_private" {
  type = list(string)
  default = [
    "10.0.4.0/24",
    "10.0.5.0/24",
    "10.0.6.0/24"]
}

variable "aws_subnet_database" {
  type = list(string)
  default = [
    "10.0.20.0/24",
    "10.0.21.0/24"]
}

variable "health_check_port" {
  type = number
}

variable "health_check_path" {
  type = string
}

variable "container_port" {
  type = number
}

variable "akka_management_port" {
  type = number
}

variable "akka_remote_port" {
  type = number
}
variable "akka_persistence_enabled" {
  type = bool
}

variable "akka_persistence_journal_name" {
  type = string
}

variable "akka_persistence_journal_gsi_name" {
  type = string
}

variable "akka_persistence_snapshot_name" {
  type = string
}

variable "akka_persistence_journal_read_capacity" {}
variable "akka_persistence_journal_write_capacity" {}
variable "akka_persistence_snapshot_read_capacity" {}
variable "akka_persistence_snapshot_write_capacity" {}

variable "number_of_shards" {
  type = number
}

variable "ecs_task_enabled" {
  default = false
}

variable "alb_enabled" {
  default = false
}

variable "image_tag" {
  type = string
  default = ""
}

variable "ecs_task_cpu" {
  type = string
}

variable "ecs_task_memory" {
  type = string
}

variable "jvm_heap_min" {
  type = string
}

variable "jvm_heap_max" {
  type = string
}

variable "jvm_meta_max" {
  type = string
}

variable "datadog-agent-key" {
  type = string
  default = ""
}

variable "eks_cluster_name" {
  type = string
}