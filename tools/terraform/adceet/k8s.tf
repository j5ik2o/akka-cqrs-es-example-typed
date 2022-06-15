resource "kubernetes_namespace" "adceet" {
  metadata {
    name = local.k8s_service_namespace
  }
}

resource "kubernetes_service_account" "adceet-write-api-server" {
  metadata {
    name = local.k8s_service_account_name
    namespace = local.k8s_service_namespace
    annotations = {
      "eks.amazonaws.com/role-arn" = "arn:aws:iam::${data.aws_caller_identity.self.account_id}:role/${local.iam_role_name}"
    }
  }
  depends_on = [
    kubernetes_namespace.adceet
  ]
}