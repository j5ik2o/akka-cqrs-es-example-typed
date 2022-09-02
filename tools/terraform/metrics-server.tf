resource "helm_release" "metrics-server" {
  name  = "metrics-server"
  chart = "https://github.com/kubernetes-sigs/metrics-server/releases/download/metrics-server-helm-chart-3.8.2/metrics-server-3.8.2.tgz"
  namespace = "kube-system"

  lifecycle {
    create_before_destroy = true
  }

  set {
    name = "containerPort"
    value = 443
  }

  depends_on = [
    module.eks
  ]
}
