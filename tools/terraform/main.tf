terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.10.0"
    }
    eksctl = {
      source  = "mumoshu/eksctl"
      version = "0.15.1"
    }
  }
  backend "s3" {
  }
}

provider "eksctl" {}

data "aws_region" "current" {
}

resource "random_string" "suffix" {
  length  = 6
  special = false
}

data "aws_availability_zones" "available" {
}