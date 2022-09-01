# CQRS+ES Example by using akka-cluster-typed

Status: WIP

This is a example for CQRS+ES(Event Sourcing).

## Concepts

- DDD-based: The Write API Server has aggregate actors in the domain module.
- CQRS+ES(Event Sourcing): The Write API Server supports the Command side, The Read API Server supports the Query side.

## TODO

- [x] Write API Server(on akka cluster) 
  - [x] Implementations
  - [x] Docker Compose Support
  - [x] Deployment to Local Kubernetes(k8s for mac/minikube)
  - [x] Deployment to AWS EKS
- [ ] Read Model Updater(without akka cluster) 
  - [ ] Implementations
  - [ ] Docker Compose Support
  - [ ] Deployment to Local Kubernetes(k8s for mac/minikube)
  - [ ] Deployment to AWS EKS
- [ ] Read API Server(without akka cluster)
  - [ ] Implementations
  - [ ] Docker Compose Support
  - [ ] Deployment to Local Kubernetes(k8s for mac/minikube)
  - [ ] Deployment to AWS EKS

## [The Tools Installation](docs/TOOLS_INSTALLATION.md)

## [AWS Setup](docs/AWS_SETUP.md)

## [Debug on Local Machine](docs/DEBUG_ON_LOCAL_MACHINE.md)

## [Debug on Docker Compose](docs/DEBUG_ON_DOCKER_COMPOSE.md)

## [Deploy to Local Kubernetes(on Docker for Mac)](docs/DEPLOY_TO_LOCAL_K8S.md)

## [Deploy to Minikube](docs/DEPLOY_TO_MINIKUBE.md)

## [Deploy to EKS](docs/DEPLOY_TO_EKS.md)
