#locals {
#  cluster_name = "${var.prefix}-${var.eks_cluster_name}-${random_string.suffix.result}"
#}
#
#module "eks" {
#  source = "./eks"
#  create_eks = var.eks_enabled
#  aws_profile = var.aws_profile
#  aws_region = var.aws_region
#
#  prefix = var.prefix
#
#  vpc_id = module.vpc.vpc_id
#  eks_cluster_name = local.cluster_name
#  subnet_ids = module.vpc.private_subnets
#
#  vpc_security_group_ids = [module.vpc.default_security_group_id]
#
#  eks_node_instance_type = var.eks_node_instance_type
#  eks_asg_desired_capacity = var.eks_asg_desired_capacity
#  eks_asg_max_size = var.eks_asg_max_size
#  eks_asg_min_size = var.eks_asg_min_size
#
#  eks_auth_users = var.eks_auth_users
#  eks_auth_roles = var.eks_auth_roles
#  eks_auth_accounts = var.eks_auth_accounts
#
#  eks_root_volume_type = var.eks_root_volume_type
#}

provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

  exec {
    api_version = "client.authentication.k8s.io/v1alpha1"
    command     = "aws"
    # This requires the awscli to be installed locally where Terraform is executed
    args = ["eks", "get-token", "--cluster-name", module.eks.cluster_id]
  }
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "18.23.0"

  create = var.eks_enabled

  cluster_name    = local.eks_cluster_name
  cluster_version = var.eks_version

  enable_irsa = true

  cluster_endpoint_private_access = true
  cluster_endpoint_public_access  = true

  cluster_addons = {
    coredns = {
      resolve_conflicts = "OVERWRITE"
    }
    kube-proxy = {}
    vpc-cni    = {
      resolve_conflicts = "OVERWRITE"
    }
  }

  cluster_encryption_config = [
    {
      provider_key_arn = aws_kms_key.eks.arn
      resources        = ["secrets"]
    }
  ]

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  # Extend cluster security group rules
  cluster_security_group_additional_rules = {
    egress_nodes_ephemeral_ports_tcp = {
      description                = "To node 1025-65535"
      protocol                   = "tcp"
      from_port                  = 1025
      to_port                    = 65535
      type                       = "egress"
      source_node_security_group = true
    }
  }

  # Extend node-to-node security group rules
  node_security_group_additional_rules = {
    ingress_self_all = {
      description = "Node to node all ports/protocols"
      protocol    = "-1"
      from_port   = 0
      to_port     = 0
      type        = "ingress"
      self        = true
    }
    egress_all = {
      description      = "Node all egress"
      protocol         = "-1"
      from_port        = 0
      to_port          = 0
      type             = "egress"
      cidr_blocks      = ["0.0.0.0/0"]
      ipv6_cidr_blocks = ["::/0"]
    }
  }

  eks_managed_node_groups = {
    initial = {
      capacity_type = "ON_DEMAND"
      instance_types = ["t3.medium"]

      min_size     = 1
      max_size     = 3
      desired_size = 1

      labels         = {
        Environment = "test"
        GithubRepo  = "terraform-aws-eks"
        GithubOrg   = "terraform-aws-modules"
      }

    }
  }


  tags = {
    Environment = "test"
    GithubRepo  = "terraform-aws-eks"
    GithubOrg   = "terraform-aws-modules"
  }

  manage_aws_auth_configmap = true

  aws_auth_roles    = local.aws_auth_roles

}

resource "aws_iam_role" "admin_role" {
  name               = "${var.prefix}-eks-admin-${var.name}"
  tags               = local.tags
  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
  POLICY
}

resource "aws_kms_key" "eks" {
  description             = "EKS Secret Encryption Key"
  deletion_window_in_days = 7
  enable_key_rotation     = true

  tags = local.tags
}

resource "aws_security_group" "additional" {
  count       = var.eks_enabled ? 1 : 0
  name_prefix = "${var.prefix}-additional"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port = 22
    to_port   = 22
    protocol  = "tcp"

    cidr_blocks = [
      "10.0.0.0/8",
      "172.16.0.0/12",
      "192.168.0.0/16",
    ]
  }

  tags = local.tags
}
