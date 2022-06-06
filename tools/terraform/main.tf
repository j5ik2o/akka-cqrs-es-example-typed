terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.10.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.11.0"
    }
  }
  backend "s3" {
  }
}

data "aws_region" "current" {
}

resource "random_string" "suffix" {
  length  = 6
  special = false
}

data "aws_availability_zones" "available" {
}

data "aws_caller_identity" "current" {
}