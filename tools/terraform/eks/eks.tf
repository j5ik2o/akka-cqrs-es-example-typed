module "eks" {
  source = "terraform-aws-modules/eks/aws"
  version = "~> 18.0"

  create = var.create_eks

  cluster_name = var.eks_cluster_name
  cluster_version = var.eks_version

  cluster_endpoint_private_access = true
  cluster_endpoint_public_access  = true

  cluster_addons = {
    coredns = {
      resolve_conflicts = "OVERWRITE"
    }
    kube-proxy = {}
    vpc-cni = {
      resolve_conflicts = "OVERWRITE"
    }
  }

  cluster_encryption_config = [{
    provider_key_arn = aws_kms_key.eks.arn
    resources        = ["secrets"]
  }]

  vpc_id = var.vpc_id
  subnet_ids = var.subnet_ids

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


  # Self Managed Node Group(s)
  self_managed_node_group_defaults = {
    vpc_security_group_ids       = [aws_security_group.all_worker_mgmt[0].id]
    iam_role_additional_policies = ["arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"]
  }

  self_managed_node_groups = {
    spot = {
      instance_type = "m5.large"
      instance_market_options = {
        market_type = "spot"
      }

      pre_bootstrap_user_data = <<-EOT
      echo "foo"
      export FOO=bar
      EOT

      bootstrap_extra_args = "--kubelet-extra-args '--node-labels=node.kubernetes.io/lifecycle=spot'"

      post_bootstrap_user_data = <<-EOT
      cd /tmp
      sudo yum install -y https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/linux_amd64/amazon-ssm-agent.rpm
      sudo systemctl enable amazon-ssm-agent
      sudo systemctl start amazon-ssm-agent
      EOT
    }
  }

  # EKS Managed Node Group(s)
  eks_managed_node_group_defaults = {
    ami_type       = "AL2_x86_64"
    instance_types = [var.eks_node_instance_type]

    attach_cluster_primary_security_group = true
    vpc_security_group_ids                = [aws_security_group.all_worker_mgmt[0].id]
  }

  eks_managed_node_groups = {
    blue = {}
    green = {
      min_size     = 1
      max_size     = 10
      desired_size = 1

      instance_types = ["t3.large"]
      capacity_type  = "SPOT"
      labels = {
        Environment = "test"
        GithubRepo  = "terraform-aws-eks"
        GithubOrg   = "terraform-aws-modules"
      }

      taints = {
        dedicated = {
          key    = "dedicated"
          value  = "gpuGroup"
          effect = "NO_SCHEDULE"
        }
      }

      update_config = {
        max_unavailable_percentage = 50 # or set `max_unavailable`
      }

      tags = {
        ExtraTag = "example"
      }
    }
  }

  tags = {
    Environment = "test"
    GithubRepo = "terraform-aws-eks"
    GithubOrg = "terraform-aws-modules"
  }

#  kubeconfig_aws_authenticator_env_variables = {
#    AWS_PROFILE = var.aws_profile
#  }
#
#  worker_groups = [
#    {
#      name = "app"
#      instance_type = var.eks_node_instance_type
#      asg_max_size = var.eks_asg_max_size
#      asg_min_size = var.eks_asg_min_size
#      asg_desired_capacity = var.eks_asg_desired_capacity
#      root_volume_type = var.eks_root_volume_type
#      tags = [
#        {
#          key = "k8s.io/cluster-autoscaler/enabled"
#          propagate_at_launch = "true"
#          value = "true"
#        },
#        {
#          key = "k8s.io/cluster-autoscaler/${var.eks_cluster_name}"
#          propagate_at_launch = "true"
#          value = "owned"
#        }
#      ]
#    }
#  ]

#  worker_additional_security_group_ids = [
#    element(concat(aws_security_group.all_worker_mgmt[*].id, list("")), 0)]

//  workers_additional_policies = [
//    "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess",
//    "arn:aws:iam::aws:policy/AmazonS3FullAccess",
//    "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
//  ]

#  config_output_path = "./config/"

  manage_aws_auth_configmap = true

  aws_auth_roles = var.eks_auth_roles
  aws_auth_users = var.eks_auth_users
  aws_auth_accounts = var.eks_auth_accounts

}

resource "aws_kms_key" "eks" {
  description             = "EKS Secret Encryption Key"
  deletion_window_in_days = 7
  enable_key_rotation     = true

#  tags = local.tags
}