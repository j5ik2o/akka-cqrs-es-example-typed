resource "aws_kms_key" "datadog-secret" {
  count = var.create ? 1 : 0
  description             = "Datadog Secret Encryption Key"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = {
    Name = "${var.prefix}-datadog"
  }
}

data "template_file" "sops-yaml" {
  count = var.create ? 1 : 0
  template = file("${path.module}/templates/.sops.yaml.tpl")
  vars = {
    kms_key_arn = aws_kms_key.datadog-secret[0].arn
  }
}

data "template_file" "secrets-yaml" {
  count = var.create ? 1 : 0
  template = file("${path.module}/templates/secrets.yaml.tpl")
  vars = {
    datadog_api_key = var.datadog_api_key
  }
}

resource "local_file" "sops-yaml" {
  count = var.create ? 1 : 0
  content = element(concat(data.template_file.sops-yaml.*.rendered, [""]), 0)
  filename = pathexpand("${path.module}/../../deployments/datadog/.sops.yaml")
  file_permission = 666
}

resource "local_file" "secrets-yaml" {
  count = var.create ? 1 : 0
  content = element(concat(data.template_file.secrets-yaml.*.rendered, [""]), 0)
  filename = pathexpand("${path.module}/../../deployments/datadog/secrets.yaml")
  file_permission = 666

  depends_on = [
    local_file.sops-yaml
  ]

  provisioner "local-exec" {
    working_dir = pathexpand("${path.module}/../../deployments/datadog")
    command = "AWS_PROFILE=${var.aws_profile} helm secrets enc secrets.yaml"
  }
}
