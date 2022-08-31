# Debug on Minikube

First, Give minikube driver(docker, virtualbox, ...) enough resources.

## Launch Minikube

```shell
tools/scripts $ minikube-start.sh
```

Change the Docker client connection to minikube.

```shell
tools/scripts $ eval $(minikube docker-env default)
```

## Push the Docker Image

Please push the image to docker registry on minikube.

```shell
tools/scripts $ ./sbt-publish-local.sh
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

## Prepare DynamoDB tabels

Next deploy the dynamodb local.

```shell
tools/scripts $ ./helmfile-apply-local-dynamodb.sh
```

Create the necessary tables.

```shell
tools/scripts $ ./dynamodb-create-tables.sh
```

## [About akka-cluster roles](DEBUG_ON_LOCAL_K8S.md#about-akka-cluster-roles)

## Deploy the Backend role

Next deploy the backend roles.

```shell
tools/scripts $ ./helmfile-apply-local-backend.sh
```

Wait a few moments for the cluster to form. Make sure there are no errors in the log.

```shell
$ stern 'write-api-server-backend-*' -n adceet
```

Make sure all pods are in Ready status.


## Deploy the Frontend role

if choose the configuration 1, deploy the frontend role.(if choose the configuration 2, Do not run this command)

```shell
tools/scripts $ ./helmfile-apply-local-frontend.sh
```

Wait a few moments for the cluster to form. Make sure there are no errors in the log.

```shell
$ stern 'write-api-server-frontend-*' -n adceet
```

Make sure all pods are in Ready status.


## Check the applications

After frontend is started, check the operation with the following commands.

```shell
$ curl -X GET http://127.0.0.1:30031/hello
Say hello to akka-http
```

Call API to check operation.

```shell
$ curl -v -X POST -H "Content-Type: application/json" -d "{ \"accountId\": \"01G41J1A2GVT5HE45AH7GP711P\" }" http://127.0.0.1:30031/threads
Note: Unnecessary use of -X or --request, POST is already inferred.
*   Trying 127.0.0.1:30031...
* Connected to localhost (127.0.0.1) port 30031 (#0)
> POST /threads HTTP/1.1
> Host: localhost:30031
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

