# DDD CQRS Event Sourcing by using akka-typed

Status: WIP

## TODO

- [ ] Write API Server(on akka cluster) 
  - [x] Implementations
  - [x] Docker Compose Support
  - [ ] Deployment to EKS (WIP)
  - [ ] Deployment to minikube
- [ ] Read Model Updater(without akka cluster) 
  - [ ] Implementations
  - [ ] Docker Compose Support
  - [ ] Deployment to EKS
  - [ ] Deployment to minikube
- [ ] Read API Server(without akka cluster)
  - [ ] Implementations
  - [ ] Docker Compose Support
  - [ ] Deployment to EKS
  - [ ] Deployment to minikube

## Tools

- awscli
- asdf
- jq
- java
- sbt
- terraform

## Installation of the tools

### asdf

```sh
$ brew install asdf
```

### jq

```shell
$ asdf plugin-add jq https://github.com/AZMCode/asdf-jq.git
$ asdf install jq 1.6
$ asdf local jq 1.6
$ jq --version
jq-1.6
```

### awscli

```shell
$ asdf plugin add awscli
$ asdf install awscli 2.7.6
$ asdf local  awscli 2.7.6
$ aws --version
aws-cli/2.7.6 Python/3.9.11 Darwin/21.5.0 exe/x86_64 prompt/off
```

### Terraform

https://github.com/hashicorp/terraform

```sh
adceet-root $ asdf plugin-add terraform https://github.com/asdf-community/asdf-hashicorp.git
adceet-root $ ASDF_HASHICORP_OVERWRITE_ARCH=amd64 asdf install terraform 1.2.1
adceet-root $ asdf local terraform 1.2.1
adceet-root $ terraform version
Terraform v1.2.1
on darwin_arm64

Your version of Terraform is out of date! The latest version
is 1.2.2. You can update by downloading from https://www.terraform.io/downloads.html
```

### Terraformer

https://github.com/GoogleCloudPlatform/terraformer

```shell
adceet-root $ asdf plugin add terraformer https://github.com/grimoh/asdf-terraformer.git
adceet-root $ asdf install terraformer 0.8.20
adceet-root $ asdf local terrformer 0.8.20
adceet-root $ mkdir ./temp
adceet-root/temp $ export GODEBUG=asyncpreemptoff=1
adceet-root/temp $ DATADOG_API_KEY="xxx" DATADOG_APP_KEY="xxx" terraformer import datadog --resources=dashboard --filter=datadog_dashboard=XXXXX
```

### Java Development Kit

```sh
$ asdf plugin add java
$ asdf list all java
$ asdf instal java temurin-11.0.14+101
$ asdf local java temurin-11.0.14+101
$ java -version
openjdk version "11.0.15" 2022-04-19
OpenJDK Runtime Environment Temurin-11.0.15+10 (build 11.0.15+10)
OpenJDK 64-Bit Server VM Temurin-11.0.15+10 (build 11.0.15+10, mixed mode)
```

### sbt

```sh
$ asdf plugin add sbt
$ asdf install sbt 1.6.2
$ asdf local sbt 1.6.2
```

### EKS

#### kubectl

Please install the same version of kubectl as the server side to avoid trouble.

```shell
$ KUBECTL_VERSION=1.21.13
$ asdf plugin-add kubectl https://github.com/asdf-community/asdf-kubectl.git
$ asdf install kubectl $KUBECTL_VERSION
akka-ddd-cqrs-es-example-typed $ asdf local kubectl $KUBECTL_VERSION # Always set up in the project root.
$ kubectl version
Client Version: version.Info{Major:"1", Minor:"21", GitVersion:"v1.21.13", GitCommit:"80ec6572b15ee0ed2e6efa97a4dcd30f57e68224", GitTreeState:"clean", BuildDate:"2022-05-24T12:40:44Z", GoVersion:"go1.16.15", Compiler:"gc", Platform:"darwin/arm64"}
Server Version: version.Info{Major:"1", Minor:"21+", GitVersion:"v1.21.12-eks-a64ea69", GitCommit:"d4336843ba36120e9ed1491fddff5f2fec33eb77", GitTreeState:"clean", BuildDate:"2022-05-12T18:29:27Z", GoVersion:"go1.16.15", Compiler:"gc", Platform:"linux/amd64"}
```

#### helm

```shell
$ asdf plugin-add helm https://github.com/Antiarchitect/asdf-helm.git
$ asdf install helm 3.9.0
$ asdf local helm 3.9.0
# Required plug-ins in helmfile
$ helm plugin install https://github.com/databus23/helm-diff
```

#### helmfile

```shell
$ asdf plugin-add helmfile https://github.com/feniix/asdf-helmfile.git
$ asdf install helmfile 0.144.0
$ asdf local helmfile 0.144.0
```

---

## Setup for AWS

### Add an aws profile

edit `~/.aws/credentials` as follows.

#### If AWS SSO is not used

Define just one entry.

```
# ...
[adceet]
aws_access_key_id=xxxxx
aws_secret_access_key=xxxxx
aws_session_token=xxxxx
```

#### If AWS SSO is used.

Define one for terraform and one for SSO separately.

`~/.aws/config`

```
[profile adceet-sso]
sso_start_url = https://d-95671bd3b6.awsapps.com/start
sso_region = us-east-1
sso_account_id = 1234567890
sso_role_name = SagradaOwnerAccess
region = us-east-1
```

`~/.aws/credentials`

```
# ...
[adceet-terraform]
aws_access_key_id=xxxxx
aws_secret_access_key=xxxxx
aws_session_token=xxxxx
# ...
[adceet-sso]
aws_access_key_id=xxxxx
aws_secret_access_key=xxxxx
aws_session_token=xxxxx
```

### copy env.sh.default as env.sh, and edit it

```shell
$ cp env.sh.default env.sh
```

Modify PREFIX, APPLICATION_NAME as appropriate.
If you want to create a personal environment, change PREFIX.

#### If AWS SSO is not used

```shell
export AWS_PROFILE_TERRAFORM=adceet
export AWS_PROFILE_SSO=adceet
export AWS_REGION=us-east-1
export AWS_ACCOUNT_ID=1234567890

export PREFIX=om2eep1k
export APPLICATION_NAME=adceet

export MODE=scala

if [[ "$OUTPUT_ENV" == 1 ]]; then
echo "--- Using Environments -----------------"
echo "AWS_PROFILE_TERRAFORM  = $AWS_PROFILE_TERRAFORM"
echo "AWS_PROFILE_SSO        = $AWS_PROFILE_SSO"
echo "AWS_REGION             = $AWS_REGION"
echo "PREFIX                 = $PREFIX"
echo "APPLICATION_NAME       = $APPLICATION_NAME"
echo "MODE                   = $MODE"
echo "----------------------------------------"
fi
```

#### If AWS SSO is used

```shell
export AWS_PROFILE_TERRAFORM=adceet-terraform
export AWS_PROFILE_SSO=adceet-sso
```

## Building an AWS environment with terraform 

Create a new file `akka-dddcqrs-es-example-typed/tools/terraform/${PREFIX}-${APPLICATION_NAME}-terraform.tfvars` with the following.
Changes defined in variables.tf can be overridden in this tfvars file.

```
akka_persistence_journal_name      = "Journal"
akka_persistence_journal_gsi_name  = "GetJournalRowsIndex"
akka_persistence_snapshot_name     = "Snapshot"
akka_persistence_snapshot_gsi_name = "GetSnapshotRowsIndex"

container_port       = 8081
akka_management_port = 8558
akka_remote_port     = 25520
number_of_shards     = 30

health_check_port = 8558
health_check_path = "/health/ready"

alb_enabled              = false
akka_persistence_enabled = true
ecs_enabled              = false

eks_version = "1.22"
eks_enabled      = true

jvm_heap_min = "1024m"
jvm_heap_max = "1024m"
jvm_meta_max = "512m"

datadog-api-key = "xxxx"

eks_auth_roles = []
eks_auth_users = []
eks_auth_accounts = []
```

### create a lock table

At first time only, Create a lock table for terraform on DynamoDB.

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./create-lock-table.sh
```

### create a s3 bucket for tfstate

At first time only, Create an s3 bucket to store tfstate.

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./create-tf-bucket.sh
```

### terraform init

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./terraform-init.sh
```

### terraform plan

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./terraform-plan.sh
```

### terraform apply

```shell
akka-ddd-cqrs-es-example-typed/tools/terraform $ ./terraform-apply.sh
```

## Confirm kubernetes-dashboard

### Port forward to kubernetes-dashboard

```shell
$ DASHBOARD_NS=kubernetes-dashboard
$ export POD_NAME=$(kubectl get pods -n $DASHBOARD_NS -l "app.kubernetes.io/name=kubernetes-dashboard,app.kubernetes.io/instance=kubernetes-dashboard" -o jsonpath="{.items[0].metadata.name}")
$ kubectl -n $DASHBOARD_NS port-forward $POD_NAME 8443:8443
```

### Configuring Chrome

1. Open `chrome://flags/#allow-insecure-localhost`
2. `Allow invalid certificates for resources loaded from localhost.` is `Enable`
3. Relaunch
4. Open `https://localhost:8443`

### Get token

```shell
$ DASHBOARD_NS=kubernetes-dashboard
$ kubectl -n $DASHBOARD_NS describe secret $(kubectl -n $DASHBOARD_NS get secret | grep kubernetes-dashboard-token | awk '{print $1}') | awk '$1=="token:"{print $2}'
```

---

## Debug on Docker Compose

### Build image

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

### Run the Docker Compose

```shell
tools/scripts $ ./docker-compose-up.sh
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

---

## Debug on local machine

### Debug by using IntelliJ IDEA

Edit Configurations

com.github.j5ik2o.api.write.Main

1. PORT=8081;AKKA_MANAGEMENT_HTTP_PORT=8558;JMX_PORT=8100
2. PORT=8082;AKKA_MANAGEMENT_HTTP_PORT=8559;JMX_PORT=8101
3. PORT=8083;AKKA_MANAGEMENT_HTTP_PORT=8560;JMX_PORT=8102

Create a configuration for all three and run it in IntelliJ IDEA. If you want to debug, run any one of the projects in debug.

---

## Debug on Local Kubernetes(on Docker)

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

Next deploy the backend roles.

```shell
$ cd tools/scripts

$ helmfile-apply-local-backend.sh
```

Wait a few moments for the cluster to form. Make sure there are no errors in the log.

```shell
$ stern 'write-api-server-*' -n adceet
```

Next deploy the frontend roles.

```shell
$ helmfile-apply-local-frontend.sh
```

After frontend is started, check the operation with the following commands.

```shell
curl -X GET http://localhost:30030/hello
```

---

## Debug on Minikube(on Docker)

First, check the resource settings for Docker for Mac. You must give it sufficient resources.

```shell
$ minikube-start.sh
```

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

First deploy the backend roles.

```shell
tools/scripts $ helmfile-apply-local-backend.sh
```

Wait a few moments for the cluster to form. Make sure there are no errors in the log.

```shell
$ stern 'write-api-server-*' -n adceet
```

Next deploy the frontend roles.

```shell
$ helmfile-apply-local-frontend.sh
```

```shell
$ minikube-service-url.sh
http://127.0.0.1:61391
...
```

After frontend is started, check the operation with the following commands.

```shell
curl -X GET http://127.0.0.1:61391/hello
```

