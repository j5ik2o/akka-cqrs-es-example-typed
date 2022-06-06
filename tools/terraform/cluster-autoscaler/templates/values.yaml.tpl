awsRegion: ${aws_region}

# https://github.com/kubernetes/autoscaler/issues/2920
image:
  repository: ${image_repository}
  tag: ${image_tag}

rbac:
  create: true
  serviceAccount:
    name: ${service_account_name}
  serviceAccountAnnotations:
    eks.amazonaws.com/role-arn: "arn:aws:iam::${aws_account_id}:role/${iam_role_name}"

autoDiscovery:
  clusterName: ${eks_cluster_name}
  enabled: true

extraArgs:
  expander: least-waste
  balance-similar-node-groups: true
  skip-nodes-with-system-pods: false

podAnnotations:
  "cluster-autoscaler.kubernetes.io/safe-to-evict": "false"