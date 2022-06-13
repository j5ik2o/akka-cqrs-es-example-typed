apiVersion: v1
kind: ServiceAccount
metadata:
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::${aws_account_id}:role/${iam_role_name}
  name: ${service_account_name}
  namespace: ${service_namespace}
