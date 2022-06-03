locals {
  chart_name = "aws-alb-ingress-controller"
  k8s_service_namespace = var.k8s_service_namespace
  k8s_service_account_name = var.k8s_service_account_name
  iam_policy_name_prefix = "${var.prefix}-eks-${local.chart_name}"
  iam_role_name =  "${var.prefix}-eks-${local.chart_name}"
}