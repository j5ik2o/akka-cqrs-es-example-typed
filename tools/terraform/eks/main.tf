#module "cluster-autoscaler" {
#  source = "./cluster-autoscaler"
#  create = var.create_eks
#  aws_region = var.aws_region
#  prefix = var.prefix
#  eks_cluster_id = module.eks.cluster_id
#  eks_cluster_version = module.eks.cluster_version
#  eks_cluster_oidc_issuer_url = module.eks.cluster_oidc_issuer_url
#  dependencies = [module.eks.kubeconfig]
#}
#
#module "aws-alb-ingress-controller" {
#  source = "./aws-alb-ingress-controller"
#  create = var.create_eks
#  aws_region = var.aws_region
#  vpc_id = var.vpc_id
#  prefix = var.prefix
#  eks_cluster_id = module.eks.cluster_id
#  eks_cluster_version = module.eks.cluster_version
#  eks_cluster_oidc_issuer_url = module.eks.cluster_oidc_issuer_url
#  dependencies = [module.eks.kubeconfig]
#}
#
#module "app" {
#  source = "./app"
#  create = var.create_eks
#  prefix = var.prefix
#  eks_cluster_id = module.eks.cluster_id
#  eks_cluster_version = module.eks.cluster_version
#  eks_cluster_oidc_issuer_url = module.eks.cluster_oidc_issuer_url
#  dependencies = [module.eks.kubeconfig]
#}