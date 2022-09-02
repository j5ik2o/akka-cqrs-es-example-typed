# Debug on Local Machine

## Run the Docker Compose for DynamoDB Local & DynamoDB Admin

Launch dynamodb-local & dynamodb-admin as docker-compose.

```shell
tools/docker-compose $ ./docker-compose-up.sh -d
```

## Debug by using IntelliJ IDEA

Edit Configurations

com.github.j5ik2o.api.write.Main

1. PORT=8081;AKKA_MANAGEMENT_HTTP_PORT=8558;JMX_PORT=8100
2. PORT=8082;AKKA_MANAGEMENT_HTTP_PORT=8559;JMX_PORT=8101
3. PORT=8083;AKKA_MANAGEMENT_HTTP_PORT=8560;JMX_PORT=8102

Create a configuration for all three and run it in IntelliJ IDEA. If you want to debug, run any one of the projects in debug.

## Operation verification

Make sure there are no unreachable nodes.

```shell
$ curl -s -X GET http://localhost:8558/cluster/members | jq .
{
  "leader": "akka://adceet@127.0.0.1:44431",
  "members": [
    {
      "node": "akka://adceet@172.31.0.4:44431",
      "nodeUid": "6606923497015772855",
      "roles": [
        "frontend",
        "backend",
        "dc-default"
      ],
      "status": "Up"
    },
    {
      "node": "akka://adceet@172.31.0.5:35433",
      "nodeUid": "-188067320620564535",
      "roles": [
        "frontend",
        "backend",
        "dc-default"
      ],
      "status": "Up"
    },
    {
      "node": "akka://adceet@172.31.0.6:35163",
      "nodeUid": "8133025084706588976",
      "roles": [
        "frontend",
        "backend",
        "dc-default"
      ],
      "status": "Up"
    }
  ],
  "oldest": "akka://adceet@172.31.0.4:44431",
  "oldestPerRole": {
    "frontend": "akka://adceet@172.31.0.4:44431",
    "backend": "akka://adceet@172.31.0.4:44431",
    "dc-default": "akka://adceet@172.31.0.4:44431"
  },
  "selfNode": "akka://adceet@172.31.0.4:44431",
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
* Connected to localhost (127.0.0.1) port 30031 (#0)
> POST /threads HTTP/1.1
> Host: localhost:18080
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
