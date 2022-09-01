# Deploy to EKS

First, enable the Kubernetes option in Docker for Mac(Enable Kubernetes).
Also check the resource settings for Docker for Mac. You must give it sufficient resources.

## Push the Docker Image

Please push the image to docker local repository.

```shell
tools/scripts $ ./sbt-ecr-push.sh
```

## Edit the Configuration file of Helmfile

```shell
tools/scripts $ vi ../config/environments/${PREFIX}-${APPLICATION_NAME}-local.yaml
adceet-root # tools/config/environments/${PREFIX}-${APPLICATION_NAME}-local.yaml
```

Notice the tag value displayed in the console; reflect it in xxx.image.tag.
Please set the following items in the yaml file appropriately

- writeApi.writeApiServer.frontend.image.repository
- writeApi.writeApiServer.frontend.image.tag
- writeApi.writeApiServer.backend.image.repository
- writeApi.writeApiServer.backend.image.tag

## [About akka-cluster roles](DEBUG_ON_LOCAL_K8S.md#about-akka-cluster-roles)

## Deploy the Backend role

Next deploy the backend role.

```shell
tools/scripts $ ./helmfile-apply-eks-backend.sh
```

Wait a few moments for the cluster to form. Make sure there are no errors in the log.

```shell
$ stern 'write-api-server-backend-*' -n adceet
```

Make sure all pods are in Ready status.

## Deploy the Frontend role

if choose the configuration 1, deploy the frontend role.(if choose the configuration 2, Do not run this command)

```shell
tools/scripts $ ./helmfile-apply-eks-frontend.sh
```

Wait a few moments for the cluster to form. Make sure there are no errors in the log.

```shell
$ stern 'write-api-server-frontend-*' -n adceet
```

Make sure all pods are in Ready status

## Check the applications

After frontend is started, check the operation with the following commands.

```shell
$ curl -X GET https://xxxxxx/hello
Say hello to akka-http
```

Call API to check operation.

```shell
$ curl -v -X POST -H "Content-Type: application/json" -d "{ \"accountId\": \"01G41J1A2GVT5HE45AH7GP711P\" }" https://xxxxxx/threads
Note: Unnecessary use of -X or --request, POST is already inferred.
*   Trying xxxxxx:433...
* Connected to xxxxxx (x.x.x.x) port 443 (#0)
> POST /threads HTTP/1.1
> Host: xxxxxx:443
> User-Agent: curl/7.79.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 45
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Server: akka-http/10.2.9
< Date: Fri, 26 Aug 2022 08:42:39 GMT
< Content-Type: application/json
< Content-Length: 41
<
* Connection #0 to host localhost left intact
{"threadId":"01GBCN25M496HB4PK9EWQMH28J"}
```

