resource "helm_release" "external-dns" {
  count = var.create ? 1 : 0
  name      = "external-dns"
  namespace = local.k8s_service_namespace
  chart     = "https://github.com/kubernetes-sigs/external-dns/releases/download/external-dns-helm-chart-1.11.0/external-dns-1.11.0.tgz"

  lifecycle {
    create_before_destroy = true
  }


  set {
    name  = "serviceAccount.name"
    value = local.k8s_service_account_name
  }

  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = "arn:aws:iam::${data.aws_caller_identity.self.account_id}:role/${local.iam_role_name}"
    type  = "string"
  }

  set {
    name = "provider"
    value = "aws"
  }

  set {
    name = "policy"
    value = "sync"
  }

  set {
    name = "txtOwnerId"
    value = aws_route53_zone.main.zone_id
    type = "string"
  }

  depends_on = [
    module.iam_assumable_role_admin
  ]
}