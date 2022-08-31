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
  "leader": "akka://adceet@172.31.0.4:44431",
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

### Testing endpoints

- hello

```shell
$ curl -s -X GET http://localhost:18080/hello
```
