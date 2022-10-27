# Deploy to Kubernetes(on Docker for Mac)

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
adceet-root # tools/config/environments/${PREFIX}-${APPLICATION_NAME}-local.yaml
```

Notice the tag value displayed in the console; reflect it in xxx.image.tag.
Please set the following items in the yaml file appropriately

- writeApi.writeApiServer.frontend.image.repository
- writeApi.writeApiServer.frontend.image.tag
- writeApi.writeApiServer.backend.image.repository
- writeApi.writeApiServer.backend.image.tag

Set the following items in the yaml file appropriately(if you use Read API Server)

- readModelUpdater.image.repository
- readModelUpdater.image.tag
- readApiServer.image.repository
- readApiServer.image.tag

---

**NOTE**

All components can be deployed with a single command below, but it is recommended that you run each step at least once to get a feel for the process.

```shell
tools/scripts $ ./helmfile-apply-local-all.sh
```

---

## Prepare DynamoDB tabels

Next deploy dynamodb local.

```shell
tools/scripts $ ./helmfile-apply-local-dynamodb.sh
```

Create the necessary tables.

```shell
tools/scripts $ ./helmfile-apply-local-dynamodb-setup.sh
```

## Prepare MySQL tabels

Next deploy mysql.

```shell
tools/scripts $ ./helmfile-apply-local-mysql.sh
```

Create the necessary tables.

```shell
tools/scripts $ ./helmfile-apply-local-flyway.sh
```

## About akka-cluster roles

The following two akka-cluster roles are defined for write-api-server.

- frontend: Write API server endpoints and use-cases etc
- backend: Aggregate Actors etc.

There are two ways to launch applications that take these roles into account

1. Launch two roles (Frontend, Backend) in each k8s deployments
2. Launch k8s deployment for Backend that also serves as Frontend(Frontend-only deployment/pods do not need to be launched)

1 is a configuration similar to a production environment, but requires a minimum of 5 pods, so system resources must be reasonable.
2 is a different configuration from the production environment, but can be started in 3 pods, which minimizes system resource consumption.


If choose the configuration 1, set the values in the helmfile configuration file(`tools/config/environments/${PREFIX}-${APPLICATION_NAME}-local.yaml`) as follows:

```
writeApi.writeApiServer.frontend.enabled = true
writeApi.writeApiServer.backend.withFrontend.enabled = false
```

Conversely, if choose the configuration 2, set as follows:

```
writeApi.writeApiServer.frontend.enabled = false
writeApi.writeApiServer.backend.withFrontend.enabled = true
```

## Deploy the Backend role

Next deploy the backend role.

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

Make sure all pods are in Ready status

## Deploy Read Model Updater (if you need) 

Next deploy Read Model Updater.

```shell
tools/scripts $ ./helmfile-apply-local-rmu.sh
```

## Deploy Read API Server (if you need)

Next deploy Read API Server

```shell
tools/scripts $ ./helmfile-apply-local-read-api.sh
```

## Check the applications

After frontend is started, check the operation with the following commands.

```shell
$ curl -X GET http://localhost:30031/hello
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

Execute the following command if you use RMU and Read API Server.

```shell
$ curl -v -H "Content-Type: application/json" http://localhost:30033/threads?owner_id=01G41J1A2GVT5HE45AH7GP711P
*   Trying 127.0.0.1:30033...
* Connected to localhost (127.0.0.1) port 30033 (#0)
> GET /threads?owner_id=01G41J1A2GVT5HE45AH7GP711P HTTP/1.1
> Host: localhost:30033
> User-Agent: curl/7.79.1
> Accept: */*
> Content-Type: application/json
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Server: akka-http/10.2.9
< Date: Tue, 25 Oct 2022 07:59:31 GMT
< Content-Type: application/json
< Content-Length: 123
<
* Connection #0 to host localhost left intact
[{"id":"01GG72CT9B62DRMH31F8SQX3H9","owner_id":"01G41J1A2GVT5HE45AH7GP711P","created_at":"2022-10-25T07:58:31.096808590Z"}]%
```