# CQRS/Event Sourcing System Example by using Akka

This is a example for CQRS+ES(Event Sourcing).

## Concepts

- DDD-based: The Write API Server has aggregate actors in the domain module.
- CQRS+ES(Event Sourcing): The Write API Server supports the Command side, The Read API Server supports the Query side.

## Status

- WIP

- Scala 2.13.8
  - Write API Server is DONE
  - Read Model Updater is DONE
  - Read API Server is DONE
- Kotlin 1.6.21
  - Write API Server is DONE
  - Read Model Updater is TODO
  - Read API Server is TODO
- Java 17
  - Write API Server is WIP
  - Read Model Updater is TODO
  - Read API Server is TODO

Note: Akka is implemented in Scala, so even if you use Kotlin or Java, you may need to implement Scala in some parts of your application.

## Updates

- Modified JDK version from temurin-11 to temurin-17, **need to set `--enable-preview` in the environment variable `JAVA_OPTS`(`export JAVA_OPTS='--enable-prewview'`)**.


## Architecture

### Main Framework/Library

- Akka
  - "com.typesafe.akka" %% "akka-actor-typed"
  - "com.typesafe.akka" %% "akka-cluster-typed" 
  - "com.typesafe.akka" %% "akka-cluster-sharding-typed"
  - "com.typesafe.akka" %% "akka-persistence-typed" 
  - "com.typesafe.akka" %% "akka-http"
  - "com.lightbend.akka.management" %% "akka-management"
  - "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap"
  - "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api"
  - "com.github.j5ik2o" %% "akka-persistence-dynamodb-journal-v2"
  - "com.github.j5ik2o" %% "akka-persistence-dynamodb-snapshot-v2"
- ULID
  - "org.wvlet.airframe" %% "airframe-ulid"

#### Scala

- DI Container
  - "org.wvlet.airframe" %% "airframe"

#### Kotiln

- DI Container
  - "org.kodein.di" % "kodein-di-jvm"
- FP
  - "io.arrow-kt" % "arrow-core"
- Collection Library
  - "io.vavr" % "vavr-kotlin"

#### Java

- DI Container
  - "org.springframework.boot" % "spring-boot-starter"
- Collection Library
  - "io.vavr" % "vavr"


## [The Tools Installation](docs/TOOLS_INSTALLATION.md)

## Debug on Local Environment

### [Debug on Local Machine](docs/DEBUG_ON_LOCAL_MACHINE.md)

### [Debug on Docker Compose](docs/DEBUG_ON_DOCKER_COMPOSE.md)

### [Deploy to Local Kubernetes(on Docker for Mac)](docs/DEPLOY_TO_LOCAL_K8S.md)

### [Deploy to Minikube](docs/DEPLOY_TO_MINIKUBE.md)

## AWS Deployment

### [AWS Setup](docs/AWS_SETUP.md)

### [Deploy to EKS](docs/DEPLOY_TO_EKS.md)
