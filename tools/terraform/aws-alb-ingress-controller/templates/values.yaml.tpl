clusterName: ${eks_cluster_name}
autoDiscoverAwsRegion: true
autoDiscoverAwsVpcID: true
awsRegion: ${aws_region}
awsVpcID: ${vpc_id}
rbac:
  serviceAccount:
    annotations:
      eks.amazonaws.com/role-arn: "arn:aws:iam::${aws_account_id}:role/${iam_role_name}"