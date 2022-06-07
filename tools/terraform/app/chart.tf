data "template_file" "service_account" {
  count = var.create ? 1 : 0
  template = file("${path.module}/templates/service_account.yaml.tpl")
  vars = {
    aws_account_id = data.aws_caller_identity.self.account_id
    service_namespace = local.k8s_service_namespace
    service_account_name = local.k8s_service_account_name
    iam_role_name = local.iam_role_name
  }
  depends_on = [
    data.template_file.namespace
  ]
}

data "template_file" "namespace" {
  count = var.create ? 1 : 0
  template = file("${path.module}/templates/namespace.yaml.tpl")
  vars = {
    service_namespace = local.k8s_service_namespace
  }
  depends_on = [
    module.iam_assumable_role_admin
  ]
}

resource "local_file" "service_account" {
  count = var.create ? 1 : 0
  content = element(concat(data.template_file.service_account.*.rendered, [""]), 0)
  filename = pathexpand("${path.module}/../../deployments/${var.prefix}/service_account.yaml")
  file_permission = 666
}

resource "local_file" "namespace" {
  count = var.create ? 1 : 0
  content = element(concat(data.template_file.namespace.*.rendered, [""]), 0)
  filename = pathexpand("${path.module}/../../deployments/${var.prefix}/namespace.yaml")
  file_permission = 666
}
