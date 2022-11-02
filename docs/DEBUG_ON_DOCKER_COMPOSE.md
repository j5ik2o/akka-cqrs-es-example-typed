# Debug on Docker Compose

## Build image

```shell
tools/scripts $ ./sbt-publish-local.sh
--- Using Environments -----------------
AWS_PROFILE      = adceet
AWS_REGION       = us-east-1
PREFIX           = om2eep1k
APPLICATION_NAME = adceet
MODE             = scala
----------------------------------------
[info] welcome to sbt 1.7.1 (Eclipse Adoptium Java 11.0.15)
# ...
[info] Built image adceet-write-api-server-scala with tags [1b3bac6c3fddc41c134b9e4bafc2579211c16996, latest]
[success] Total time: 10 s, completed 2022/08/18 10:32:04
```

The image name is `adceet-write-api-server-${MODE}` as fixed.

## Run the Docker Compose

```shell
tools/docker-compose $ ./docker-compose-up.sh
```

## Operation verification

### Akka Management

Make sure there are no unreachable nodes.

```shell
$ curl -s -X GET http://localhost:8558/cluster/members | jq .
{
  "leader": "akka://adceet@172.25.0.5:46693",
  "members": [
    {
      "node": "akka://adceet@172.25.0.5:46693",
      "nodeUid": "6449282681366255574",
      "roles": [
        "frontend",
        "backend",
        "dc-default"
      ],
      "status": "Up"
    },
    {
      "node": "akka://adceet@172.25.0.6:36927",
      "nodeUid": "929922260930827876",
      "roles": [
        "frontend",
        "backend",
        "dc-default"
      ],
      "status": "Up"
    },
    {
      "node": "akka://adceet@172.25.0.7:40933",
      "nodeUid": "-9123478749678732860",
      "roles": [
        "frontend",
        "backend",
        "dc-default"
      ],
      "status": "Up"
    }
  ],
  "oldest": "akka://adceet@172.25.0.5:46693",
  "oldestPerRole": {
    "frontend": "akka://adceet@172.25.0.5:46693",
    "backend": "akka://adceet@172.25.0.5:46693",
    "dc-default": "akka://adceet@172.25.0.5:46693"
  },
  "selfNode": "akka://adceet@172.25.0.5:46693",
  "unreachable": []
}
```

### Check the applications

Check the operation with the following commands.

```shell
$ curl -s -X GET http://localhost:18080/hello
Say hello to akka-http
```

Call API to check operation.

```shell
$ curl -v -X POST -H "Content-Type: application/json" -d "{ \"accountId\": \"01G41J1A2GVT5HE45AH7GP711P\" }" http://127.0.0.1:18080/threads
Note: Unnecessary use of -X or --request, POST is already inferred.
*   Trying 127.0.0.1:18080...
* Connected to 127.0.0.1 (127.0.0.1) port 18080 (#0)
> POST /threads HTTP/1.1
> Host: 127.0.0.1:18080
> User-Agent: curl/7.79.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 45
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Server: akka-http/10.2.9
< Date: Wed, 31 Aug 2022 02:46:42 GMT
< Content-Type: application/json
< Content-Length: 41
<
* Connection #0 to host 127.0.0.1 left intact
{"threadId":"01GBRWPCHEZKHX8QCR3226AGAM"}
```

**NOTE: In a local environment, the first event may not consume well. If this is the case, try sending the command again.**

Execute the following command if you use RMU and Read API Server.

```shell
$ curl -v -H "Content-Type: application/json" http://127.0.0.1:18082/threads?owner_id=01G41J1A2GVT5HE45AH7GP711P
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


