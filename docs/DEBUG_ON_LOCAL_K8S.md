# Debug on Local Kubernetes(on Docker for Mac)

First, enable the Kubernetes option in Docker for Mac(Enable Kubernetes).
Also check the resource settings for Docker for Mac. You must give it sufficient resources.

Please push the image to ECR repository.

```shell
tools/scripts $ ./sbt-ecr-push.sh
```

```shell
$ vi tools/config/environments/${PREFIX}-${APPLICATION_NAME}-local.yaml
```

Please set the following items in the yaml file appropriately

- writeApi.writeApiServer.frontend.image.repository
- writeApi.writeApiServer.frontend.image.tag
- writeApi.writeApiServer.backend.image.repository
- writeApi.writeApiServer.backend.image.tag

Next deploy the dynamodb local.

```shell
tools/scripts $ ./helmfile-apply-local-dynamodb.sh
tools/scripts $ ./dynamodb-create-tables.sh
```

Next deploy the backend roles.

```shell
tools/scripts $ ./helmfile-apply-local-backend.sh
```

Wait a few moments for the cluster to form. Make sure there are no errors in the log.

```shell
$ stern 'write-api-server-*' -n adceet
```

After frontend is started, check the operation with the following commands.

```shell
$ curl -X GET http://localhost:30030/hello
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

