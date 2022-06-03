variable "aws_profile" {
  default = "cwtest"
}

variable "aws_region" {
  default = "ap-northeast-1"
}

variable "create_eks" {
  type = bool
  default = false
}

variable "vpc_id" {
  default = ""
}

variable "subnet_ids" {
  default = ""
}
variable "create" {
  default = true
}

variable "prefix" {
  default = "default"
}

variable "eks_version" {
  default = "1.15"
}

variable "eks_cluster_name" {}

variable "vpc_security_group_ids" {
  type = list(string)
}

variable "eks_auth_accounts" {
  description = "Additional AWS account numbers to add to the aws-auth configmap."
  type        = list(string)

  default = [
    "777777777777",
    "888888888888",
  ]
}

variable "eks_auth_roles" {
  description = "Additional IAM roles to add to the aws-auth configmap."
  type = list(object({
    rolearn  = string
    username = string
    groups   = list(string)
  }))

  default = [
    {
      rolearn  = "arn:aws:iam::66666666666:role/role1"
      username = "role1"
      groups   = ["system:masters"]
    },
  ]
}

variable "eks_auth_users" {
  description = "Additional IAM users to add to the aws-auth configmap."
  type = list(object({
    userarn  = string
    username = string
    groups   = list(string)
  }))

  default = [
    {
      userarn  = "arn:aws:iam::66666666666:user/user1"
      username = "user1"
      groups   = ["system:masters"]
    },
    {
      userarn  = "arn:aws:iam::66666666666:user/user2"
      username = "user2"
      groups   = ["system:masters"]
    },
  ]
}

variable "eks_asg_desired_capacity" {
  default = "3"
}
variable "eks_asg_max_size" {
  default = "10"
}
variable "eks_node_instance_type" {
  default = "t2.medium"
}
variable "eks_asg_min_size" {
  default = "3"
}

variable "eks_root_volume_type" {
  default = "gp2"
}
