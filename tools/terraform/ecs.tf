module "ecs" {
  count                    = var.ecs_enabled ? 1 : 0

  source = "terraform-aws-modules/ecs/aws"

  name = "${var.prefix}-ecs-cluster-${var.name}"

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy = [
    {
      capacity_provider = "FARGATE_SPOT"
      weight            = "1"
    }
  ]

  tags = {
    Name        = "${var.prefix}-ecs-cluster-${var.name}"
    Environment = var.prefix
  }
}

resource "aws_iam_role" "ecs_task_execution_role" {
  count                    = var.ecs_enabled ? 1 : 0
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": [
          "ecs-tasks.amazonaws.com"
        ]
      },
      "Action": [
        "sts:AssumeRole"
      ]
    }
  ]
}
EOF
}

resource "aws_iam_policy" "akka-app_ecs_policy" {
  count                    = var.ecs_enabled ? 1 : 0
  name = "${var.prefix}-ecs-policy-${var.name}"
  path = "/"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecs:*",
        "iam:Get*",
        "iam:List*",
        "iam:PassRole",
        "ecs:ListClusters",
        "ecs:ListContainerInstances",
        "ecs:DescribeContainerInstances",
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "dynamodb:*"
      ],
      "Resource": "*"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "akka-app-attach_ec2_policy" {
  count                    =var.ecs_enabled ? 1 : 0
  role       = aws_iam_role.ecs_task_execution_role[0].name
  policy_arn = aws_iam_policy.akka-app_ecs_policy[0].arn
}

resource "aws_cloudwatch_log_group" "log_group" {
  count                    = var.ecs_enabled ? 1 : 0
  name  = "/ecs/logs/${var.prefix}-${var.name}"
}

resource "aws_ecs_task_definition" "akka-app" {
  count                    = var.ecs_enabled ? 1 : 0
  family                   = "${var.prefix}-ecs-task-def-${var.name}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  task_role_arn            = aws_iam_role.ecs_task_execution_role[0].arn
  execution_role_arn       = aws_iam_role.ecs_task_execution_role[0].arn
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  container_definitions    = <<EOF
[
  {
    "name": "${var.prefix}-${var.name}",
    "essential": true,
    "image": "${module.ecr_akka_app.aws_ecr_repository_url}:${var.image_tag}",
    "environment": [
      { "name": "ROOT_LOG_LEVEL", "value": "INFO" },
      { "name": "JVM_HEAP_MIN", "value": "${var.jvm_heap_min}" },
      { "name": "JVM_HEAP_MAX", "value": "${var.jvm_heap_max}" },
      { "name": "JVM_META_MAX", "value": "${var.jvm_meta_max}" },
      { "name": "AWS_REGION", "value": "${data.aws_region.current.name}" },
      { "name": "CONFIG_RESOURCE", "value": "production.conf" },
      { "name": "KAMON_ENVIRONMENT_TAGS_ENV", "value": "${var.prefix}-${var.name}" },
      { "name": "AKKA_DISCOVERY_AWS_API_ECS_SERVICE", "value": "${local.ecs_service_name}" },
      { "name": "AKKA_DISCOVERY_AWS_API_ECS_CLUSTER", "value": "${module.ecs[0].ecs_cluster_name}" },
      { "name": "AKKA_CLUSTER_SHARDING_NUMBER_OF_SHARDS", "value": "${var.number_of_shards}" },
      { "name": "J5IK2O_DYNAMO_DB_JOURNAL_TABLE_NAME", "value": "${module.akka.aws_dynamodb_table_journal_table_name}" },
      { "name": "J5IK2O_DYNAMO_DB_JOURNAL_GET_JOURNAL_ROWS_INDEX_NAME", "value": "${module.akka.aws_dynamodb_table_journal_gsi_name}" },
      { "name": "J5IK2O_DYNAMO_DB_SNAPSHOT_TABLE_NAME", "value": "${module.akka.aws_dynamodb_table_snapshot_table_name}" }
    ],
    "portMappings": [
      { "containerPort": ${var.container_port}, "hostPort": ${var.container_port} },
      { "containerPort": ${var.akka_management_port}, "hostPort": ${var.akka_management_port} },
      { "containerPort": ${var.akka_remote_port}, "hostPort": ${var.akka_remote_port} }
    ],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "${aws_cloudwatch_log_group.log_group[0].name}",
        "awslogs-region": "${data.aws_region.current.name}",
        "awslogs-stream-prefix": "${var.prefix}-${var.name}"
      }
    },
    "dockerLabels": {
      "com.datadoghq.ad.instances": "[{\"host\": \"%%host%%\", \"port\": ${var.container_port}}]",
      "com.datadoghq.ad.check_names": "[\"${var.prefix}-${var.name}\"]",
      "com.datadoghq.ad.init_configs": "[{}]"
    }
  },
  {
    "name": "datadog-agent",
    "essential": true,
    "image": "datadog/agent:latest",
    "memoryReservation": 256,
    "cpu": 10,
    "environment": [
      { "name": "ECS_FARGATE", "value": "true" },
      { "name": "DD_PROCESS_AGENT_ENABLED", "value": "true" },
      { "name": "DD_DOGSTATSD_NON_LOCAL_TRAFFIC", "value": "true" },
      { "name": "DD_APM_ENABLED", "value": "true" },
      { "name": "DD_APM_NON_LOCAL_TRAFFIC", "value": "true" },
      { "name": "DD_API_KEY", "value": "${var.datadog-agent-key}" }
    ],
    "portMappings": [
      { "containerPort": 8125, "protcol": "udp", "hostPort": 8125 },
      { "containerPort": 8126, "protcol": "tcp", "hostPort": 8126 }
    ],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group":  "${aws_cloudwatch_log_group.log_group[0].name}",
        "awslogs-region": "${data.aws_region.current.name}",
        "awslogs-stream-prefix": "datadog-agent"
      }
    }
  }
]
EOF

  depends_on = [
    module.akka
  ]
}

resource "aws_ecs_service" "akka_http_service" {
  count    = var.ecs_enabled ? 1 : 0
  name     = local.ecs_service_name
  cluster  = module.ecs[0].ecs_cluster_id

  task_definition = aws_ecs_task_definition.akka-app[0].arn

  desired_count   = 3

  network_configuration {
    security_groups = [module.security_groups.ecs_tasks_id]
    subnets = module.vpc.public_subnets
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = module.alb.aws_alb_target_group_arn
    container_name   = "${var.prefix}-${var.name}"
    container_port   = var.container_port
  }

#  deployment_maximum_percent         = 100
#  deployment_minimum_healthy_percent = 0
}


