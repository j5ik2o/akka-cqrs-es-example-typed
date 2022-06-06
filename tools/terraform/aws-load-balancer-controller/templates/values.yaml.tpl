clusterName: ${eks_cluster_name}
region: ${aws_region}
vpcId: ${vpc_id}
serviceAccount:
  name: ${service_account_name}
  annotations:
    eks.amazonaws.com/role-arn: "arn:aws:iam::${aws_account_id}:role/${iam_role_name}"