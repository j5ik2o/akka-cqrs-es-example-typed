# The Tools Installation

## asdf

```sh
adceet-root $ brew install asdf
```

## jq

```shell
adceet-root $ asdf plugin-add jq https://github.com/AZMCode/asdf-jq.git
adceet-root $ asdf install jq 1.6
adceet-root $ asdf local jq 1.6
adceet-root $ jq --version
jq-1.6
```

## awscli

```shell
adceet-root $ asdf plugin add awscli
adceet-root $ asdf install awscli 2.7.6
adceet-root $ asdf local  awscli 2.7.6
adceet-root $ aws --version
aws-cli/2.7.6 Python/3.9.11 Darwin/21.5.0 exe/x86_64 prompt/off
```

## terraform

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

## terraformer(Optional)

https://github.com/GoogleCloudPlatform/terraformer

```shell
adceet-root $ asdf plugin add terraformer https://github.com/grimoh/asdf-terraformer.git
adceet-root $ asdf install terraformer 0.8.20
adceet-root $ asdf local terraformer 0.8.20
adceet-root $ mkdir ./temp
adceet-root/temp $ export GODEBUG=asyncpreemptoff=1
adceet-root/temp $ DATADOG_API_KEY="xxx" DATADOG_APP_KEY="xxx" terraformer import datadog --resources=dashboard --filter=datadog_dashboard=XXXXX
```

## Java Development Kit

```sh
adceet-root $ asdf plugin add java
adceet-root $ asdf list all java
adceet-root $ asdf install java temurin-17.0.4+101
adceet-root $ asdf local java temurin-17.0.4+101
# Set the appropriate settings for your environment.
adceet-root $ echo "export JAVA_OPTS='--enable-preview'" >> ~/.bashrc
adceet-root $ java -version
openjdk version "17.0.4.1" 2022-08-12
OpenJDK Runtime Environment Temurin-17.0.4.1+1 (build 17.0.4.1+1)
OpenJDK 64-Bit Server VM Temurin-17.0.4.1+1 (build 17.0.4.1+1, mixed mode)
```

**CAUTION: If "export JAVA_OPTS='--enable-preview'" is not set, compilation will fail.**

## sbt

```shell
adceet-root $ asdf plugin add sbt
adceet-root $ asdf install sbt 1.6.2
adceet-root $ asdf local sbt 1.6.2
# Set the appropriate settings for your environment.
$ echo "export SBT_OPTS='-Xms8g -Xmx12g -XX:MaxMetaspaceSize=1g -XX:ReservedCodeCacheSize=1000m'" >> ~/.bashrc
```

## Kubernetes

### kubectl

Please install the same version of kubectl as the server side to avoid trouble.

```shell
adceet-root $ KUBECTL_VERSION=1.21.13
adceet-root $ asdf plugin-add kubectl https://github.com/asdf-community/asdf-kubectl.git
adceet-root $ asdf install kubectl $KUBECTL_VERSION
adceet-root $ asdf local kubectl $KUBECTL_VERSION # Always set up in the project root.
adceet-root $ kubectl version
Client Version: version.Info{Major:"1", Minor:"21", GitVersion:"v1.21.13", GitCommit:"80ec6572b15ee0ed2e6efa97a4dcd30f57e68224", GitTreeState:"clean", BuildDate:"2022-05-24T12:40:44Z", GoVersion:"go1.16.15", Compiler:"gc", Platform:"darwin/arm64"}
Server Version: version.Info{Major:"1", Minor:"21+", GitVersion:"v1.21.12-eks-a64ea69", GitCommit:"d4336843ba36120e9ed1491fddff5f2fec33eb77", GitTreeState:"clean", BuildDate:"2022-05-12T18:29:27Z", GoVersion:"go1.16.15", Compiler:"gc", Platform:"linux/amd64"}
```

### stern

```shell
adceet-root $ asdf plugin-add stern https://github.com/looztra/asdf-stern
adceet-root $ asdf install stern 1.22.0
adceet-root $ asdf local stern 1.22.0
```

### helm

```shell
adceet-root $ asdf plugin-add helm https://github.com/Antiarchitect/asdf-helm.git
adceet-root $ asdf install helm 3.9.0
adceet-root $ asdf local helm 3.9.0
# Required plug-ins in helmfile
adceet-root $ helm plugin install https://github.com/databus23/helm-diff
```

### helmfile

```shell
adceet-root $ asdf plugin-add helmfile https://github.com/feniix/asdf-helmfile.git
adceet-root $ asdf install helmfile 0.144.0
adceet-root $ asdf local helmfile 0.144.0
```

### minikube (optional)

```shell
adceet-root $ asdf plugin-add minikube https://github.com/alvarobp/asdf-minikube.git
adceet-root $ asdf install minikube 1.26.1
adceet-root $ asdf local minikube 1.26.1
```
