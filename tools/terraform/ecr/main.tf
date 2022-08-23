resource "aws_ecr_repository" "this" {
  name = "${var.prefix}-ecr-${var.application_name}-${var.name}"
  lifecycle {
    create_before_destroy = true
  }
  tags = {
    Name  = "${var.prefix}-ecr-${var.application_name}-${var.name}"
    Owner = var.owner
  }
}

resource "aws_ecr_repository_policy" "this" {
  policy = <<EOF
{
    "Version": "2008-10-17",
    "Statement": [
        {
            "Sid": "${aws_ecr_repository.this.name}-policy",
            "Effect": "Allow",
            "Principal": "*",
            "Action": [
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "ecr:BatchCheckLayerAvailability",
                "ecr:PutImage",
                "ecr:InitiateLayerUpload",
                "ecr:UploadLayerPart",
                "ecr:CompleteLayerUpload",
                "ecr:DescribeRepositories",
                "ecr:GetRepositoryPolicy",
                "ecr:ListImages",
                "ecr:DeleteRepository",
                "ecr:BatchDeleteImage",
                "ecr:SetRepositoryPolicy",
                "ecr:DeleteRepositoryPolicy"
            ]
        }
    ]
}
EOF

  repository = aws_ecr_repository.this.name
}