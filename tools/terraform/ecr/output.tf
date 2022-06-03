output "aws_ecr_repository_arn" {
  value = aws_ecr_repository.this.arn
}

output "aws_ecr_repository_url" {
  value = aws_ecr_repository.this.repository_url
}