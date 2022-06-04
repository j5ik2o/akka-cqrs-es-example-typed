locals {
  ecs_service_name = "${var.prefix}-ecs-service-${var.name}"
  eks_cluster_name   = replace("${var.prefix}-eks-${var.name}", "_", "-")

  tags = {
    Example    = local.eks_cluster_name
    GithubRepo = "terraform-aws-eks"
    GithubOrg  = "terraform-aws-modules"
  }

  aws_auth_roles = [{
    rolearn  = aws_iam_role.admin_role.arn
    username = "system_masters"
    groups   = ["system:masters"]
  }]
}