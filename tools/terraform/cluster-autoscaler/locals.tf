locals {
  chart_name = "cluster-autoscaler"
  image_repository = "k8s.gcr.io/autoscaling/cluster-autoscaler"
  image_tag = "v1.23.0"
  k8s_service_namespace = var.k8s_service_namespace
  k8s_service_account_name = var.k8s_service_account_name
  iam_policy_name_prefix = "${var.prefix}-cluster-autoscaler"
  iam_role_name =  "${var.prefix}-cluster-autoscaler"
}