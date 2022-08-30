# Debug on Local Kubernetes(on Docker for Mac)

First, enable the Kubernetes option in Docker for Mac(Enable Kubernetes).
Also check the resource settings for Docker for Mac. You must give it sufficient resources.

## Push the Docker Image

Please push the image to docker local repository.

```shell
tools/scripts $ ./sbt-publish-local.sh
```

## Edit the Configuration file of Helmfile

```shell
tools/scripts $ vi ../config/environments/${PREFIX}-${APPLICATION_NAME}-local.yaml
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

## Abount akka-cluster roles

The following two akka-cluster roles are defined for write-api-server.

- frontend: Write API server endpoints and use-cases etc
- backend: Aggregate Actors etc.

There are two ways to launch applications that take these roles into account

1. Launch two roles (Frontend, Backend) in each k8s deployments
2. Launch ｋ8ｓ deployment for Backend that also serves as Frontend(Frontend-only deployment/pods do not need to be launched)

1 is a configuration similar to a production environment, but requires a minimum of 5 pods, so system resources must be reasonable.
2 is a different configuration from the production environment, but can be started in 3 pods, which minimizes system resource consumption.


If choose the configuration 1, set as follows:

```
writeApi.writeApiServer.frontend.enabled = true
writeApi.writeApiServer.backend.withFrontend = false
```

Conversely, if choose the configuration 2, set as follows:

```
writeApi.writeApiServer.frontend.enabled = false
writeApi.writeApiServer.backend.withFrontend = true
```

## Deploy Backend role

Next deploy the backend role.

```shell
tools/scripts $ ./helmfile-apply-local-backend.sh
```

## Deploy Frontend role

if choose the configuration 1, deploy the frontend role.(if choose the configuration 2, Do not run this command)


```shell
tools/scripts $ ./helmfile-apply-local-frontend.sh
```

## Check the applications

Wait a few moments for the cluster to form. Make sure there are no errors in the log.

```shell
$ stern 'write-api-server-*' -n adceet
```

After frontend is started, check the operation with the following commands.

```shell
$ curl -X GET http://localhost:30030/hello
Say hello to akka-http
```

Call API to check operation.

```shell
$ curl -v -X POST -H "Content-Type: application/json" -d "{ \"accountId\": \"01G41J1A2GVT5HE45AH7GP711P\" }" http://localhost:30031/threads
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

