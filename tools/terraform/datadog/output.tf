output "datadog_secret_key_arn" {
  value = element(concat(aws_kms_key.datadog-secret.*.arn, [""]), 0)
}