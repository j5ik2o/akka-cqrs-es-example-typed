- https://github.com/kubernetes/dashboard
- https://github.com/kubernetes/dashboard/tree/master/aio/deploy/helm-chart/kubernetes-dashboard
- https://docs.aws.amazon.com/ja_jp/eks/latest/userguide/dashboard-tutorial.html

### Applying ClusterRoleBinding

```shell
$ kubectl apply -f kubernetes-dashboard-clusterrolebinding.yaml
```

### Port-forwarding  

```shell
$ DASHBOARD_NS=kubernetes-dashboard
$ export POD_NAME=$(kubectl get pods -n $DASHBOARD_NS -l "app.kubernetes.io/name=kubernetes-dashboard,app.kubernetes.io/instance=kubernetes-dashboard" -o jsonpath="{.items[0].metadata.name}")
$ kubectl -n $DASHBOARD_NS port-forward $POD_NAME 8443:8443
```

### Configuring Chrome

1. Open `chrome://flags/#allow-insecure-localhost`
2. `Allow invalid certificates for resources loaded from localhost.` is `Enable`
3. Relaunch
4. Open `https://localhost:8443`

### Get token

```shell
$ DASHBOARD_NS=kubernetes-dashboard
$ kubectl -n $DASHBOARD_NS describe secret $(kubectl -n $DASHBOARD_NS get secret | grep kubernetes-dashboard-token | awk '{print $1}')
```