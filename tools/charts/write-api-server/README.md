# akka

[Akka](https://akka.io/) is a scala application that makes it easier to build powerful reactive, concurrent, and distributed applications.
This chart is generic for deploying the akka application.

## TL;DR;

```
$ helm install chatwork/akka
```

## Prerequisites

* Kubernetes 1.14+

## Installing the Chart

To install the chart with the release name `my-release`:

```
$ helm install --name my-release chatwork/akka
```

The command deploys the akka chart on the Kubernetes cluster in the default configuration. The [configuration](https://github.com/chatwork/charts/tree/master/akka#configuration) section lists the parameters that can be configured during installation.

## Uninstalling the Chart

To uninstall the `my-release` deployment:

```
$ helm uninstall my-release
```

The command removes all the Kubernetes components associated with the chart and deletes the release.

## Configuration

The following table lists the configurable parameters of the akka chart and their default values.

|  Parameter | Description | Default |
| --- | --- | --- |
| `image.repository` | The image repository to pull from | `"kinzal/akkasvis"` |
| `image.tags` | The image tag to pull | `"latest"` |
| `image.pullPolicy` | Image pull policy | `"IfNotPresent"` |
| `imagePullSecrets` | Image pull secrets | `[]` |
| `nameOverride` | Override name of app | `""` |
| `fullnameOverride` | Override full name of app | `""` |
| `akka.loglevel` | Log level used by the configured loggers (see "loggers") as soon | `"INFO"` |
| `akka.actor.allowJavaSerialization` | Enable java serialization | `"true"` |
| `akka.cluster.enabled` | If it is true, set akka.actor.provider to cluster. To use this feature, you need to include akka-cluster in your application's dependencies | `true` |
| `akka.discovery.enabled` | If it is true, set akka.discovery. To use this feature, you need to include akka-cluster in your application dependencies | `true` |
| `akka.discovery.method` | Specify the API to be used for "akka-dns", "kubernetes-api", and "aggregate" discovery. If you want to use kubernetes-api, include akka-discovery-kubernetes-api in your application dependencies | `"kubernetes-api"` |
| `akka.discovery.aggregate.discoveryMethods` | If akka.discovery.method is "aggregate", specify the discovery method to aggregate. | `[]` |
| `akka.http.enabled` | If it is true, set akka.http and container port, and service, and ingress. To use this feature, you need to include akka-http in your application dependencies | `true` |
| `akka.http.hostname` | Hostname to bind http | `0.0.0.0` |
| `akka.http.port` | Port to bind http | `8080` |
| `akka.http.portName` | Name of the port to bind the http to | `http` |
| `akka.http.extraPorts` | Extra ports to bind http | `[]` |
| `akka.http.service.enabled` | If true, create a service for akka.http.port and akka.http.extraPorts | `false` |
| `akka.http.service.annotations` | Annotations to be added to service | `{}` |
| `akka.http.service.labels` | Labels to be added to service | `{}` |
| `akka.http.service.ports` | The service ports for http | `[]` |
| `akka.http.service.type` | The service type for http | `"NodePort"` |
| `akka.http.ingresses[].annotations` | Annotations to be added to ingress | `nil` |
| `akka.http.ingresses[].labels` | Labels to be added to ingress | `nil` |
| `akka.http.ingresses[].name` | The name of the Ingress. You create will be in the `{{ include "akka.fullname" . }}-{{ .Values.akka.http.ingresses[].name }}` format | `nil` |
| `akka.http.ingresses[].tls` | TLS configuration for the ingress | `nil` |
| `akka.http.ingresses[].hosts` | A list of hosts for the ingresss | `nil` |
| `akka.http.ingresses[].targetPort` | The target port of the service | `nil` |
| `akka.http.ingresses[].ingressClass` | Specifies ingressClass you use ingress controller. This feature is available since 1.18 and impremented by ingress controller | `nil` |
| `akka.management.enabled` | If it is true, set akka.management and liveness/readiness probe, and headless service. To use this feature, you need to include akka-management and akka-management-cluster-bootstrap in your application dependencies | `true` |
| `akka.management.hostname` | Hostname to bind management | `0.0.0.0` |
| `akka.management.port` | Port to bind management | `8558` |
| `akka.management.cluster.bootstrap.contactPointDiscovery.serviceName` | Define this name to be looked up in service discovery for "neighboring" nodes.  | `` |
| `akka.management.cluster.bootstrap.contactPointDiscovery.requiredContactPointNr` | The smallest number of contact points that need to be discovered before the bootstrap process can start | `3` |
| `akka.management.healthChecks.livenessPath` | When exposting health checks via Akka Management, the path to expost liveness checks on | `"alive"` |
| `akka.management.healthChecks.readinessPath` | When exposting health checks via Akka Management, the path to expost readiness checks on | `"ready"` |
| `akka.management.healthChecks.startupPath` | When exposting health checks via Akka Management, the path to expost startup checks on | `"ready"` |
| `akka.management.service.enabled` | If true, create a service for akka.management.port | `false` |
| `akka.management.service.annotations` | Annotations to be added to service | `{}` |
| `akka.management.service.labels` | Labels to be added to service | `{}` |
| `akka.management.service.clusterIP` | Static "clusterIP" or "None" for headless services | `` |
| `akka.management.service.publishNotReadyAddresses` | If true, it will register the pod with the service before the pod becomes ready. This is useful when using akka-dns in the discovery api. | `false` |
| `akka.management.service.type` | The service type for management | `"ClusterIP"` |
| `akka.remote.enabled` | If it is true, set akka.remote. To use this feature, you need to include akka-remote in your application's dependencies | `true` |
| `akka.remote.hostname` | Hostname to bind remote | `"0.0.0.0"` |
| `akka.remote.port` | Port to bind remote | `25520` |
| `affinity` | Deployment affinities | `{}` |
| `annotations` | Annotations to be added to deployment | `{}` |
| `containerName` | Override container name of akka application | `""` |
| `command` | Additional command arguments | `[]` |
| `env` | Extra environment variables that will be passed onto pods | `[]` |
| `envFrom` | Extra environment from that will be passed onto pods | `[]` |
| `extraContainers` | Extra containers variable that will be passed onto pods | `[]` |
| `extraVolumes` | Extra volumes variables that will be passed onto pods | `[]` |
| `extraVolumeMounts` | Extra volume mounts variables that will be passed onto pods | `[]` |
| `labels` | labels to be added to deployment | `{}` |
| `lifecycle` | Lifecycle hook script to execute | `{}` |
| `livenessProbe.enabled` | Enable liveness probes | `true` |
| `livenessProbe.httpGet` | Specify the endpoint to check the liveness probe with http. This can be omitted if akka.management.enabled is true | `{}` |
| `livenessProbe.tcpSocket` | Specify the endpoint to check the liveness probe with tco. This can be omitted if akka.management.enabled is true | `{}` |
| `livenessProbe.exec` | Specify the command to check the liveness probe with exec. This can be omitted if akka.management.enabled is true | `{}` |
| `livenessProbe.initialDelaySeconds` | Delay before liveness probe is initiated | `15` |
| `livenessProbe.periodSeconds` | How often to perform the probe | `5` |
| `livenessProbe.timeoutSeconds` | When the probe times out | `1` |
| `livenessProbe.successThreshold` | Minimum number of consecutive successes for a probe to be considered successful after a probe has failed | `1` |
| `livenessProbe.failureThreshold` | Minimum number of consecutive successes for the probe to be considered successful after having failed | `3` |
| `startupProbe.enabled` | Enable startup probes. This feature is available since 1.18 | `false` |
| `startupProbe.httpGet` | Specify the endpoint to check the startup probe with http. This can be omitted if akka.management.enabled is true | `{}` |
| `startupProbe.tcpSocket` | Specify the endpoint to check the startup probe with tco. This can be omitted if akka.management.enabled is true | `{}` |
| `startupProbe.exec` | Specify the command to check the startup probe with exec. This can be omitted if akka.management.enabled is true | `{}` |
| `startupProbe.initialDelaySeconds` | Delay before start probe is initiated | `15` |
| `startupProbe.periodSeconds` | How often to perform the probe | `5` |
| `startupProbe.timeoutSeconds` | When the probe times out | `1` |
| `startupProbe.successThreshold` | Minimum number of consecutive successes for a probe to be considered successful after a probe has failed | `1` |
| `startupProbe.failureThreshold` | Minimum number of consecutive successes for the probe to be considered successful after having failed | `3` |
| `nodeSelector` | Node labels for pod assignment | `{}` |
| `podAnnotations` | Annotations to be added to pods | `{}` |
| `podLabels` | Labels to be added to pods | `{}` |
| `podSecurityContext` | Security context policies to add to the controller pod | `{}` |
| `priorityClassName` | Priority class name | `""` |
| `readinessGate` | The status of the pod will be ready when condition is true. | `[]` |
| `readinessProbe.enabled` | Enable readiness probes | `true` |
| `readinessProbe.httpGet` | Specify the endpoint to check the readiness probe with http. This can be omitted if akka.management.enabled is true | `{}` |
| `readinessProbe.tcpSocket` | Specify the endpoint to check the readiness probe with tcp. This can be omitted if akka.management.enabled is true | `{}` |
| `readinessProbe.exec` | Specify the command to check the readiness probe with exec. This can be omitted if akka.management.enabled is true | `{}` |
| `readinessProbe.initialDelaySeconds` | Delay before readiness probe is initiated | `15` |
| `readinessProbe.periodSeconds` | How often to perform the probe | `5` |
| `readinessProbe.timeoutSeconds` | When the probe times out | `1` |
| `readinessProbe.successThreshold` | Minimum number of consecutive successes for a probe to be considered successful after a probe has failed | `1` |
| `readinessProbe.failureThreshold` | Minimum number of consecutive successes for the probe to be considered successful after having failed | `3` |
| `replicaCount` | Number of replicas | `3` |
| `revisionHistoryLimit` | The number of old ReplicaSets to retain to allow rollback. | `10` |
| `resources` | Pod resource requests & limits | `{}` |
| `restartPolicy` | Container restart policy | `"Always"` |
| `securityContext` | Allows you to overwrite the default securityContext applied to the container | `{}` |
| `strategy` | Specifies the strategy used to replace old pods by new ones | `{}` |
| `topologySpreadConstraints` | Set topology spread constraints to control how Pods are spread across your cluster. This feature is available since 1.18 | `[]` |
| `terminationGracePeriodSeconds` | Termination grace period (in seconds) | `60` |
| `tolerations` | Node taints to tolerate | `[]` |
| `rbac.create` | If true, create & use RBAC resources | `true` |
| `serviceAccount.create` | If true, create a service account for the pod | `true` |
| `serviceAccount.annotations` | Annotations for the created service account | `{}` |
| `serviceAccount.labels` | Labels for the created service account | `{}` |
| `serviceAccount.name` | The name of the service account to use. If not set and create is true, a name is generated using the fullname template | `` |
| `podDisruptionBudget.enabled` | If true, create a pod disruption budget for keeper pods | `false` |
| `podDisruptionBudget.annotations` | Annotations for the created pod disruption budget | `{}` |
| `podDisruptionBudget.labels` | Labels for the created pod disruption budget | `{}` |
| `podDisruptionBudget.maxUnavailable` | Maximum number / percentage of pods that should remain scheduled | `` |
| `podDisruptionBudget.minAvailable` | Minimum number / percentage of pods that should remain scheduled | `` |
| `autoscaling.enabled` | If true, create a horizontal pod autoscaler for autoscaling | `false` |
| `autoscaling.annotations` | Annotations for the created horizontal pod autoscaler | `{}` |
| `autoscaling.labels` | Labels for the created horizontal pod autoscaler | `{}` |
| `autoscaling.minReplicas` | Min pods for HorizontalPodAutoscaler | `` |
| `autoscaling.maxReplicas` | Max pods for HorizontalPodAutoscaler | `` |
| `autoscaling.metrics` | Metrics used for HorizontalPodAutoscaler | `[]` |
| `autoscaling.behavior` | Behavior for HorizontalPodAutoscaler.  This feature is available since 1.18 | `{}` |
| `metrics.enabled` | If true, enable Prometheus metrics | `false` |
| `metrics.port` | Listen port | `9095` |
| `metrics.path` | Metrics HTTP endpoint | `"/"` |
| `configmaps` | Define configuration files and environment variables that can be expanded by tpl function | `{...}` |
| `useResourceApplicationConf` | If true, mount the file defined in configmaps in /opt/conf and set -Dconfig.file=/opt/conf/chart.conf in args. It is recommended to set this to false if you are using compiled application.conf, using an older version supported by chart, or for scala applications that do not use akka | `true` |
| `secrets` | Define secret files and environment variables that can be expanded by tpl function | `{}` |
| `test.enabled` | If true, create a test pods | `false` |
| `test.annotations` | Annotations for the created test pods | `{}` |
| `test.labeld` | Labels for the created test pods | `{}` |
| `test.spec` | Test pod spec | `{}` |

## Akka Configuration file

There are several ways to inject the configuration file in this chart.

### 1. Using the configuration file generated by chart

If `useResourceApplicationConf` is `true`, it passes `chart.conf` of configmaps as an argument.

**values.yaml**
```yaml
useResourceApplicationConf: true
```

**result**
```yaml
containers:
  - name: akka
    image: "kinzal/akkavis:latest"
    imagePullPolicy: "IfNotPresent"
    args:
      - "-Dconfig.file=/opt/conf/chart.conf"
    ...
    volumeMounts:
      - name: akka-conf
        mountPath: /opt/conf/akka.conf
        subPath: akka.conf
      - name: chart-conf
        mountPath: /opt/conf/chart.conf
        subPath: chart.conf
      - name: kamon-conf
        mountPath: /opt/conf/kamon.conf
        subPath: kamon.conf
```

This method is safe because the settings of chart are linked to the settings of akka and kamon.
We recommend this method unless there is a special reason to do so.

### 2. Using the customize configuration file

If you have a setting that is not supported by this chart, you can rewrite the setting itself.

**values.yaml**
```yaml
useResourceApplicationConf: true
configmaps:
  chart.yaml: |
    my.param = 1

  akka.conf: |
    ...

  kamon.conf: |
    ...
```

NOTE: If you have any missing settings, please create a PR.

### 3. Using bundled configuration files

If there are other configuration files bundled with the JAR, you can also use them.

**values.yaml**
```yaml
command:
  - /opt/docker/bin/akkavis
  - -Dconfig.resource=prod.conf

useResourceApplicationConf: false
configmaps:
  chart.yaml: ""
  akka.conf: ""
  kamon.conf: ""
```

**result**
```yaml
containers:
  - name: akka
    image: "kinzal/akkavis:latest"
    imagePullPolicy: "IfNotPresent"
    command:
      - /opt/docker/bin/akkavis
      - -Dconfig.resource=prod.conf
```

This does not link the configuration file to the chart configuration.
Therefore, please specify `values.yaml` so that the port numbers and so on are the same.

### 4. Using environment variables

If the bundled configuration file reads environment variables, you can inject the configuration with environment variables.

**application.conf**
```hocon
akkavis {
  hostname = "0.0.0.0"
  hostname = ${?AKKAVIS_HOSTNAME}
  port = 8080
  port = ${?AKKAVIS_PORT}
}
```

**values.yaml**
```yaml
env:
  - name: AKKAVIS_HOSTNAME
    value: "0.0.0.0"
  - name: AKKAVIS_PORT
    value: "8080"

useResourceApplicationConf: false
configmaps:
  chart.yaml: ""
  akka.conf: ""
  kamon.conf: ""
```

## Akka discovery API

### 1.kubernetes-api

If you are building an akka cluster on a single kubernetes cluster, please use `kubernetes-api` unless you have a special reason to do so.

**values.yaml**
```yaml
akka:
  cluster:
    enabled: true
  discovery:
    enabled: true
    method: kubernetes-api
  management:
    enabled: true
    service:
      enabled: false
  remote:
    enabled: true

replicaCount: 3

rbac:
  create: true

serviceAccount:
  create: true

useResourceApplicationConf: true
```

Related Link:

- [Kubernetes API - Akka Management](https://doc.akka.io/docs/akka-management/current/bootstrap/kubernetes-api.html)

### 2. akka-dns

This Chart supports `akka-dns`.
We recommend using `kubernetes-api` if you don't have a special use case to use DNS.

**values.yaml**
```yaml
akka:
  cluster:
    enabled: true
  discovery:
    enabled: true
    method: akka-dns
  management:
    enabled: true
    service:
      enabled: true
      clusterIP: None
      publishNotReadyAddresses: true
  remote:
    enabled: true

replicaCount: 3

useResourceApplicationConf: true
```

Related Link:

- [Kubernetes via DNS - Akka Management](https://doc.akka.io/docs/akka-management/current/bootstrap/kubernetes.html)

### 3. aggregate

You can also use both akka-dns and kubernetes-api.
This can be used when building a single akka cluster with multiple kubernetes clusters, such as when migrating a kubernetes cluster.

NOTE: This sample only works with EKS

**values.yaml**
```yaml
akka:
  cluster:
    enabled: true
  discovery:
    enabled: true
    method: aggregate
    aggregate:
      discoveryMethods:
        - akka-dns
        - kubernetes-api
  http:
    enabled: true
    service:
      enabled: true
      port: 8080
  management:
    enabled: true
    port: 8558
    cluster:
      bootstrap:
        contactPointDiscovery:
          serviceName: "[your management domain]"
    service:
      enabled: true
      annotations:
        external-dns.alpha.kubernetes.io/hostname: "[your management domain]"
        external-dns.alpha.kubernetes.io/ttl: "1"
      clusterIP: None
  remote:
    enabled: true
    port: 25520

rbac:
  create: true

serviceAccount:
  create: true

useResourceApplicationConf: true
```

The kubernetes cluster migration works as follows:

1. Build an akka cluster on `Cluster A`
    - Initially, DNS is not registered, so we will form a cluster with `kubernetes-api`
2. Build an akka cluster on `Cluster B`
    - Since `Cluster A`'s DNS is registered, join `Cluster A` with `akka-dns`
    - Due to the external-dns specification, `Cluster B`'s DNS will not be registered
3. Remove an akka cluster on `Cluster A`
    - The DNS for `Cluster A` will be removed
    - `Cluster A`'s DNS has disappeared, so `Cluster B`'s DNS is registered

NOTE: This method requires you to prepare an [external-dns](https://github.com/kubernetes-sigs/external-dns)

## Resource limit

## Memory

Akka recommends that the [option be set](https://doc.akka.io/docs/akka/current/additional/deploying.html#memory) to match the java version.

```yaml
command:
  - /opt/docker/bin/akkavis
  - -XX:+UnlockExperimentalVMOptions
  - -XX:+UseCGroupMemoryLimitForHeap
```

NOTE: `-XX` is supported by a docker image create with sbt-native-packager. If the image is made in any other way, please deal with it on the image side

## CPU

Akka recommends that you specify [only resource.request](https://doc.akka.io/docs/akka/current/additional/deploying.html#deploying-to-kubernetes).
