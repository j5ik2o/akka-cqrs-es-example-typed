locals {
  cluster_name = "${var.prefix}-${var.eks_cluster_name}-${random_string.suffix.result}"
}

module "eks" {
  source = "./eks"
  create_eks = var.eks_enabled
  aws_profile = var.aws_profile
  aws_region = var.aws_region

  prefix = var.prefix

  vpc_id = module.vpc.vpc_id
  eks_cluster_name = local.cluster_name
  subnet_ids = module.vpc.private_subnets

  vpc_security_group_ids = [module.vpc.default_security_group_id]

  eks_node_instance_type = var.eks_node_instance_type
  eks_asg_desired_capacity = var.eks_asg_desired_capacity
  eks_asg_max_size = var.eks_asg_max_size
  eks_asg_min_size = var.eks_asg_min_size

  eks_auth_users = var.eks_auth_users
  eks_auth_roles = var.eks_auth_roles
  eks_auth_accounts = var.eks_auth_accounts

  eks_root_volume_type = var.eks_root_volume_type
}
