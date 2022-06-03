output "cluster_name" {
  value = module.eks.cluster_id
}

#output "kubeconfig_filename" {
#  value = module.eks.kubeconfig_filename
#}

#output "cluster_autoscaler_iam_role_name" {
#  value = module.cluster-autoscaler.iam_role_name
#}
#
#output "cluster_autoscaler_iam_role_arn" {
#  value = module.cluster-autoscaler.iam_role_arn
#}
#
#output "cluster_autoscaler_iam_role_path" {
#  value = module.cluster-autoscaler.iam_role_path
#}
#
#output "alb_ingress_controller_iam_role_name" {
#  value = module.cluster-autoscaler.iam_role_name
#}
#
#output "alb_ingress_controller_iam_role_arn" {
#  value = module.cluster-autoscaler.iam_role_arn
#}
#
#output "alb_ingress_controller_iam_role_path" {
#  value = module.cluster-autoscaler.iam_role_path
#}
#
