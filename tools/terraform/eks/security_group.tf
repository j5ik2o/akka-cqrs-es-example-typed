resource "aws_security_group" "worker_group_mgmt_one" {
  count = var.create_eks ? 1 : 0
  name_prefix = "worker_group_mgmt_one"
  vpc_id = var.vpc_id

  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"

    cidr_blocks = [
      "10.0.0.0/8",
    ]
  }
}

resource "aws_security_group" "all_worker_mgmt" {
  count = var.create_eks ? 1 : 0
  name_prefix = "all_worker_management"
  vpc_id = var.vpc_id

  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"

    cidr_blocks = [
      "10.0.0.0/8",
      "172.16.0.0/12",
      "192.168.0.0/16",
    ]
  }
}
