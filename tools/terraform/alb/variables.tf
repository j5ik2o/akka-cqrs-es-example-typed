variable "enabled" {
  default = false
  type = bool
}
variable "name" {
  description = "the name of your stack, e.g. \"demo\""
}

variable "prefix" {
  description = "the name of your environment, e.g. \"prod\""
}

variable "subnets" {
  description = "Comma separated list of subnet IDs"
  type = list(string)
}

variable "vpc_id" {
  description = "VPC ID"
}

variable "alb_security_groups" {
  description = "Comma separated list of security groups"
}

#variable "alb_tls_cert_arn" {
#  description = "The ARN of the certificate that the ALB uses for https"
#}
#

variable "health_check_port" {
  type = number
}

variable "health_check_path" {
  description = "Path to check if the service is healthy, e.g. \"/status\""
  type = string
}