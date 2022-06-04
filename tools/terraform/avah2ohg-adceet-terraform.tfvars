akka_persistence_journal_name     = "Journal"
akka_persistence_journal_gsi_name = "GetJournalRowsIndex"
akka_persistence_snapshot_name    = "Snapshot"

container_port       = 8081
akka_management_port = 8558
akka_remote_port     = 25520
number_of_shards     = 30

health_check_port = 8558
health_check_path = "/health/ready"

alb_enabled              = true
akka_persistence_enabled = false
ecs_enabled              = false

eks_enabled      = true

# https://docs.aws.amazon.com/ja_jp/AmazonECS/latest/developerguide/AWS_Fargate.html
ecs_task_cpu    = "512"
ecs_task_memory = "4096"

jvm_heap_min = "1024m"
jvm_heap_max = "1024m"
jvm_meta_max = "512m"