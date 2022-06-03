
data "template_file" "chart_values" {
  count = var.create ? 1 : 0
  template = file("${path.module}/templates/values.yaml.tpl")
  vars = {
    image_repository = local.image_repository
    image_tag = local.image_tag
    aws_region = var.aws_region
    aws_account_id = data.aws_caller_identity.self.account_id
    eks_cluster_name  = var.eks_cluster_id
    service_account_name = local.k8s_service_account_name
    iam_role_name = local.iam_role_name
  }
  depends_on = [
    aws_iam_policy.cluster_autoscaler
  ]
}

resource "local_file" "chart_values" {
  count = var.create ? 1 : 0
  content = element(concat(data.template_file.chart_values[*].rendered, list("")), 0)
  filename = pathexpand("${path.module}/../../../helmfile/${local.chart_name}/environments/${var.prefix}-values.yaml")
  file_permission = 666
}
